package com.followmemobile.camidecavalls.presentation.fullmap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import org.koin.compose.koinInject

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

        FullMapScreenContent(
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onRouteClick = { routeId -> screenModel.selectRoute(routeId) },
            onMapReady = { controller -> screenModel.onMapReady(controller) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullMapScreenContent(
    uiState: FullMapUiState,
    onBackClick: () -> Unit,
    onRouteClick: (Int) -> Unit,
    onMapReady: (MapLayerController) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camí de Cavalls - Mappa Completa") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // MapLibre map with all 20 routes
            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = 39.95,  // Menorca center
                longitude = 4.05,
                zoom = 10.0,
                styleUrl = MapStyles.LIBERTY,
                onMapReady = { controller ->
                    onMapReady(controller)
                }
            )

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
