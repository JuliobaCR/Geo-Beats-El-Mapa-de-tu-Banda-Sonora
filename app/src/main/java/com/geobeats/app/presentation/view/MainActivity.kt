package com.geobeats.app.presentation.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.geobeats.app.data.datasource.LocalPlacesDataSource
import com.geobeats.app.data.repository.PlacesRepositoryImpl
import com.geobeats.app.future.spotify.SpotifyControllerImpl
import com.geobeats.app.presentation.navigation.NavGraph
import com.geobeats.app.presentation.viewmodel.MapViewModel
import com.geobeats.app.services.distance.DistanceCalculator
import com.geobeats.app.services.location.LocationService
import com.geobeats.app.ui.theme.GeoBeatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationService = LocationService(this)
        val dataSource = LocalPlacesDataSource()
        val repository = PlacesRepositoryImpl(dataSource)
        val distanceCalculator = DistanceCalculator()
        val spotifyController = SpotifyControllerImpl(this)  // ← agregar esto
        val viewModel = MapViewModel(locationService, repository, distanceCalculator, spotifyController)

        setContent {
            GeoBeatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}