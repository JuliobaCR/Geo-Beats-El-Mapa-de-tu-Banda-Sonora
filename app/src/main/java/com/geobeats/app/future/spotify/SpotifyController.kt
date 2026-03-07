package com.geobeats.app.future.spotify

interface SpotifyController {
    fun connect()
    fun playPlaylist(playlistId: String)
    fun pause()
}
