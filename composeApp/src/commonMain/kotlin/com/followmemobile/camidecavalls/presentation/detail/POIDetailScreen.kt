package com.followmemobile.camidecavalls.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.PointOfInterest

/**
 * POI Detail content showing complete information about a Point of Interest.
 *
 * Features:
 * - Full image display
 * - Complete description
 * - Action button: custom URL if available, else open in Maps
 * - Navigation button to open in external map app
 * - Pastel background color based on POI type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIDetailContent(
    poi: PointOfInterest,
    currentLanguage: Language,
    onBackClick: () -> Unit,
    onNavigateClick: (PointOfInterest) -> Unit
) {
    // Get background color based on POI type
    val backgroundColor = when (poi.type.name) {
        "BEACH" -> Color(0xFFE6F5FF)         // Very light blue
        "NATURAL" -> Color(0xFFEAF7EA)       // Very light green
        "HISTORIC" -> Color(0xFFFFE6E6)      // Very light red/pink
        "COMMERCIAL" -> Color(0xFFFFF3E0)    // Very light orange
        "DANGER" -> Color(0xFFFFEBEE)        // Very light red
        else -> Color(0xFFF5F5F5)
    }

    // Badge text translations
    val badgeText = when (poi.type.name) {
        "BEACH" -> when (currentLanguage) {
            Language.CATALAN -> "🏖️ Zona Costanera"
            Language.SPANISH -> "🏖️ Zona Costera"
            Language.ENGLISH -> "🏖️ Coastal Zone"
            Language.FRENCH -> "🏖️ Zone Côtière"
            Language.GERMAN -> "🏖️ Küstenzone"
            Language.ITALIAN -> "🏖️ Zona Costiera"
        }
        "NATURAL" -> when (currentLanguage) {
            Language.CATALAN -> "🌿 Espai Natural"
            Language.SPANISH -> "🌿 Espacio Natural"
            Language.ENGLISH -> "🌿 Natural Space"
            Language.FRENCH -> "🌿 Espace Naturel"
            Language.GERMAN -> "🌿 Naturraum"
            Language.ITALIAN -> "🌿 Spazio Naturale"
        }
        "HISTORIC" -> when (currentLanguage) {
            Language.CATALAN -> "🏛️ Patrimoni"
            Language.SPANISH -> "🏛️ Patrimonio"
            Language.ENGLISH -> "🏛️ Heritage"
            Language.FRENCH -> "🏛️ Patrimoine"
            Language.GERMAN -> "🏛️ Erbe"
            Language.ITALIAN -> "🏛️ Patrimonio"
        }
        "COMMERCIAL" -> when (currentLanguage) {
            Language.CATALAN -> "🏪 Comercial"
            Language.SPANISH -> "🏪 Comercial"
            Language.ENGLISH -> "🏪 Commercial"
            Language.FRENCH -> "🏪 Commercial"
            Language.GERMAN -> "🏪 Kommerziell"
            Language.ITALIAN -> "🏪 Commerciale"
        }
        "DANGER" -> when (currentLanguage) {
            Language.CATALAN -> "⚠️ Perill"
            Language.SPANISH -> "⚠️ Peligro"
            Language.ENGLISH -> "⚠️ Danger"
            Language.FRENCH -> "⚠️ Danger"
            Language.GERMAN -> "⚠️ Gefahr"
            Language.ITALIAN -> "⚠️ Pericolo"
        }
        else -> poi.type.name
    }

    // Badge color
    val badgeColor = when (poi.type.name) {
        "BEACH" -> Color(0xFF6FBAFF)
        "NATURAL" -> Color(0xFF7FD17F)
        "HISTORIC" -> Color(0xFFFF8080)
        "COMMERCIAL" -> Color(0xFFFFB85C)
        "DANGER" -> Color(0xFFFF5252)
        else -> Color(0xFF9E9E9E)
    }

    // Navigate button text translations
    val navigateButtonText = when (currentLanguage) {
        Language.CATALAN -> "Anar amb Maps"
        Language.SPANISH -> "Ir con Maps"
        Language.ENGLISH -> "Open in Maps"
        Language.FRENCH -> "Ouvrir dans Maps"
        Language.GERMAN -> "In Maps öffnen"
        Language.ITALIAN -> "Apri in Maps"
    }

    // Check for custom action button
    val actionButtonText = poi.getActionButtonText(currentLanguage)
    val hasActionUrl = !poi.actionUrl.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(poi.getName(currentLanguage)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // POI Image
            if (poi.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = poi.imageUrl,
                    contentDescription = poi.getName(currentLanguage),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Type badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = poi.getDescription(currentLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action button (custom URL if available)
                if (hasActionUrl && actionButtonText != null) {
                    Button(
                        onClick = { openUrl(poi.actionUrl!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = badgeColor
                        )
                    ) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Open URL",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = actionButtonText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Navigate button (always show)
                Button(
                    onClick = { onNavigateClick(poi) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = "Navigate",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = navigateButtonText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Platform-specific function to open coordinates in external map app
 */
expect fun openInMaps(latitude: Double, longitude: Double, name: String)

/**
 * Platform-specific function to open a URL in the default browser
 */
expect fun openUrl(url: String)
