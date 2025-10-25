package com.followmemobile.camidecavalls.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.dellisd.spatialk.geojson.Position
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

/**
 * Test screen for MapLibre integration.
 * Displays a simple map with OpenFreeMap tiles.
 */
class MapScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        MapScreenContent(
            onBackClick = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScreenContent(
    onBackClick: () -> Unit
) {
    // Initial camera position centered on Menorca island
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(4.05, 39.95), // Menorca coordinates (longitude, latitude)
            zoom = 9.2
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camí de Cavalls") },
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
            MaplibreMap(
                cameraState = cameraState,
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
