package com.geobeats.app.future.spotify

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

class SpotifyControllerImpl(private val context: Context) : SpotifyController {

    override fun connect() {
        // Con Implicit Intent no necesitamos conexión previa
    }

    override fun playPlaylist(playlistId: String) {
        val uri = "spotify:playlist:$playlistId".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Spotify no está instalado, abrimos en el navegador
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                "https://open.spotify.com/playlist/$playlistId".toUri()
            )
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    override fun pause() {
        // No aplica con Implicit Intent
    }
}