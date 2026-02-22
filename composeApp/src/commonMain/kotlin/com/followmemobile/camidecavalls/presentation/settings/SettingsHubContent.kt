package com.followmemobile.camidecavalls.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.icons.CamiDeCavallsIcon

/**
 * Settings hub content for the SETTINGS tab.
 * Shows language, about, and contact options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubContent(
    strings: LocalizedStrings,
    onLanguageClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Language option
                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Language, contentDescription = null) },
                    label = strings.settingsLanguageOption,
                    onClick = onLanguageClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // About option
                SettingsOptionRow(
                    icon = { Icon(CamiDeCavallsIcon, contentDescription = null) },
                    label = strings.settingsAboutOption,
                    onClick = onAboutClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Contact us option (disabled for now)
                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Email, contentDescription = null) },
                    label = strings.settingsContactUs,
                    onClick = { /* no-op for now */ },
                    enabled = false
                )
            }

            // App info at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camí de Cavalls\nv1.0.1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
    }
}

@Composable
private fun SettingsOptionRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val alpha = if (enabled) 1f else 0.4f
        Box(modifier = Modifier.size(24.dp)) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                    MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            ) {
                icon()
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
    }
}
