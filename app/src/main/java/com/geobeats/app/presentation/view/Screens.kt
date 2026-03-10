package com.geobeats.app.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
            description = "Agrega y gestiona puntos del mapa.",
            icon = Icons.Default.Build,
            onClick = onDevModeSelected
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
        
        Text("Acceso Desarrollador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = false },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = false },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error) {
            Text("Credenciales incorrectas", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppButton(
            text = "Entrar",
            onClick = {
                if (username == "admin" && password == "admin123") {
                    onLoginSuccess()
                } else {
                    error = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMapScreen(viewModel: MapViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(9.9333, -84.0833), 15f)
    }

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
                    cameraPositionState = cameraPositionState,
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
                    distance = uiState.distanceToSelectedPlace
                )
            }
        }
    }
}

@Composable
fun PlaceDetailContent(place: PlaceLocation, distance: Double?) {
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
        
        Spacer(modifier = Modifier.height(24.dp))

        // Spotify Embed Player inside WebView
        SpotifyEmbedPlayer(playlistId = place.spotifyPlaylistId)
    }
}

@Composable
fun SpotifyEmbedPlayer(playlistId: String) {
    val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId?utm_source=generator"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) {
                    webView.loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperMapScreen(viewModel: MapViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddFormAt by remember { mutableStateOf<LatLng?>(null) }
    var selectedPlaceForAction by remember { mutableStateOf<PlaceLocation?>(null) }
    var showEditFormFor by remember { mutableStateOf<PlaceLocation?>(null) }
    var showDeleteConfirmFor by remember { mutableStateOf<PlaceLocation?>(null) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(9.9333, -84.0833), 15f)
    }

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
                onClick = { /* Help info toast or similar */ },
                icon = { Icon(Icons.Default.Info, null) },
                text = { Text("Mantén presionado el mapa para agregar") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    showAddFormAt = latLng
                }
            ) {
                uiState.places.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        onClick = {
                            selectedPlaceForAction = place
                            true
                        }
                    )
                }
            }
        }

        // Menú de opciones para un punto seleccionado
        if (selectedPlaceForAction != null) {
            ModalBottomSheet(onDismissRequest = { selectedPlaceForAction = null }) {
                Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                    Text(
                        text = selectedPlaceForAction!!.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showEditFormFor = selectedPlaceForAction
                            selectedPlaceForAction = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modificar Detalles")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            showDeleteConfirmFor = selectedPlaceForAction
                            selectedPlaceForAction = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Punto")
                    }
                }
            }
        }

        // Formulario para AGREGAR
        if (showAddFormAt != null) {
            ModalBottomSheet(onDismissRequest = { showAddFormAt = null }) {
                PlaceForm(
                    latLng = showAddFormAt!!,
                    onSave = { newPlace ->
                        viewModel.addPlace(newPlace)
                        showAddFormAt = null
                    }
                )
            }
        }

        // Formulario para EDITAR
        if (showEditFormFor != null) {
            ModalBottomSheet(onDismissRequest = { showEditFormFor = null }) {
                PlaceForm(
                    latLng = LatLng(showEditFormFor!!.latitude, showEditFormFor!!.longitude),
                    initialPlace = showEditFormFor,
                    onSave = { updatedPlace ->
                        viewModel.updatePlace(updatedPlace)
                        showEditFormFor = null
                    }
                )
            }
        }

        // Confirmación de ELIMINAR
        if (showDeleteConfirmFor != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmFor = null },
                title = { Text("Eliminar Punto") },
                text = { Text("¿Estás seguro que deseas eliminar '${showDeleteConfirmFor?.name}'? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deletePlace(showDeleteConfirmFor!!.id)
                        showDeleteConfirmFor = null
                    }) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmFor = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun PlaceForm(
    latLng: LatLng, 
    initialPlace: PlaceLocation? = null,
    onSave: (PlaceLocation) -> Unit
) {
    var name by remember { mutableStateOf(initialPlace?.name ?: "") }
    var description by remember { mutableStateOf(initialPlace?.description ?: "") }
    var playlistId by remember { mutableStateOf(initialPlace?.spotifyPlaylistId ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (initialPlace == null) "Nuevo Punto Musical" else "Modificar Punto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Coordenadas: ${"%.5f".format(latLng.latitude)}, ${"%.5f".format(latLng.longitude)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del lugar") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
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
            placeholder = { Text("Ej: 37i9dQZF1DXcBWIGoYBM3M") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppButton(
            text = if (initialPlace == null) "Guardar Punto" else "Guardar Cambios",
            onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        PlaceLocation(
                            id = initialPlace?.id ?: UUID.randomUUID().toString(),
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
