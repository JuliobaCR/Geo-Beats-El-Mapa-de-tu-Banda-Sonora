package com.geobeats.app.domain.models

data class PlaceLocation(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val spotifyPlaylistId: String
)
