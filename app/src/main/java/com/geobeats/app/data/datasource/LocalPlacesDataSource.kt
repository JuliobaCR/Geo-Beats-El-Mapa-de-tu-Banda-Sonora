package com.geobeats.app.data.datasource

import com.geobeats.app.domain.models.PlaceLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalPlacesDataSource {
    private val _places = MutableStateFlow(listOf(
        PlaceLocation(
            id = "1",
            name = "Parque Central",
            description = "Un hermoso parque para relajarse con música chill.",
            latitude = 9.9333, // Costa Rica default instead of NY
            longitude = -84.0833,
            spotifyPlaylistId = "37i9dQZF1DX4WYpdgoIcnS"
        ),
        PlaceLocation(
            id = "2",
            name = "Tienda de Discos",
            description = "Encuentra los mejores vinilos mientras escuchas rock clásico.",
            latitude = 9.9350,
            longitude = -84.0850,
            spotifyPlaylistId = "37i9dQZF1DXcF6BvBaseic"
        ),
        PlaceLocation(
            id = "3",
            name = "Plaza del Sol",
            description = "El corazón de la ciudad, ritmo urbano asegurado.",
            latitude = 9.9370,
            longitude = -84.0800,
            spotifyPlaylistId = "37i9dQZF1DXcBWIGoYBM3M"
        )
    ))
    
    fun getPlacesFlow(): StateFlow<List<PlaceLocation>> = _places.asStateFlow()

    fun addPlace(place: PlaceLocation) {
        _places.update { currentList ->
            currentList + place
        }
    }

    fun updatePlace(place: PlaceLocation) {
        _places.update { currentList ->
            currentList.map { if (it.id == place.id) place else it }
        }
    }

    fun deletePlace(placeId: String) {
        _places.update { currentList ->
            currentList.filter { it.id != placeId }
        }
    }
}
