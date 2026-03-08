package com.geobeats.app.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.geobeats.app.domain.models.PlaceLocation
import com.geobeats.app.presentation.components.AppButton
import com.geobeats.app.presentation.components.ModeSelectorCard
import com.geobeats.app.presentation.viewmodel.MapViewModel
import com.geobeats.app.ui.theme.SpotifyGreen
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SplashScreen(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = SpotifyGreen
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "GeoBeats",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "El mapa de tu banda sonora",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 500)) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                AppButton(
                    text = "Comenzar",
                    onClick = onNext,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
fun ModeSelectionScreen(
    onUserModeSelected: () -> Unit,
    onDevModeSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a GeoBeats",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecciona cómo quieres usar la app",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        ModeSelectorCard(
            title = "Modo Usuario",
            description = "Explora lugares y escucha música asociada.",
            icon = Icons.Default.Person,
            onClick = onUserModeSelected
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ModeSelectorCard(
            title = "Modo Desarrollador",
            description = "Agrega puntos al mapa con playlists.",
            icon = Icons.Default.Build,
            onClick = onDevModeSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMapScreen(viewModel: MapViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Explorar Lugares", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (hasLocationPermission) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    uiState.places.forEach { place ->
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            title = place.name,
                            onClick = {
                                viewModel.onPlaceSelected(place)
                                true
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Se requiere permiso de ubicación para ver el mapa")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                            Text("Otorgar Permiso")
                        }
                    }
                }
            }
        }

        if (uiState.selectedPlace != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelectedPlace() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                PlaceDetailContent(
                    place = uiState.selectedPlace!!,
                    distance = uiState.distanceToSelectedPlace,
                    onPlayPlaylist = { playlistId ->
                        viewModel.playSpotifyPlaylist(playlistId)
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceDetailContent(place: PlaceLocation, distance: Double?, onPlayPlaylist: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = SpotifyGreen
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        distance?.let {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "A ${"%.0f".format(it)}m de ti",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = place.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            text = "Reproducir Playlist",
            onClick = { onPlayPlaylist(place.spotifyPlaylistId) }
        )
        
        Text(
            text = "Abre Spotify automáticamente",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperMapScreen(viewModel: MapViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf<LatLng?>(null) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel Desarrollador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Help or Info */ },
                icon = { Icon(Icons.Default.AddLocation, null) },
                text = { Text("Mantén presionado el mapa") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                onMapLongClick = { latLng ->
                    showForm = latLng
                }
            ) {
                uiState.places.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }

        if (showForm != null) {
            ModalBottomSheet(
                onDismissRequest = { showForm = null }
            ) {
                AddPlaceForm(
                    latLng = showForm!!,
                    onSave = { newPlace ->
                        viewModel.addPlace(newPlace)
                        showForm = null
                    }
                )
            }
        }
    }
}

@Composable
fun AddPlaceForm(latLng: LatLng, onSave: (PlaceLocation) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var playlistId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Nuevo Punto Musical",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Lat: ${"%.5f".format(latLng.latitude)}, Lon: ${"%.5f".format(latLng.longitude)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del lugar") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = playlistId,
            onValueChange = { playlistId = it },
            label = { Text("Spotify Playlist ID") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Ej: 37i9dQZF1DXcBWIGoYBM3M") }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppButton(
            text = "Guardar Punto",
            onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        PlaceLocation(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            spotifyPlaylistId = playlistId
                        )
                    )
                }
            }
        )
    }
}
