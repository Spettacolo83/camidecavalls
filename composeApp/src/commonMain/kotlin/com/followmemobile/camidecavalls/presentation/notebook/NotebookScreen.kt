package com.followmemobile.camidecavalls.presentation.notebook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * Public composable for the NOTEBOOK tab in the bottom navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookTabContent(
    onSessionClick: (String) -> Unit,
    onSwitchToMapTab: () -> Unit
) {
    val screenModel: NotebookScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()

    NotebookScreenContent(
        uiState = uiState,
        onSessionClick = { session -> onSessionClick(session.id) },
        onDeleteClick = { session -> screenModel.showDeleteConfirmation(session) },
        onStartTracking = onSwitchToMapTab
    )

    // Delete confirmation dialog
    uiState.sessionToDelete?.let { session ->
        DeleteConfirmationDialog(
            strings = uiState.strings,
            sessionName = session.name.ifEmpty { formatDate(session.startTime) },
            onConfirm = { screenModel.confirmDelete() },
            onDismiss = { screenModel.dismissDeleteConfirmation() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotebookScreenContent(
    uiState: NotebookUiState,
    onSessionClick: (TrackingSession) -> Unit,
    onDeleteClick: (TrackingSession) -> Unit,
    onStartTracking: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.sessions.isEmpty() -> {
                    EmptyState(
                        strings = uiState.strings,
                        onStartTracking = onStartTracking,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.sessions, key = { it.id }) { session ->
                            SessionCard(
                                session = session,
                                strings = uiState.strings,
                                onClick = { onSessionClick(session) },
                                onDeleteClick = { onDeleteClick(session) }
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
    }
}

@Composable
private fun EmptyState(
    strings: LocalizedStrings,
    onStartTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsWalk,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Text(
            text = strings.notebookNoSessions,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
            onClick = onStartTracking,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(strings.notebookStartTracking)
        }
    }
}

@Composable
private fun SessionCard(
    session: TrackingSession,
    strings: LocalizedStrings,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.name.ifEmpty { formatDate(session.startTime) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatDateTime(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatItem(
                        label = strings.trackingDistance,
                        value = "${formatDistance(session.distanceMeters)} km"
                    )
                    StatItem(
                        label = strings.homeDuration,
                        value = formatDuration(session.durationSeconds)
                    )
                    if (session.elevationGainMeters > 0) {
                        StatItem(
                            label = strings.homeElevation,
                            value = "+${session.elevationGainMeters} m"
                        )
                    }
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = strings.notebookDeleteConfirm,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
private fun DeleteConfirmationDialog(
    strings: LocalizedStrings,
    sessionName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.notebookDeleteTitle) },
        text = { Text(strings.notebookDeleteMessage) },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(strings.notebookDeleteConfirm)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.notebookCancel)
            }
        }
    )
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDate.dayOfMonth.toString().padStart(2, '0')}/${localDate.monthNumber.toString().padStart(2, '0')}/${localDate.year}"
}

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
