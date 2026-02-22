package com.followmemobile.camidecavalls.presentation.detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.SubcomposeAsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.presentation.main.RouteSelectionManager
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.components.ElevationChart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import camidecavalls.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Route Detail screen showing detailed information about a specific trail stage.
 */
data class RouteDetailScreen(val routeId: Int) : Screen {

    @Composable
    override fun Content() {
        val screenModel: RouteDetailScreenModel = koinInject { parametersOf(routeId) }
        val routeSelectionManager: RouteSelectionManager = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        val onStartTracking: (Route) -> Unit = { route ->
            routeSelectionManager.selectRoute(route.id)
            navigator.popUntilRoot()
        }

        when (val state = uiState) {
            is RouteDetailUiState.Success -> {
                RouteDetailScreenContent(
                    uiState = state,
                    strings = state.strings,
                    onBackClick = { navigator.pop() },
                    onStartTracking = onStartTracking
                )
            }
            else -> {
                RouteDetailScreenContent(
                    uiState = uiState,
                    strings = null,
                    onBackClick = { navigator.pop() },
                    onStartTracking = onStartTracking
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
    // Get route name for collapsing toolbar
    val routeName = if (uiState is RouteDetailUiState.Success) {
        uiState.route.name
    } else {
        ""
    }

    // Track scroll position for collapsing title
    var titleAlpha by remember { mutableStateOf(0f) }

    // Interpolate toolbar color based on scroll
    val defaultToolbarColor = MaterialTheme.colorScheme.primaryContainer
    val scrolledToolbarColor = Color(0xFF80DEEA) // Light cyan/turquoise to match beach/sea images
    val toolbarColor = lerp(defaultToolbarColor, scrolledToolbarColor, titleAlpha)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        // Default title (visible when not scrolled)
                        Text(
                            text = strings?.routeViewDetails ?: "",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 1f - titleAlpha)
                        )
                        // Route name title (visible when scrolled)
                        Text(
                            text = routeName,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = titleAlpha)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings?.back ?: "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = toolbarColor,
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
                        imageVector = Icons.Default.Flag,
                        contentDescription = strings?.trackingStart ?: ""
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
                        strings = strings!!,
                        onTitleAlphaChange = { alpha -> titleAlpha = alpha }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun RouteDetailContent(
    route: Route,
    currentLanguage: String,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings,
    onTitleAlphaChange: (Float) -> Unit
) {
    // Track selected point from elevation chart
    var selectedChartPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Load route image (hero_cami.jpg for complete route, route image otherwise)
    val routeImageBytes by produceState<ByteArray?>(initialValue = null) {
        value = try {
            if (route.id == 0) {
                Res.readBytes("files/images/hero_cami.jpg")
            } else {
                Res.readBytes("files/images/routes/route_${route.id}.jpg")
            }
        } catch (e: Exception) {
            null
        }
    }

    // Track scroll for collapsing toolbar
    val scrollState = rememberScrollState()

    // Get image height in pixels
    val density = LocalDensity.current
    val imageHeightPx = with(density) { 250.dp.toPx() }

    // Update title alpha based on scroll position
    LaunchedEffect(scrollState.value) {
        // Hero image is 250dp tall
        // Title transition occurs as image scrolls out (0px = alpha 0, imageHeightPx = alpha 1)
        val alpha = (scrollState.value / imageHeightPx).coerceIn(0f, 1f)
        onTitleAlphaChange(alpha)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero image with route header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Background image
            routeImageBytes?.let { imageBytes ->
                SubcomposeAsyncImage(
                    model = imageBytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Gradient overlay + header text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.0f),
                            0.5f to Color.Black.copy(alpha = 0.0f),
                            1.0f to Color.Black.copy(alpha = 0.8f)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    if (route.id == 0) {
                        Text(
                            text = strings.completeRouteName.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.completeRouteSubtitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = strings.routeStage(route.number).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = route.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map preview
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            RouteMapPreview(
                route = route,
                selectedPoint = selectedChartPoint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Elevation Profile Chart (moved here - below the map)
        Text(
            text = strings.routeDetailElevationProfile,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (route.gpxData != null) {
            ElevationChart(
                gpxData = route.gpxData,
                strings = strings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onPointSelected = { coordinates ->
                    selectedChartPoint = coordinates
                }
            )
        } else {
            Text(
                text = strings.routeDetailNoElevationData,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Info
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = strings.routeDetailDescription,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = route.getLocalizedDescription(currentLanguage),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
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
private fun RouteMapPreview(
    route: Route,
    selectedPoint: Pair<Double, Double>? = null
) {
    // Parse route coordinates if GPX data is available
    val routeCoordinates = route.gpxData?.let { parseGeoJsonLineString(it) } ?: emptyList()

    // Store map controller to update marker without recreating map
    var mapController by remember { mutableStateOf<com.followmemobile.camidecavalls.presentation.map.MapLayerController?>(null) }

    // Update marker when selected point changes, without recreating entire map
    LaunchedEffect(selectedPoint) {
        mapController?.let { controller ->
            // Remove old marker layer if exists
            controller.removeLayer("selected-point-${route.id}")

            // Add new marker if point is selected
            selectedPoint?.let { point ->
                controller.addMarker(
                    markerId = "selected-point-${route.id}",
                    latitude = point.second,
                    longitude = point.first,
                    color = "#FFEB3B",
                    radius = 5f  // Smaller radius
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Map dimensions
        val mapHeight = 250.dp
        val mapWidth = maxWidth

        // Get density for conversion
        val density = LocalDensity.current

        // Calculate map aspect ratio in Dp (this is dimensionless)
        val mapAspectRatio = with(density) {
            maxWidth.toPx() / mapHeight.toPx()
        }

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

            // Calculate deltas in degrees (route extent)
            val latDelta = maxLat - minLat
            val lonDelta = maxLon - minLon

            // Add padding factor to ensure route doesn't touch edges
            // Use more padding for latitude (vertical) since map is wider than tall
            val latPaddingFactor = 2.5
            val lonPaddingFactor = 2.0

            // Apply padding
            val paddedLatDelta = latDelta * latPaddingFactor
            val paddedLonDelta = lonDelta * lonPaddingFactor

            // The key insight: we need to compare deltas accounting for map dimensions
            // Normalize both deltas to a "vertical equivalent" scale
            // - latDelta directly maps to map height
            // - lonDelta maps to map width, so we need to adjust it by aspect ratio
            //   (if map is 2x wider than tall, the same lonDelta needs half the zoom)

            val effectiveLatDelta = paddedLatDelta
            val effectiveLonDelta = paddedLonDelta / mapAspectRatio

            // Use the larger effective delta (the dimension that needs more zoom out)
            val limitingDelta = max(effectiveLatDelta, effectiveLonDelta)

            // Calculate zoom: smaller delta = higher zoom
            val calculatedZoom = if (limitingDelta > 0) {
                val baseZoom = ln(360.0 / limitingDelta) / ln(2.0)
                min(15.0, max(8.0, baseZoom - 0.3))
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
                // Save controller for updating markers
                mapController = controller
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
