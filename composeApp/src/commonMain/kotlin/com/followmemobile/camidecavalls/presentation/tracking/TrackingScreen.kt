package com.followmemobile.camidecavalls.presentation.tracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import camidecavalls.composeapp.generated.resources.Res
import camidecavalls.composeapp.generated.resources.back
import camidecavalls.composeapp.generated.resources.cancel
import camidecavalls.composeapp.generated.resources.retry
import camidecavalls.composeapp.generated.resources.tracking_acquiring_signal
import camidecavalls.composeapp.generated.resources.tracking_avg_speed
import camidecavalls.composeapp.generated.resources.tracking_completed
import camidecavalls.composeapp.generated.resources.tracking_distance
import camidecavalls.composeapp.generated.resources.tracking_duration
import camidecavalls.composeapp.generated.resources.tracking_error
import camidecavalls.composeapp.generated.resources.tracking_altitude
import camidecavalls.composeapp.generated.resources.tracking_far_message
import camidecavalls.composeapp.generated.resources.tracking_far_title
import camidecavalls.composeapp.generated.resources.tracking_gps_follow_disabled
import camidecavalls.composeapp.generated.resources.tracking_gps_follow_enabled
import camidecavalls.composeapp.generated.resources.tracking_navigation_open_menu
import camidecavalls.composeapp.generated.resources.tracking_latitude
import camidecavalls.composeapp.generated.resources.tracking_longitude
import camidecavalls.composeapp.generated.resources.tracking_accuracy
import camidecavalls.composeapp.generated.resources.tracking_session_prefix
import camidecavalls.composeapp.generated.resources.tracking_session_summary
import camidecavalls.composeapp.generated.resources.tracking_show_statistics
import camidecavalls.composeapp.generated.resources.tracking_start
import camidecavalls.composeapp.generated.resources.tracking_start_anyway
import camidecavalls.composeapp.generated.resources.tracking_start_new_session
import camidecavalls.composeapp.generated.resources.tracking_pause
import camidecavalls.composeapp.generated.resources.tracking_resume
import camidecavalls.composeapp.generated.resources.tracking_statistics_title
import camidecavalls.composeapp.generated.resources.tracking_stop
import camidecavalls.composeapp.generated.resources.tracking_title
import camidecavalls.composeapp.generated.resources.tracking_speed
import camidecavalls.composeapp.generated.resources.tracking_elevation_gain
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreen
import com.followmemobile.camidecavalls.presentation.home.DrawerContent
import com.followmemobile.camidecavalls.presentation.home.DrawerScreen
import com.followmemobile.camidecavalls.presentation.home.RoutesScreen
import com.followmemobile.camidecavalls.presentation.home.RoutesUiState
import com.followmemobile.camidecavalls.presentation.map.MapCameraConfig
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.map.rememberMenorcaViewportState
import com.followmemobile.camidecavalls.presentation.pois.POIsScreen
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreen
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.math.roundToInt
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.jetbrains.compose.resources.stringResource

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

        val languageRepository: LanguageRepository = koinInject()
        val systemLanguage = remember { languageRepository.getSystemLanguage() }
        val currentLanguage by languageRepository.observeCurrentLanguage().collectAsState(initial = systemLanguage)
        val localizedStrings = remember(currentLanguage) { LocalizedStrings(currentLanguage) }

        val onStartTracking = {
            if (screenModel.isPermissionGranted()) {
                screenModel.startTracking()
            } else {
                permissionRequester()
            }
        }

        val onStartNewSession = {
            screenModel.startNewSession()
        }

        if (routeId == null) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        uiState = convertTrackingToRoutesUiState(localizedStrings, currentLanguage),
                        currentScreen = DrawerScreen.TRACKING,
                        onAboutClick = {
                            scope.launch { drawerState.close() }
                            navigator.replaceAll(AboutScreen())
                        },
                        onRoutesClick = {
                            scope.launch { drawerState.close() }
                            navigator.replaceAll(RoutesScreen())
                        },
                        onMapClick = {
                            scope.launch { drawerState.close() }
                            navigator.replaceAll(FullMapScreen())
                        },
                        onTrackingClick = {
                            scope.launch { drawerState.close() }
                        },
                        onPOIsClick = {
                            scope.launch { drawerState.close() }
                            navigator.replaceAll(POIsScreen())
                        },
                        onNotebookClick = {
                            scope.launch { drawerState.close() }
                            // TODO: Navigate to Notebook/Sessions screen
                        },
                        onSettingsClick = {
                            scope.launch { drawerState.close() }
                            navigator.replaceAll(SettingsScreen())
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                TrackingScreenContent(
                    uiState = uiState,
                    showMenuButton = true,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onBackClick = { navigator.pop() },
                    onMapReady = screenModel::onMapReady,
                    onMapReleased = screenModel::onMapReleased,
                    onStartTracking = onStartTracking,
                    onStartTrackingForced = { screenModel.startTrackingForced() },
                    onPauseTracking = { screenModel.pauseTracking() },
                    onResumeTracking = { screenModel.resumeTracking() },
                    onCancelConfirmation = { screenModel.cancelConfirmation() },
                    onStopTracking = { screenModel.stopTracking() },
                    onStartNewSession = onStartNewSession,
                    onClearError = { screenModel.clearError() }
                )
            }
        } else {
            TrackingScreenContent(
                uiState = uiState,
                showMenuButton = false,
                onMenuClick = {},
                onBackClick = { navigator.pop() },
                onMapReady = screenModel::onMapReady,
                onMapReleased = screenModel::onMapReleased,
                onStartTracking = onStartTracking,
                onStartTrackingForced = { screenModel.startTrackingForced() },
                onPauseTracking = { screenModel.pauseTracking() },
                onResumeTracking = { screenModel.resumeTracking() },
                onCancelConfirmation = { screenModel.cancelConfirmation() },
                onStopTracking = { screenModel.stopTracking() },
                onStartNewSession = onStartNewSession,
                onClearError = { screenModel.clearError() }
            )
        }
    }
}

private fun convertTrackingToRoutesUiState(strings: LocalizedStrings, currentLanguage: String): RoutesUiState {
    return RoutesUiState.Success(
        routes = emptyList(),
        currentLanguage = currentLanguage,
        strings = strings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingScreenContent(
    uiState: TrackingUiState,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onMapReady: (MapLayerController) -> Unit,
    onMapReleased: (MapLayerController) -> Unit,
    onStartTracking: () -> Unit,
    onStartTrackingForced: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    onCancelConfirmation: () -> Unit,
    onStopTracking: () -> Unit,
    onStartNewSession: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.tracking_title)) },
                navigationIcon = {
                    if (showMenuButton) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(Res.string.tracking_navigation_open_menu))
                        }
                    } else {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TrackingUiState.Idle -> {
                    IdleContent(
                        routes = uiState.routes,
                        selectedRoute = uiState.selectedRoute,
                        currentLocation = uiState.currentLocation,
                        onMapReady = onMapReady,
                        onMapReleased = onMapReleased,
                        onStartTracking = onStartTracking,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is TrackingUiState.AwaitingConfirmation -> {
                    // Show idle map with confirmation dialog on top
                    IdleContent(
                        routes = uiState.routes,
                        selectedRoute = uiState.selectedRoute,
                        currentLocation = uiState.currentLocation,
                        onMapReady = onMapReady,
                        onMapReleased = onMapReleased,
                        onStartTracking = onStartTracking,
                        modifier = Modifier.fillMaxSize()
                    )
                    ConfirmationDialog(
                        distanceKm = uiState.distanceFromRoute / 1000.0,
                        onConfirm = onStartTrackingForced,
                        onCancel = onCancelConfirmation
                    )
                }

                is TrackingUiState.Tracking -> {
                    ActiveTrackingContent(
                        routes = uiState.routes,
                        selectedRoute = uiState.selectedRoute,
                        sessionId = uiState.sessionId,
                        currentLocation = uiState.currentLocation,
                        trackPoints = uiState.trackPoints,
                        distanceMeters = uiState.distanceMeters,
                        isPaused = false,
                        onMapReady = onMapReady,
                        onMapReleased = onMapReleased,
                        onPauseOrResume = onPauseTracking,
                        onStopTracking = onStopTracking,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is TrackingUiState.Paused -> {
                    ActiveTrackingContent(
                        routes = uiState.routes,
                        selectedRoute = uiState.selectedRoute,
                        sessionId = uiState.sessionId,
                        currentLocation = uiState.currentLocation,
                        trackPoints = uiState.trackPoints,
                        distanceMeters = uiState.distanceMeters,
                        isPaused = true,
                        onMapReady = onMapReady,
                        onMapReleased = onMapReleased,
                        onPauseOrResume = onResumeTracking,
                        onStopTracking = onStopTracking,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is TrackingUiState.Completed -> {
                    CompletedContent(
                        session = uiState.session,
                        onNewSession = onStartNewSession,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                is TrackingUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        onRetry = onStartTracking,
                        onDismiss = onClearError,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(
    routes: List<Route>,
    selectedRoute: Route?,
    currentLocation: LocationData?,
    onMapReady: (MapLayerController) -> Unit,
    onMapReleased: (MapLayerController) -> Unit,
    onStartTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewportState = rememberMenorcaViewportState()
    BoxWithConstraints(modifier = Modifier.fillMaxSize().then(modifier)) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }.roundToInt().coerceAtLeast(1)
        val heightPx = with(density) { maxHeight.toPx() }.roundToInt().coerceAtLeast(1)
        val fallbackCamera = viewportState.updateSize(widthPx, heightPx)
        val useFallbackZoom = selectedRoute == null && currentLocation == null
        val cameraPosition = remember(routes, selectedRoute, currentLocation, fallbackCamera, useFallbackZoom) {
            calculateCameraPosition(
                routes = routes,
                selectedRoute = selectedRoute,
                location = currentLocation,
                fallbackCamera = fallbackCamera,
                useFallbackZoom = useFallbackZoom
            )
        }
        var mapController by remember { mutableStateOf<MapLayerController?>(null) }

        MapWithLayers(
            modifier = Modifier.fillMaxSize(),
            latitude = cameraPosition.latitude,
            longitude = cameraPosition.longitude,
            zoom = cameraPosition.zoom,
            styleUrl = MapStyles.LIBERTY,
            onMapReady = { controller ->
                mapController = controller
                onMapReady(controller)
                controller.updateCamera(
                    latitude = cameraPosition.latitude,
                    longitude = cameraPosition.longitude,
                    zoom = cameraPosition.zoom,
                    animated = false
                )
            }
        )

        // Start Tracking FAB
        ExtendedFloatingActionButton(
            onClick = onStartTracking,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Default.Flag, contentDescription = null) },
            text = { Text(stringResource(Res.string.tracking_start)) }
        )

        LaunchedEffect(mapController, cameraPosition) {
            val controller = mapController ?: return@LaunchedEffect
            controller.updateCamera(
                latitude = cameraPosition.latitude,
                longitude = cameraPosition.longitude,
                zoom = cameraPosition.zoom,
                animated = false
            )
        }

        DisposableEffect(mapController) {
            val controller = mapController
            onDispose {
                if (controller != null) {
                    onMapReleased(controller)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveTrackingContent(
    routes: List<Route>,
    selectedRoute: Route?,
    sessionId: String,
    currentLocation: LocationData?,
    trackPoints: List<TrackPoint>,
    distanceMeters: Double,
    isPaused: Boolean,
    onMapReady: (MapLayerController) -> Unit,
    onMapReleased: (MapLayerController) -> Unit,
    onPauseOrResume: () -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val viewportState = rememberMenorcaViewportState()
    BoxWithConstraints(modifier = Modifier.fillMaxSize().then(modifier)) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }.roundToInt().coerceAtLeast(1)
        val heightPx = with(density) { maxHeight.toPx() }.roundToInt().coerceAtLeast(1)
        val fallbackCamera = viewportState.updateSize(widthPx, heightPx)

        // GPS following state - enabled by default
        var followGpsLocation by remember { mutableStateOf(true) }
        var lastKnownPosition by remember { mutableStateOf<CameraPosition?>(null) }

        // Calculate camera position based on GPS following state
        val useFallbackZoom = selectedRoute == null && currentLocation == null && trackPoints.isEmpty()
        val cameraPosition = if (followGpsLocation) {
            calculateCameraPosition(
                routes = routes,
                selectedRoute = selectedRoute,
                location = currentLocation,
                fallbackCamera = fallbackCamera,
                useFallbackZoom = useFallbackZoom
            ).also {
                lastKnownPosition = it
            }
        } else {
            lastKnownPosition ?: calculateCameraPosition(
                routes = routes,
                selectedRoute = selectedRoute,
                location = currentLocation,
                fallbackCamera = fallbackCamera,
                useFallbackZoom = useFallbackZoom
            )
        }

        // Remember the map controller for dynamic updates
        var mapController by remember { mutableStateOf<MapLayerController?>(null) }
        val cameraState = rememberUpdatedState(cameraPosition)

        LaunchedEffect(mapController, followGpsLocation, cameraPosition) {
            val controller = mapController ?: return@LaunchedEffect
            if (followGpsLocation) {
                val position = cameraState.value
                controller.updateCamera(
                    latitude = position.latitude,
                    longitude = position.longitude,
                    zoom = null,
                    animated = true
                )
            }
        }

        val onMapReadyCallback = remember(onMapReady) {
            { controller: MapLayerController ->
                mapController = controller
                onMapReady(controller)
                val position = cameraState.value
                controller.updateCamera(
                    latitude = position.latitude,
                    longitude = position.longitude,
                    zoom = position.zoom,
                    animated = false
                )
            }
        }

        val onCameraMovedCallback = remember {
            {
                followGpsLocation = false
            }
        }

        DisposableEffect(mapController) {
            val controller = mapController
            onDispose {
                if (controller != null) {
                    onMapReleased(controller)
                }
            }
        }

        // Fullscreen map with route and user track
        MapWithLayers(
            modifier = Modifier.fillMaxSize(),
            latitude = cameraPosition.latitude,
            longitude = cameraPosition.longitude,
            zoom = cameraPosition.zoom,
            styleUrl = MapStyles.LIBERTY,
            onMapReady = onMapReadyCallback,
            onCameraMoved = onCameraMovedCallback
        )

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
                contentDescription = if (followGpsLocation) {
                    stringResource(Res.string.tracking_gps_follow_enabled)
                } else {
                    stringResource(Res.string.tracking_gps_follow_disabled)
                },
                tint = if (followGpsLocation) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // Floating controls anchored bottom end, with Stop button animating above Play/Pause when paused
        val controlsSpacing = 8.dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(controlsSpacing),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = isPaused,
                enter = scaleIn(animationSpec = tween(durationMillis = 1000), initialScale = 0.1f),
                exit = scaleOut(animationSpec = tween(durationMillis = 1000), targetScale = 0.1f)
            ) {
                FloatingActionButton(
                    onClick = onStopTracking,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = stringResource(Res.string.tracking_stop)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(controlsSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = stringResource(Res.string.tracking_show_statistics)
                    )
                }

                FloatingActionButton(
                    onClick = onPauseOrResume,
                    containerColor = if (isPaused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = stringResource(
                            if (isPaused) Res.string.tracking_resume else Res.string.tracking_pause
                        ),
                        tint = if (isPaused) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
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
                    text = stringResource(Res.string.tracking_statistics_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (currentLocation != null) {
                    LocationInfoRow(
                        stringResource(Res.string.tracking_latitude),
                        "${(currentLocation.latitude * 1000000).toInt() / 1000000.0}°"
                    )
                    LocationInfoRow(
                        stringResource(Res.string.tracking_longitude),
                        "${(currentLocation.longitude * 1000000).toInt() / 1000000.0}°"
                    )

                    currentLocation.altitude?.let {
                        LocationInfoRow(
                            stringResource(Res.string.tracking_altitude),
                            "${(it * 10).toInt() / 10.0} m"
                        )
                    }

                    currentLocation.accuracy?.let {
                        LocationInfoRow(
                            stringResource(Res.string.tracking_accuracy),
                            "±${(it * 10).toInt() / 10.0} m"
                        )
                    }

                    currentLocation.speed?.let {
                        val speedKmh = it * 3.6
                        LocationInfoRow(
                            stringResource(Res.string.tracking_speed),
                            "${(speedKmh * 10).toInt() / 10.0} km/h"
                        )
                    }

                    // Show distance traveled
                    LocationInfoRow(
                        stringResource(Res.string.tracking_distance),
                        "${((distanceMeters / 1000.0) * 100).toInt() / 100.0} km"
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.tracking_acquiring_signal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.tracking_session_prefix, sessionId.take(8)),
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
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize().then(modifier),
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
            text = stringResource(Res.string.tracking_completed),
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
                        text = stringResource(Res.string.tracking_session_summary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LocationInfoRow(
                        stringResource(Res.string.tracking_distance),
                        "${((session.distanceMeters / 1000.0) * 100).toInt() / 100.0} km"
                    )
                    LocationInfoRow(
                        stringResource(Res.string.tracking_duration),
                        "${session.durationSeconds / 3600}h ${(session.durationSeconds % 3600) / 60}m"
                    )
                    LocationInfoRow(
                        stringResource(Res.string.tracking_avg_speed),
                        "${(session.averageSpeedKmh * 10).toInt() / 10.0} km/h"
                    )
                    LocationInfoRow(
                        stringResource(Res.string.tracking_elevation_gain),
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
            Text(stringResource(Res.string.tracking_start_new_session))
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize().then(modifier),
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
            text = stringResource(Res.string.tracking_error),
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
                Text(stringResource(Res.string.cancel))
            }

            FilledTonalButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.retry))
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
            Text(stringResource(Res.string.tracking_far_title))
        },
        text = {
            val distanceFormatted = (distanceKm * 10).toInt() / 10.0
            Text(
                stringResource(Res.string.tracking_far_message, distanceFormatted)
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text(stringResource(Res.string.tracking_start_anyway))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
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
    routes: List<Route>,
    selectedRoute: Route?,
    location: LocationData?,
    fallbackCamera: MapCameraConfig,
    useFallbackZoom: Boolean = false
): CameraPosition {
    if (useFallbackZoom) {
        return CameraPosition(
            latitude = fallbackCamera.latitude,
            longitude = fallbackCamera.longitude,
            zoom = fallbackCamera.zoom
        )
    }

    // If we have current location, center on it
    location?.let {
        return CameraPosition(
            latitude = it.latitude,
            longitude = it.longitude,
            zoom = 14.0
        )
    }

    // Otherwise, if we have a selected route with data, center on it
    val primaryCoordinates = selectedRoute?.gpxData?.let { geoJson ->
        parseGeoJsonLineString(geoJson)
    }

    val coordinates = when {
        primaryCoordinates != null && primaryCoordinates.isNotEmpty() -> primaryCoordinates
        else -> routes.mapNotNull { route ->
            route.gpxData?.let { parseGeoJsonLineString(it) }
        }.flatten()
    }

    if (coordinates.isNotEmpty()) {
        val lats = coordinates.map { it.second }
        val lons = coordinates.map { it.first }
        val centerLat = (lats.minOrNull()!! + lats.maxOrNull()!!) / 2.0
        val centerLon = (lons.minOrNull()!! + lons.maxOrNull()!!) / 2.0
        val zoom = if (selectedRoute != null || routes.size == 1) 12.0 else 10.5
        return CameraPosition(centerLat, centerLon, zoom)
    }

    // Default: Menorca center
    return CameraPosition(fallbackCamera.latitude, fallbackCamera.longitude, fallbackCamera.zoom)
}
