package com.followmemobile.camidecavalls.presentation.notebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.map.rememberMenorcaViewportState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

/**
 * Session detail screen showing the recorded track on a map with altitude-based coloring.
 */
data class SessionDetailScreen(val sessionId: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel: SessionDetailScreenModel = koinInject { parametersOf(sessionId) }
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        // State for export share sheet
        var showExportSheet by remember { mutableStateOf(false) }
        var gpxContent by remember { mutableStateOf<String?>(null) }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.session?.name?.ifEmpty {
                                uiState.strings.notebookTitle
                            } ?: uiState.strings.notebookTitle,
                            maxLines = 1
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = uiState.strings.notebookCancel
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                gpxContent = screenModel.generateGpxContent()
                                if (gpxContent != null) {
                                    showExportSheet = true
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = uiState.strings.sessionExport
                            )
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
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.session != null -> {
                        SessionDetailContent(
                            session = uiState.session!!,
                            strings = uiState.strings,
                            isDetailsPanelExpanded = uiState.isDetailsPanelExpanded,
                            onMapReady = screenModel::onMapReady,
                            onTogglePanel = screenModel::toggleDetailsPanel
                        )
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // GPX Export Bottom Sheet
        if (showExportSheet && gpxContent != null) {
            GpxExportSheet(
                gpxContent = gpxContent!!,
                fileName = "${uiState.session?.name?.ifEmpty { "track" } ?: "track"}.gpx",
                strings = uiState.strings,
                onDismiss = { showExportSheet = false }
            )
        }
    }
}

@Composable
private fun SessionDetailContent(
    session: TrackingSession,
    strings: LocalizedStrings,
    isDetailsPanelExpanded: Boolean,
    onMapReady: (MapLayerController) -> Unit,
    onTogglePanel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        var mapController by remember { mutableStateOf<MapLayerController?>(null) }
        val viewportState = rememberMenorcaViewportState()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }.roundToInt().coerceAtLeast(1)
            val heightPx = with(density) { maxHeight.toPx() }.roundToInt().coerceAtLeast(1)
            val cameraConfig = viewportState.updateSize(widthPx, heightPx)

            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = cameraConfig.latitude,
                longitude = cameraConfig.longitude,
                zoom = cameraConfig.zoom,
                styleUrl = MapStyles.LIBERTY,
                onMapReady = { controller ->
                    mapController = controller
                    onMapReady(controller)
                }
            )
        }

        // Collapsible Details Panel at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Toggle button — taller handle with text at top, no gap to panel below
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable(onClick = onTogglePanel),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 40.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.sessionDetails,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        imageVector = if (isDetailsPanelExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Details panel
            AnimatedVisibility(
                visible = isDetailsPanelExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SessionDetailsPanel(
                    session = session,
                    strings = strings
                )
            }
        }

        // Altitude legend
        AltitudeLegend(
            strings = strings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SessionDetailsPanel(
    session: TrackingSession,
    strings: LocalizedStrings
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date and time
            Text(
                text = formatDateTime(session.startTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = strings.trackingDistance,
                    value = "${formatDistance(session.distanceMeters)} km"
                )
                StatCard(
                    label = strings.homeDuration,
                    value = formatDuration(session.durationSeconds)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = strings.routeDetailElevationGain,
                    value = "+${session.elevationGainMeters} m"
                )
                StatCard(
                    label = strings.routeDetailElevationLoss,
                    value = "-${session.elevationLossMeters} m"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = strings.sessionAvgSpeed,
                    value = "${formatSpeed(session.averageSpeedKmh)} km/h"
                )
                StatCard(
                    label = strings.sessionMaxSpeed,
                    value = "${formatSpeed(session.maxSpeedKmh)} km/h"
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AltitudeLegend(
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.sessionAltitude,
                style = MaterialTheme.typography.labelSmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Green box
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Text(strings.elevationChartMin, style = MaterialTheme.typography.labelSmall)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yellow box
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color(0xFFFFEB3B),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Text(strings.elevationChartMid, style = MaterialTheme.typography.labelSmall)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red box
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color(0xFFF44336),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Text(strings.elevationChartMax, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GpxExportSheet(
    gpxContent: String,
    fileName: String,
    strings: LocalizedStrings,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.sessionExport,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = strings.sessionExportMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Platform-specific share will be handled by actual implementation
            ShareGpxButton(
                gpxContent = gpxContent,
                fileName = fileName,
                strings = strings
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
expect fun ShareGpxButton(
    gpxContent: String,
    fileName: String,
    strings: LocalizedStrings
)

private fun formatDateTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.year}"
    val time = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    return "$date $time"
}

private fun formatDistance(meters: Double): String {
    val km = meters / 1000.0
    return ((km * 100).toInt() / 100.0).toString()
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

private fun formatSpeed(speedKmh: Double): String {
    return ((speedKmh * 10).toInt() / 10.0).toString()
}
