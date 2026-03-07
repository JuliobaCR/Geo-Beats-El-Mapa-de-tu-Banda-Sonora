package com.geobeats.app.domain.repository

import com.geobeats.app.domain.models.PlaceLocation
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getPlaces(): Flow<List<PlaceLocation>>
    suspend fun addPlace(place: PlaceLocation)
}
