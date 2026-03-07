package com.geobeats.app.data.repository

import com.geobeats.app.data.datasource.LocalPlacesDataSource
import com.geobeats.app.domain.models.PlaceLocation
import com.geobeats.app.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow

class PlacesRepositoryImpl(
    private val dataSource: LocalPlacesDataSource
) : PlacesRepository {
    override fun getPlaces(): Flow<List<PlaceLocation>> {
        return dataSource.getPlacesFlow()
    }

    override suspend fun addPlace(place: PlaceLocation) {
        dataSource.addPlace(place)
    }
}
