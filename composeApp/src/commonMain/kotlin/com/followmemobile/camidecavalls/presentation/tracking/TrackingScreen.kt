package com.followmemobile.camidecavalls.presentation.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.TrackPoint
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
                    IdleContent(onStartTracking = onStartTracking)
                }

                is TrackingUiState.AwaitingConfirmation -> {
                    ConfirmationDialog(
                        distanceKm = uiState.distanceFromRoute / 1000.0,
                        onConfirm = onStartTrackingForced,
                        onCancel = onCancelConfirmation
                    )
                }

                is TrackingUiState.Tracking -> {
                    TrackingContent(
                        sessionId = uiState.sessionId,
                        location = uiState.currentLocation,
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
private fun IdleContent(onStartTracking: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready to Track",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start tracking your hike on the Camí de Cavalls",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onStartTracking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Tracking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Battery Optimized",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Updates every 5 seconds\n• Works completely offline\n• GPS-only tracking",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TrackingContent(
    sessionId: String,
    location: LocationData?,
    onStopTracking: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tracking Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Location data
        if (location != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current Position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LocationInfoRow("Latitude", "${(location.latitude * 1000000).toInt() / 1000000.0}°")
                    LocationInfoRow("Longitude", "${(location.longitude * 1000000).toInt() / 1000000.0}°")

                    location.altitude?.let {
                        LocationInfoRow("Altitude", "${(it * 10).toInt() / 10.0} m")
                    }

                    location.accuracy?.let {
                        LocationInfoRow("Accuracy", "±${(it * 10).toInt() / 10.0} m")
                    }

                    location.speed?.let {
                        val speedKmh = it * 3.6 // Convert m/s to km/h
                        LocationInfoRow("Speed", "${(speedKmh * 10).toInt() / 10.0} km/h")
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Acquiring GPS signal...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Stop button
        Button(
            onClick = onStopTracking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Tracking")
        }

        Text(
            text = "Session ID: ${sessionId.take(8)}...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
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
