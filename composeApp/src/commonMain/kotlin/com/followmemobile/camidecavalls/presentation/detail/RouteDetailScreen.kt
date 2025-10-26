package com.followmemobile.camidecavalls.presentation.detail

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.tracking.TrackingScreen
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Route Detail screen showing detailed information about a specific trail stage.
 */
data class RouteDetailScreen(val routeId: Int) : Screen {

    @Composable
    override fun Content() {
        val screenModel: RouteDetailScreenModel = koinInject { parametersOf(routeId) }
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        RouteDetailScreenContent(
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onStartTracking = { route ->
                navigator.push(TrackingScreen(routeId = route.id))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailScreenContent(
    uiState: RouteDetailUiState,
    onBackClick: () -> Unit,
    onStartTracking: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState is RouteDetailUiState.Success) {
                FloatingActionButton(
                    onClick = { onStartTracking(uiState.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Tracking"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is RouteDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is RouteDetailUiState.Error -> {
                    Text(
                        text = uiState.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is RouteDetailUiState.Success -> {
                    RouteDetailContent(route = uiState.route)
                }
            }
        }
    }
}

@Composable
private fun RouteDetailContent(route: Route) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Stage ${route.number}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = route.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Map preview
        RouteMapPreview(route = route)

        Spacer(modifier = Modifier.height(16.dp))

        // Route Info
        InfoRow(label = "Start Point", value = route.startPoint)
        InfoRow(label = "End Point", value = route.endPoint)
        InfoRow(label = "Distance", value = "${route.distanceKm} km")
        InfoRow(label = "Difficulty", value = route.difficulty.name)
        InfoRow(label = "Elevation Gain", value = "+${route.elevationGainMeters}m")
        InfoRow(label = "Elevation Loss", value = "-${route.elevationLossMeters}m")
        InfoRow(label = "Max Altitude", value = "${route.maxAltitudeMeters}m")
        InfoRow(label = "Min Altitude", value = "${route.minAltitudeMeters}m")
        InfoRow(label = "Asphalt", value = "${route.asphaltPercentage}%")
        InfoRow(
            label = "Est. Duration",
            value = "${route.estimatedDurationMinutes / 60}h ${route.estimatedDurationMinutes % 60}m"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = route.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
private fun RouteMapPreview(route: Route) {
    // Parse route coordinates if GPX data is available
    val routeCoordinates = route.gpxData?.let { parseGeoJsonLineString(it) } ?: emptyList()

    // Calculate camera position based on route coordinates or use Menorca center
    val (centerLat, centerLon, zoom) = if (routeCoordinates.isNotEmpty()) {
        val lat = routeCoordinates.map { it.second }.average()
        val lon = routeCoordinates.map { it.first }.average()
        Triple(lat, lon, 12.0)
    } else {
        Triple(39.95, 4.05, 10.5)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                // Consume all drag gestures to prevent scroll conflict with parent
                detectDragGestures { _, _ -> }
            }
    ) {
        MapWithLayers(
            modifier = Modifier.fillMaxSize(),
            latitude = centerLat,
            longitude = centerLon,
            zoom = zoom,
            styleUrl = "https://tiles.openfreemap.org/styles/liberty",
            onMapReady = { controller ->
                // Add route path if GPX data is available
                if (route.gpxData != null && routeCoordinates.isNotEmpty()) {
                    // Add route path with blue color
                    controller.addRoutePath(
                        routeId = "route-${route.id}",
                        geoJsonLineString = route.gpxData,
                        color = "#2196F3",
                        width = 4f
                    )

                    // Add start marker (green)
                    val startPoint = routeCoordinates.first()
                    controller.addMarker(
                        markerId = "start-${route.id}",
                        latitude = startPoint.second,
                        longitude = startPoint.first,
                        color = "#4CAF50",
                        radius = 6f
                    )

                    // Add end marker (red)
                    val endPoint = routeCoordinates.last()
                    controller.addMarker(
                        markerId = "end-${route.id}",
                        latitude = endPoint.second,
                        longitude = endPoint.first,
                        color = "#F44336",
                        radius = 6f
                    )

                    // Add POI marker at midpoint (orange) for routes with GPX data
                    if (route.id <= 3) {
                        val midIndex = routeCoordinates.size / 2
                        val midPoint = routeCoordinates[midIndex]
                        controller.addMarker(
                            markerId = "poi-${route.id}",
                            latitude = midPoint.second,
                            longitude = midPoint.first,
                            color = "#FF9800",
                            radius = 5f
                        )
                    }
                }
            }
        )
    }
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
