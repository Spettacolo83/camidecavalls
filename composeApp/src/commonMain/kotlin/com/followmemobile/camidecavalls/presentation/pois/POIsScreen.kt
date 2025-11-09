package com.followmemobile.camidecavalls.presentation.pois

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.SubcomposeAsyncImage
import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.presentation.detail.POIDetailContent
import com.followmemobile.camidecavalls.presentation.detail.openInMaps
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import org.koin.compose.koinInject

/**
 * POIs Screen showing all POIs on a Menorca map with colored markers.
 *
 * Features:
 * - All POIs displayed with colored markers (blue=BEACH, green=NATURAL, red=HISTORIC)
 * - Semi-transparent popup on marker tap with preview
 * - Navigation to full detail screen
 */
class POIsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel: POIsScreenModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        // State for detail bottom sheet
        var selectedPoiForDetail by remember { mutableStateOf<PointOfInterest?>(null) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        POIsScreenContent(
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onMapReady = { controller -> screenModel.onMapReady(controller) },
            onClosePopup = { screenModel.closePopup() },
            onShowDetails = { poi ->
                // Show POI detail in bottom sheet (map stays loaded underneath)
                selectedPoiForDetail = poi
            }
        )

        // Material 3 ModalBottomSheet for POI detail
        selectedPoiForDetail?.let { poi ->
            ModalBottomSheet(
                onDismissRequest = { selectedPoiForDetail = null },
                sheetState = sheetState
            ) {
                POIDetailContent(
                    poi = poi,
                    currentLanguage = uiState.currentLanguage,
                    onBackClick = { selectedPoiForDetail = null },
                    onNavigateClick = { poiToNavigate ->
                        openInMaps(
                            poiToNavigate.latitude,
                            poiToNavigate.longitude,
                            poiToNavigate.getName(uiState.currentLanguage)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun POIsScreenContent(
    uiState: POIsUiState,
    onBackClick: () -> Unit,
    onMapReady: (MapLayerController) -> Unit,
    onClosePopup: () -> Unit,
    onShowDetails: (PointOfInterest) -> Unit
) {
    val screenTitle = when (uiState.currentLanguage) {
        Language.CATALAN -> "Punts d'Interès"
        Language.SPANISH -> "Puntos de Interés"
        Language.ENGLISH -> "Points of Interest"
        Language.FRENCH -> "Points d'Intérêt"
        Language.GERMAN -> "Sehenswürdigkeiten"
        Language.ITALIAN -> "Punti di Interesse"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // MapLibre map with all POI markers
            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = 39.95,  // Menorca center
                longitude = 4.05,
                zoom = 10.0,
                styleUrl = MapStyles.LIBERTY,
                onMapReady = { controller ->
                    onMapReady(controller)
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(uiState.error)
                }
            }

            // POI Popup (shown when a marker is tapped)
            uiState.selectedPoi?.let { poi ->
                POIPopup(
                    poi = poi,
                    currentLanguage = uiState.currentLanguage,
                    onClose = onClosePopup,
                    onShowDetails = { onShowDetails(poi) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

/**
 * Semi-transparent popup showing POI preview
 * New compact design: max 280dp wide, 1-line title, image with badge, 3-line description
 */
@Composable
private fun POIPopup(
    poi: PointOfInterest,
    currentLanguage: Language,
    onClose: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get background color based on POI type
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

    // Button text translations
    val detailsButtonText = when (currentLanguage) {
        Language.CATALAN -> "Veure detalls"
        Language.SPANISH -> "Ver detalles"
        Language.ENGLISH -> "View details"
        Language.FRENCH -> "Voir détails"
        Language.GERMAN -> "Details ansehen"
        Language.ITALIAN -> "Vedi dettagli"
    }

    Card(
        modifier = modifier
            .width(280.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title (1 line) with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poi.getName(currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image with badge overlay
            poi.imageUrl?.let { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = poi.getName(currentLanguage),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
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
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    )

                    // Badge overlay on top-left of image
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (poi.type.name) {
                            "BEACH" -> Color(0xFF6FBAFF)
                            "NATURAL" -> Color(0xFF7FD17F)
                            "HISTORIC" -> Color(0xFFFF8080)
                            else -> Color.Gray
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description (max 3 lines with ellipsis)
            val description = poi.getDescription(currentLanguage)
            val previewText = if (description.length > 120) {
                description.substring(0, 120).trim() + "..."
            } else {
                description
            }

            Text(
                text = previewText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // "See Details" button - always visible
            Button(
                onClick = onShowDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = detailsButtonText,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
                )
            }
        }
    }
}
