package com.followmemobile.camidecavalls.presentation.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Screen for GPS tracking functionality.
 * Shows real-time location data and tracking controls.
 */
data class TrackingScreen(val routeId: Int? = null) : Screen {

    @Composable
    override fun Content() {
        val screenModel: TrackingScreenModel = koinInject { parametersOf(routeId) }
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        // Cleanup when screen leaves composition
        DisposableEffect(screenModel) {
            onDispose {
                screenModel.onDispose()
            }
        }

        val permissionRequester = rememberPermissionRequester { granted ->
            if (granted) {
                screenModel.startTracking()
            } else {
                // Permission denied - will be shown through UI state
                screenModel.requestPermission {
                    // This won't be called since permission was already denied
                }
            }
        }

        TrackingScreenContent(
            uiState = uiState,
            onStartTracking = {
                if (screenModel.isPermissionGranted()) {
                    screenModel.startTracking()
                } else {
                    permissionRequester()
                }
            },
            onStartTrackingForced = { screenModel.startTrackingForced() },
            onCancelConfirmation = { screenModel.cancelConfirmation() },
            onStopTracking = { screenModel.stopTracking() },
            onBackClick = { navigator.pop() },
            onClearError = { screenModel.clearError() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingScreenContent(
    uiState: TrackingUiState,
    onStartTracking: () -> Unit,
    onStartTrackingForced: () -> Unit,
    onCancelConfirmation: () -> Unit,
    onStopTracking: () -> Unit,
    onBackClick: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState) {
                is TrackingUiState.Idle -> {
                    IdleContent(
                        route = uiState.route,
                        currentLocation = uiState.currentLocation,
                        onStartTracking = onStartTracking
                    )
                }

                is TrackingUiState.AwaitingConfirmation -> {
                    // Show idle map with confirmation dialog on top
                    IdleContent(
                        route = uiState.route,
                        currentLocation = uiState.currentLocation,
                        onStartTracking = onStartTracking
                    )
                    ConfirmationDialog(
                        distanceKm = uiState.distanceFromRoute / 1000.0,
                        onConfirm = onStartTrackingForced,
                        onCancel = onCancelConfirmation
                    )
                }

                is TrackingUiState.Tracking -> {
                    TrackingContent(
                        route = uiState.route,
                        sessionId = uiState.sessionId,
                        currentLocation = uiState.currentLocation,
                        trackPoints = uiState.trackPoints,
                        onStopTracking = onStopTracking
                    )
                }

                is TrackingUiState.Completed -> {
                    CompletedContent(
                        session = uiState.session,
                        onNewSession = onStartTracking
                    )
                }

                is TrackingUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        onRetry = onStartTracking,
                        onDismiss = onClearError
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(
    route: Route?,
    currentLocation: LocationData?,
    onStartTracking: () -> Unit
) {
    val cameraPosition = calculateCameraPosition(route, currentLocation)

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen map
        key("idle-map-${route?.id ?: "no-route"}") {
            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = cameraPosition.latitude,
                longitude = cameraPosition.longitude,
                zoom = cameraPosition.zoom,
                styleUrl = "https://tiles.openfreemap.org/styles/liberty",
                onMapReady = { controller ->
                    // Add route path if available
                    route?.gpxData?.let { geoJson ->
                        controller.addRoutePath(
                            routeId = "route-${route.id}",
                            geoJsonLineString = geoJson,
                            color = "#2196F3",  // Blue
                            width = 4f
                        )
                    }

                    // Add current location marker if available
                    currentLocation?.let { location ->
                        controller.addMarker(
                            markerId = "current-location",
                            latitude = location.latitude,
                            longitude = location.longitude,
                            color = "#4CAF50",  // Green
                            radius = 8f
                        )
                    }
                }
            )
        }

        // Start Tracking FAB
        FloatingActionButton(
            onClick = onStartTracking,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text("Start Tracking")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingContent(
    route: Route?,
    sessionId: String,
    currentLocation: LocationData?,
    trackPoints: List<TrackPoint>,
    onStopTracking: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // GPS following state - enabled by default
    var followGpsLocation by remember { mutableStateOf(true) }
    var lastKnownPosition by remember { mutableStateOf<CameraPosition?>(null) }
    var currentZoom by remember { mutableStateOf<Double?>(null) }

    // Calculate camera position based on GPS following state
    val cameraPosition = if (followGpsLocation) {
        calculateCameraPosition(route, currentLocation).also {
            lastKnownPosition = it
        }
    } else {
        lastKnownPosition ?: calculateCameraPosition(route, currentLocation)
    }

    // Remember the map controller for dynamic updates
    var mapController by remember { mutableStateOf<MapLayerController?>(null) }

    // LaunchedEffect 1: Draw route (blue) - ONLY once when route is loaded
    LaunchedEffect(mapController, route) {
        val controller = mapController ?: return@LaunchedEffect

        route?.gpxData?.let { geoJson ->
            controller.addRoutePath(
                routeId = "route-${route.id}",
                geoJsonLineString = geoJson,
                color = "#2196F3",
                width = 4f
            )
        }
    }

    // LaunchedEffect 2: Update user track (green) - ONLY when trackPoints change
    LaunchedEffect(mapController, trackPoints.size) {
        val controller = mapController ?: return@LaunchedEffect

        if (trackPoints.size >= 2) {
            val userTrackGeoJson = trackPointsToGeoJson(trackPoints)
            controller.addRoutePath(
                routeId = "user-track",
                geoJsonLineString = userTrackGeoJson,
                color = "#4CAF50",
                width = 5f
            )
        }
    }

    // LaunchedEffect 3: Update marker (red) - ONLY when location changes
    LaunchedEffect(mapController, currentLocation?.latitude, currentLocation?.longitude) {
        val controller = mapController ?: return@LaunchedEffect

        currentLocation?.let { location ->
            controller.removeLayer("current-location")
            controller.addMarker(
                markerId = "current-location",
                latitude = location.latitude,
                longitude = location.longitude,
                color = "#FF5722",
                radius = 10f
            )
        }
    }

    // Separate LaunchedEffect to update camera position (when GPS following is enabled)
    LaunchedEffect(mapController, followGpsLocation, cameraPosition) {
        val controller = mapController ?: return@LaunchedEffect

        if (followGpsLocation) {
            controller.updateCamera(
                latitude = cameraPosition.latitude,
                longitude = cameraPosition.longitude,
                zoom = null,
                animated = true
            )
        }
    }

    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            mapController = null
        }
    }

    // Initial camera position (only used once at map creation)
    val initialPosition = remember { calculateCameraPosition(route, currentLocation) }

    // Stabilize callbacks to prevent recomposition
    val onMapReadyCallback = remember {
        { controller: MapLayerController ->
            mapController = controller
            currentZoom = controller.getCurrentZoom()
        }
    }

    val onCameraMovedCallback = remember {
        {
            followGpsLocation = false
        }
    }

    val onZoomChangedCallback = remember {
        { newZoom: Double ->
            currentZoom = newZoom
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen map with route and user track
        key("tracking-map-$sessionId") {
            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = initialPosition.latitude,
                longitude = initialPosition.longitude,
                zoom = initialPosition.zoom,
                onMapReady = onMapReadyCallback,
                onCameraMoved = onCameraMovedCallback,
                onZoomChanged = onZoomChangedCallback
            )
        }

        // GPS following toggle button (left side)
        FloatingActionButton(
            onClick = {
                followGpsLocation = !followGpsLocation
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = if (followGpsLocation) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Icon(
                imageVector = if (followGpsLocation) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                contentDescription = if (followGpsLocation) "GPS Following Enabled" else "GPS Following Disabled",
                tint = if (followGpsLocation) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // Bottom FAB row with Stop and Stats buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stats FAB
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Info, contentDescription = "Show Statistics")
            }

            // Stop Tracking FAB
            FloatingActionButton(
                onClick = onStopTracking,
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop Tracking")
            }
        }
    }

    // Bottom Sheet with statistics
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tracking Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (currentLocation != null) {
                    LocationInfoRow("Latitude", "${(currentLocation.latitude * 1000000).toInt() / 1000000.0}°")
                    LocationInfoRow("Longitude", "${(currentLocation.longitude * 1000000).toInt() / 1000000.0}°")

                    currentLocation.altitude?.let {
                        LocationInfoRow("Altitude", "${(it * 10).toInt() / 10.0} m")
                    }

                    currentLocation.accuracy?.let {
                        LocationInfoRow("Accuracy", "±${(it * 10).toInt() / 10.0} m")
                    }

                    currentLocation.speed?.let {
                        val speedKmh = it * 3.6
                        LocationInfoRow("Speed", "${(speedKmh * 10).toInt() / 10.0} km/h")
                    }
                } else {
                    Text(
                        text = "Acquiring GPS signal...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Session: ${sessionId.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LocationInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CompletedContent(
    session: com.followmemobile.camidecavalls.domain.model.TrackingSession?,
    onNewSession: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tracking Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (session != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Session Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LocationInfoRow(
                        "Distance",
                        "${((session.distanceMeters / 1000.0) * 100).toInt() / 100.0} km"
                    )
                    LocationInfoRow(
                        "Duration",
                        "${session.durationSeconds / 3600}h ${(session.durationSeconds % 3600) / 60}m"
                    )
                    LocationInfoRow(
                        "Avg Speed",
                        "${(session.averageSpeedKmh * 10).toInt() / 10.0} km/h"
                    )
                    LocationInfoRow(
                        "Elevation Gain",
                        "+${session.elevationGainMeters} m"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onNewSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start New Session")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Dismiss")
            }

            FilledTonalButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    distanceKm: Double,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Sei lontano dal percorso")
        },
        text = {
            val distanceFormatted = (distanceKm * 10).toInt() / 10.0
            Text(
                "Ti trovi a $distanceFormatted km dal percorso più vicino. Vuoi iniziare il tracking comunque?"
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text("Inizia comunque")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Annulla")
            }
        }
    )
}

/**
 * Convert list of TrackPoints to GeoJSON LineString format
 */
private fun trackPointsToGeoJson(trackPoints: List<TrackPoint>): String {
    val coordinates = trackPoints.map { point ->
        JsonArray(listOf(
            JsonPrimitive(point.longitude),
            JsonPrimitive(point.latitude)
        ))
    }

    return buildJsonObject {
        put("type", JsonPrimitive("LineString"))
        put("coordinates", JsonArray(coordinates))
    }.toString()
}

/**
 * Parse GeoJSON LineString coordinates from JSON string.
 * Returns list of (longitude, latitude) pairs.
 */
private fun parseGeoJsonLineString(geoJson: String): List<Pair<Double, Double>> {
    return try {
        val json = Json.parseToJsonElement(geoJson).jsonObject
        val coordinates = json["coordinates"]?.jsonArray ?: return emptyList()

        coordinates.map { coord ->
            val array = coord.jsonArray
            val lon = array[0].jsonPrimitive.content.toDouble()
            val lat = array[1].jsonPrimitive.content.toDouble()
            Pair(lon, lat)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Calculate camera position from route or location
 */
private data class CameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double
)

private fun calculateCameraPosition(
    route: Route?,
    location: LocationData?
): CameraPosition {
    // If we have current location, center on it
    location?.let {
        return CameraPosition(
            latitude = it.latitude,
            longitude = it.longitude,
            zoom = 14.0
        )
    }

    // Otherwise, if we have route, center on route center
    route?.gpxData?.let { geoJson ->
        val coordinates = parseGeoJsonLineString(geoJson)
        if (coordinates.isNotEmpty()) {
            val lats = coordinates.map { it.second }
            val lons = coordinates.map { it.first }
            val centerLat = (lats.minOrNull()!! + lats.maxOrNull()!!) / 2.0
            val centerLon = (lons.minOrNull()!! + lons.maxOrNull()!!) / 2.0
            return CameraPosition(centerLat, centerLon, 12.0)
        }
    }

    // Default: Menorca center
    return CameraPosition(39.95, 4.05, 10.5)
}
