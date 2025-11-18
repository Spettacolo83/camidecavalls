package com.followmemobile.camidecavalls.presentation.fullmap

import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.home.DrawerContent
import com.followmemobile.camidecavalls.presentation.home.DrawerScreen
import com.followmemobile.camidecavalls.presentation.home.RoutesScreen
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.map.MenorcaViewportCalculator
import com.followmemobile.camidecavalls.presentation.pois.POIsScreen
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreen
import com.followmemobile.camidecavalls.presentation.tracking.TrackingScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * Full Map Screen showing all 20 routes of Camí de Cavalls overlayed on the map.
 *
 * Features:
 * - All 20 routes displayed simultaneously with simplified GPS data for performance
 * - Click on route to highlight and show popup
 * - Future: POI markers with interactions
 */
class FullMapScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: FullMapScreenModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = convertToRoutesUiState(uiState),
                    currentScreen = DrawerScreen.MAP,
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
                    },
                    onTrackingClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(TrackingScreen())
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
            FullMapScreenContent(
                uiState = uiState,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onRouteClick = { routeId -> screenModel.selectRoute(routeId) },
                onMapReady = { controller -> screenModel.onMapReady(controller) }
            )
        }
    }
}

// Helper to convert FullMapUiState to RoutesUiState for drawer
private fun convertToRoutesUiState(fullMapUiState: FullMapUiState): com.followmemobile.camidecavalls.presentation.home.RoutesUiState {
    return com.followmemobile.camidecavalls.presentation.home.RoutesUiState.Success(
        routes = emptyList(),
        currentLanguage = "en",
        strings = fullMapUiState.strings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullMapScreenContent(
    uiState: FullMapUiState,
    onMenuClick: () -> Unit,
    onRouteClick: (Int) -> Unit,
    onMapReady: (MapLayerController) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.strings.mapTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
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
            var mapController by remember { mutableStateOf<MapLayerController?>(null) }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }.roundToInt().coerceAtLeast(1)
                val heightPx = with(density) { maxHeight.toPx() }.roundToInt().coerceAtLeast(1)
                val cameraConfig = remember(widthPx, heightPx) {
                    MenorcaViewportCalculator.calculateForSize(widthPx, heightPx)
                }

                MapWithLayers(
                    modifier = Modifier.fillMaxSize(),
                    latitude = cameraConfig.latitude,
                    longitude = cameraConfig.longitude,
                    zoom = cameraConfig.zoom,
                    styleUrl = MapStyles.LIBERTY,
                    onMapReady = { controller ->
                        mapController = controller
                        onMapReady(controller)
                        controller.updateCamera(
                            latitude = cameraConfig.latitude,
                            longitude = cameraConfig.longitude,
                            zoom = cameraConfig.zoom,
                            animated = false
                        )
                    }
                )

                LaunchedEffect(mapController, cameraConfig) {
                    mapController?.updateCamera(
                        latitude = cameraConfig.latitude,
                        longitude = cameraConfig.longitude,
                        zoom = cameraConfig.zoom,
                        animated = false
                    )
                }
            }

            // Loading indicator while routes are being loaded
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message if loading failed
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                ) {
                    Text(uiState.error)
                }
            }

            // TODO: Route selection popup (future enhancement)
            // TODO: POI markers (future enhancement)
        }
    }
}
