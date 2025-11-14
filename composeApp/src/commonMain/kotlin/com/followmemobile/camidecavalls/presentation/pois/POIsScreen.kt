package com.followmemobile.camidecavalls.presentation.pois

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.detail.POIDetailContent
import com.followmemobile.camidecavalls.presentation.detail.openInMaps
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreen
import com.followmemobile.camidecavalls.presentation.home.DrawerContent
import com.followmemobile.camidecavalls.presentation.home.DrawerScreen
import com.followmemobile.camidecavalls.presentation.home.RoutesScreen
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch
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

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // State for detail bottom sheet
        var selectedPoiForDetail by remember { mutableStateOf<PointOfInterest?>(null) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = convertToRoutesUiState(uiState),
                    currentScreen = DrawerScreen.POIS,
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(AboutScreen())
                    },
                    onRoutesClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(RoutesScreen())
                    },
                    onMapClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(FullMapScreen())
                    },
                    onPOIsClick = {
                        scope.launch { drawerState.close() }
                    },
                    onNotebookClick = {
                        scope.launch { drawerState.close() }
                        // TODO: Navigate to Notebook/Sessions screen
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(SettingsScreen())
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            POIsScreenContent(
                uiState = uiState,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onMapReady = { controller -> screenModel.onMapReady(controller) },
                onClosePopup = { screenModel.closePopup() },
                onToggleType = { type -> screenModel.toggleType(type) },
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
}

// Helper to convert POIsUiState to RoutesUiState for drawer
private fun convertToRoutesUiState(poisUiState: POIsUiState): com.followmemobile.camidecavalls.presentation.home.RoutesUiState {
    return com.followmemobile.camidecavalls.presentation.home.RoutesUiState.Success(
        routes = emptyList(),
        currentLanguage = "en",
        strings = poisUiState.strings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun POIsScreenContent(
    uiState: POIsUiState,
    onMenuClick: () -> Unit,
    onMapReady: (MapLayerController) -> Unit,
    onClosePopup: () -> Unit,
    onToggleType: (POIType) -> Unit,
    onShowDetails: (PointOfInterest) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.strings.poisTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
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
                        .padding(bottom = 104.dp)
                )
            }

            POIFilterBar(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 24.dp),
                visibleTypes = uiState.visibleTypes,
                strings = uiState.strings,
                onToggleType = onToggleType
            )
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
@Composable
private fun POIFilterBar(
    visibleTypes: Set<POIType>,
    strings: LocalizedStrings,
    onToggleType: (POIType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            POIFilterChip(
                type = POIType.BEACH,
                label = strings.poiTypeBeach,
                selected = visibleTypes.contains(POIType.BEACH),
                onToggleType = onToggleType
            )
            POIFilterChip(
                type = POIType.NATURAL,
                label = strings.poiTypeNatural,
                selected = visibleTypes.contains(POIType.NATURAL),
                onToggleType = onToggleType
            )
            POIFilterChip(
                type = POIType.HISTORIC,
                label = strings.poiTypeHistoric,
                selected = visibleTypes.contains(POIType.HISTORIC),
                onToggleType = onToggleType
            )
            POIFilterChip(
                type = POIType.COMMERCIAL,
                label = strings.poiTypeCommercial,
                selected = visibleTypes.contains(POIType.COMMERCIAL),
                onToggleType = onToggleType
            )
        }
    }
}

@Composable
private fun POIFilterChip(
    type: POIType,
    label: String,
    selected: Boolean,
    onToggleType: (POIType) -> Unit
) {
    val tint = PoiTypeColors.chipTint(type)
    val backgroundColor = if (selected) {
        tint.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onToggleType(type) },
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        tonalElevation = if (selected) 1.dp else 0.dp,
        border = BorderStroke(1.dp, tint.copy(alpha = if (selected) 0.6f else 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(tint, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
