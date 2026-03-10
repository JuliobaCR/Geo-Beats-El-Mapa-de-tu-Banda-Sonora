package com.geobeats.app.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geobeats.app.domain.models.PlaceLocation
import com.geobeats.app.domain.repository.PlacesRepository
import com.geobeats.app.future.spotify.SpotifyController
import com.geobeats.app.services.distance.DistanceCalculator
import com.geobeats.app.services.location.LocationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MapUiState(
    val userLocation: Location? = null,
    val places: List<PlaceLocation> = emptyList(),
    val selectedPlace: PlaceLocation? = null,
    val distanceToSelectedPlace: Double? = null,
    val nearbyPlace: PlaceLocation? = null,
    val error: String? = null
)

class MapViewModel(
    private val locationService: LocationService,
    private val placesRepository: PlacesRepository,
    private val distanceCalculator: DistanceCalculator,
    private val spotifyController: SpotifyController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadPlaces()
        startLocationUpdates()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            placesRepository.getPlaces().collect { places ->
                _uiState.update { it.copy(places = places) }
            }
        }
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationService.getLocationUpdates()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { location ->
                    _uiState.update { it.copy(userLocation = location) }
                    checkProximity(location)
                    updateDistanceToSelected(location)
                }
        }
    }

    private fun checkProximity(location: Location) {
        val nearby = _uiState.value.places.find { place ->
            distanceCalculator.calculateDistanceMeters(
                location.latitude, location.longitude,
                place.latitude, place.longitude
            ) <= 100.0
        }

        if (nearby != null && nearby.id != _uiState.value.nearbyPlace?.id) {
            // New place detected within 100m
            _uiState.update { it.copy(nearbyPlace = nearby, selectedPlace = nearby) }
            spotifyController.playPlaylist(nearby.spotifyPlaylistId)
        } else if (nearby == null && _uiState.value.nearbyPlace != null) {
            _uiState.update { it.copy(nearbyPlace = null) }
        }
    }

    private fun updateDistanceToSelected(location: Location) {
        val selected = _uiState.value.selectedPlace ?: return
        val distance = distanceCalculator.calculateDistanceMeters(
            location.latitude, location.longitude,
            selected.latitude, selected.longitude
        )
        _uiState.update { it.copy(distanceToSelectedPlace = distance) }
    }

    fun onPlaceSelected(place: PlaceLocation) {
        val userLoc = _uiState.value.userLocation
        val distance = if (userLoc != null) {
            distanceCalculator.calculateDistanceMeters(
                userLoc.latitude, userLoc.longitude,
                place.latitude, place.longitude
            )
        } else null

        _uiState.update { it.copy(selectedPlace = place, distanceToSelectedPlace = distance) }
    }

    fun clearSelectedPlace() {
        _uiState.update { it.copy(selectedPlace = null, distanceToSelectedPlace = null) }
    }

    fun addPlace(place: PlaceLocation) {
        viewModelScope.launch {
            placesRepository.addPlace(place)
        }
    }

    fun updatePlace(place: PlaceLocation) {
        viewModelScope.launch {
            placesRepository.updatePlace(place)
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            placesRepository.deletePlace(placeId)
        }
    }

    fun playSpotifyPlaylist(playlistId: String) {
        spotifyController.playPlaylist(playlistId)
    }
}
