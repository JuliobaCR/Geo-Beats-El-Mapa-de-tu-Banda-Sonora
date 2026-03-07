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
            latitude = 40.785091,
            longitude = -73.968285,
            spotifyPlaylistId = "37i9dQZF1DX4WYpdgoIcnS"
        ),
        PlaceLocation(
            id = "2",
            name = "Tienda de Discos",
            description = "Encuentra los mejores vinilos mientras escuchas rock clásico.",
            latitude = 40.7810,
            longitude = -73.9660,
            spotifyPlaylistId = "37i9dQZF1DXcF6BvBaseic"
        ),
        PlaceLocation(
            id = "3",
            name = "Plaza del Sol",
            description = "El corazón de la ciudad, ritmo urbano asegurado.",
            latitude = 40.7870,
            longitude = -73.9700,
            spotifyPlaylistId = "37i9dQZF1DXcBWIGoYBM3M"
        )
    ))
    
    fun getPlacesFlow(): StateFlow<List<PlaceLocation>> = _places.asStateFlow()

    fun addPlace(place: PlaceLocation) {
        _places.update { currentList ->
            currentList + place
        }
    }
}
