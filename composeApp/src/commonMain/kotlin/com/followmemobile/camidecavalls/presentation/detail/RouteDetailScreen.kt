package com.followmemobile.camidecavalls.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.dp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
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
import androidx.compose.runtime.key
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
import camidecavalls.composeapp.generated.resources.Res
import camidecavalls.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Route Detail screen showing detailed information about a specific trail stage.
 */
data class RouteDetailScreen(val routeId: Int) : Screen {

    @Composable
    override fun Content() {
        val screenModel: RouteDetailScreenModel = koinInject { parametersOf(routeId) }
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        when (val state = uiState) {
            is RouteDetailUiState.Success -> {
                RouteDetailScreenContent(
                    uiState = state,
                    strings = state.strings,
                    onBackClick = { navigator.pop() },
                    onStartTracking = { route ->
                        navigator.push(TrackingScreen(routeId = route.id))
                    }
                )
            }
            else -> {
                RouteDetailScreenContent(
                    uiState = uiState,
                    strings = null,
                    onBackClick = { navigator.pop() },
                    onStartTracking = { route ->
                        navigator.push(TrackingScreen(routeId = route.id))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailScreenContent(
    uiState: RouteDetailUiState,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings?,
    onBackClick: () -> Unit,
    onStartTracking: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(strings?.routeViewDetails ?: stringResource(Res.string.route_view_details))
                },
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
                    RouteDetailContent(
                        route = uiState.route,
                        currentLanguage = uiState.currentLanguage,
                        strings = strings!!
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteDetailContent(
    route: Route,
    currentLanguage: String,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = strings.routeStage(route.number),
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
        InfoRow(label = strings.startPoint, value = route.startPoint)
        InfoRow(label = strings.endPoint, value = route.endPoint)
        InfoRow(label = strings.trackingDistance, value = strings.routeDistance(route.distanceKm.toString()))
        InfoRow(
            label = strings.routeDifficulty,
            value = when (route.difficulty) {
                com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> strings.difficultyLow
                com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> strings.difficultyMedium
                com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> strings.difficultyHigh
            }
        )
        InfoRow(label = strings.routeDetailElevationGain, value = strings.routeDetailMeters(route.elevationGainMeters))
        InfoRow(label = strings.routeDetailElevationLoss, value = strings.routeDetailMeters(route.elevationLossMeters))
        InfoRow(label = strings.routeDetailMaxAltitude, value = strings.routeDetailMeters(route.maxAltitudeMeters))
        InfoRow(label = strings.routeDetailMinAltitude, value = strings.routeDetailMeters(route.minAltitudeMeters))
        InfoRow(label = strings.routeDetailAsphalt, value = strings.routeDetailPercent(route.asphaltPercentage))
        InfoRow(
            label = strings.routeDetailEstimatedTime,
            value = if (route.estimatedDurationMinutes >= 60) {
                strings.routeDetailHours(route.estimatedDurationMinutes / 60.0)
            } else {
                strings.routeDetailMinutes(route.estimatedDurationMinutes)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = strings.routeDetailDescription,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = route.getLocalizedDescription(currentLanguage),
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Calculate map aspect ratio from available space
        val mapHeight = 350.dp
        val mapWidthPx = maxWidth.value
        val mapHeightPx = mapHeight.value
        val mapAspectRatio = mapWidthPx / mapHeightPx // width / height

        // Calculate camera position and zoom based on route coordinates
        val (centerLat, centerLon, zoom) = if (routeCoordinates.isNotEmpty()) {
            // Calculate bounding box
            val lats = routeCoordinates.map { it.second }
            val lons = routeCoordinates.map { it.first }

            val minLat = lats.minOrNull() ?: 0.0
            val maxLat = lats.maxOrNull() ?: 0.0
            val minLon = lons.minOrNull() ?: 0.0
            val maxLon = lons.maxOrNull() ?: 0.0

            // Calculate center
            val centerLat = (minLat + maxLat) / 2.0
            val centerLon = (minLon + maxLon) / 2.0

            // Calculate zoom level to fit entire route
            // Add padding factor to ensure route doesn't touch edges
            val paddingFactor = 1.4

            // Calculate deltas in degrees
            val latDelta = (maxLat - minLat) * paddingFactor
            val lonDelta = (maxLon - minLon) * paddingFactor

            // Adjust longitude delta by map aspect ratio
            // If map is wider than tall, we can fit more longitude degrees
            val adjustedLonDelta = lonDelta / mapAspectRatio

            // Use the larger delta to calculate zoom
            val maxDelta = max(latDelta, adjustedLonDelta)

            // Calculate zoom: smaller delta = higher zoom
            // Formula: zoom ≈ log2(360 / delta) - rough approximation
            val calculatedZoom = if (maxDelta > 0) {
                val baseZoom = ln(360.0 / maxDelta) / ln(2.0)
                min(15.0, max(8.0, baseZoom - 0.8)) // Clamp between 8 and 15, subtract 0.8 for more margin
            } else {
                12.0
            }

            Triple(centerLat, centerLon, calculatedZoom)
        } else {
            Triple(39.95, 4.05, 10.5)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
                .clip(RoundedCornerShape(12.dp))
        ) {
            key("route-detail-map-${route.id}") {
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
                }
            }
        )
            }  // key
        }  // Box
    }  // Column
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
