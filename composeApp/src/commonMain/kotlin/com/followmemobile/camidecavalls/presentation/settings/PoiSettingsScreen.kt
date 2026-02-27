package com.followmemobile.camidecavalls.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.tracking.PoiProximityManager
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

/**
 * POI Settings screen - pushed from Settings hub.
 * Allows configuring notification radius and enable/disable POI notifications.
 */
class PoiSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val settings: Settings = koinInject()
        val languageRepository: LanguageRepository = koinInject()
        val language by languageRepository.observeCurrentLanguage()
            .collectAsState(initial = languageRepository.getSystemLanguage())
        val strings = LocalizedStrings(language)

        var notificationsEnabled by remember {
            mutableStateOf(
                settings.getBoolean(PoiProximityManager.SETTINGS_KEY_NOTIFICATIONS_ENABLED, true)
            )
        }
        var selectedRadius by remember {
            mutableStateOf(
                settings.getInt(PoiProximityManager.SETTINGS_KEY_NOTIFICATION_RADIUS, PoiProximityManager.DEFAULT_NOTIFICATION_RADIUS.toInt())
            )
        }
        var maxVisiblePois by remember {
            mutableStateOf(
                settings.getInt(PoiProximityManager.SETTINGS_KEY_MAX_VISIBLE_POIS, PoiProximityManager.DEFAULT_MAX_VISIBLE_POIS)
            )
        }

        PoiSettingsContent(
            strings = strings,
            notificationsEnabled = notificationsEnabled,
            selectedRadius = selectedRadius,
            maxVisiblePois = maxVisiblePois,
            onBackClick = { navigator.pop() },
            onNotificationsEnabledChange = { enabled ->
                notificationsEnabled = enabled
                settings.putBoolean(PoiProximityManager.SETTINGS_KEY_NOTIFICATIONS_ENABLED, enabled)
            },
            onRadiusSelected = { radius ->
                selectedRadius = radius
                settings.putInt(PoiProximityManager.SETTINGS_KEY_NOTIFICATION_RADIUS, radius)
            },
            onMaxVisiblePoisChange = { value ->
                maxVisiblePois = value
                settings.putInt(PoiProximityManager.SETTINGS_KEY_MAX_VISIBLE_POIS, value)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoiSettingsContent(
    strings: LocalizedStrings,
    notificationsEnabled: Boolean,
    selectedRadius: Int,
    maxVisiblePois: Int,
    onBackClick: () -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onRadiusSelected: (Int) -> Unit,
    onMaxVisiblePoisChange: (Int) -> Unit
) {
    val radiusOptions = listOf(200, 300, 500, 1000)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsPoiOption) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable/disable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.settingsPoiNotificationsEnabled,
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsEnabledChange
                )
            }

            HorizontalDivider()

            // Notification radius section
            Text(
                text = strings.settingsPoiNotificationRadius,
                style = MaterialTheme.typography.titleMedium,
                color = if (notificationsEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            radiusOptions.forEach { radius ->
                RadiusOptionItem(
                    radius = radius,
                    isSelected = selectedRadius == radius,
                    enabled = notificationsEnabled,
                    onSelect = { onRadiusSelected(radius) }
                )
            }

            HorizontalDivider()

            // Max visible POIs slider
            Text(
                text = "${strings.settingsPoiMaxVisible}: $maxVisiblePois",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Slider(
                value = maxVisiblePois.toFloat(),
                onValueChange = { onMaxVisiblePoisChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RadiusOptionItem(
    radius: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val label = if (radius >= 1000) "${radius / 1000} km" else "$radius m"
    val alpha = if (enabled) 1f else 0.4f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected && enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected && enabled) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onSelect)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected && enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                }
            )

            if (isSelected && enabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
