package com.followmemobile.camidecavalls.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
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
    // Get background color based on POI type (lighter than popup)
    val backgroundColor = when (poi.type.name) {
        "BEACH" -> Color(0xFFE6F5FF)      // Very very light blue
        "NATURAL" -> Color(0xFFEAF7EA)    // Very very light green
        "HISTORIC" -> Color(0xFFFFE6E6)   // Very very light red/pink
        else -> Color(0xFFF5F5F5)         // Very light gray
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
        else -> poi.type.name
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
            poi.imageUrl?.let { imageUrl ->
                SubcomposeAsyncImage(
                    model = imageUrl,
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
                // Type badge (no title duplicate - title is in TopBar)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (poi.type.name) {
                        "BEACH" -> Color(0xFF6FBAFF)
                        "NATURAL" -> Color(0xFF7FD17F)
                        "HISTORIC" -> Color(0xFFFF8080)
                        else -> Color(0xFF9E9E9E)
                    },
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

                // Navigate button
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
