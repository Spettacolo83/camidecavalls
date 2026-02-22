package com.followmemobile.camidecavalls.presentation.pois

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.SubcomposeAsyncImage
import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.POIType
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.detail.POIDetailContent
import com.followmemobile.camidecavalls.presentation.detail.openInMaps
import com.followmemobile.camidecavalls.presentation.map.MapLayerController
import com.followmemobile.camidecavalls.presentation.map.MapStyles
import com.followmemobile.camidecavalls.presentation.map.MapWithLayers
import com.followmemobile.camidecavalls.presentation.map.rememberMenorcaViewportState
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * Public composable for the POI tab in the bottom navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIsTabContent(
    fabBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val screenModel: POIsScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()

    // State for detail bottom sheet
    var selectedPoiForDetail by remember { mutableStateOf<PointOfInterest?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    POIsScreenContent(
        uiState = uiState,
        onMapReady = { controller -> screenModel.onMapReady(controller) },
        onClosePopup = { screenModel.closePopup() },
        onToggleType = { type -> screenModel.toggleType(type) },
        onShowDetails = { poi ->
            selectedPoiForDetail = poi
        },
        onPopupMeasured = { heightPx -> screenModel.updatePopupHeight(heightPx) },
        onPopupBottomPaddingResolved = { paddingPx ->
            screenModel.updatePopupBottomPadding(paddingPx)
        },
        fabBottomPadding = fabBottomPadding
    )

    // Material 3 ModalBottomSheet for POI detail
    selectedPoiForDetail?.let { poi ->
        // Match sheet background with POI type color
        val sheetBackgroundColor = when (poi.type.name) {
            "BEACH" -> Color(0xFFE6F5FF)
            "NATURAL" -> Color(0xFFEAF7EA)
            "HISTORIC" -> Color(0xFFFFE6E6)
            else -> Color(0xFFF5F5F5)
        }
        ModalBottomSheet(
            onDismissRequest = { selectedPoiForDetail = null },
            sheetState = sheetState,
            containerColor = sheetBackgroundColor,
            dragHandle = { BottomSheetDefaults.DragHandle() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun POIsScreenContent(
    uiState: POIsUiState,
    onMapReady: (MapLayerController) -> Unit,
    onClosePopup: () -> Unit,
    onToggleType: (POIType) -> Unit,
    onShowDetails: (PointOfInterest) -> Unit,
    onPopupMeasured: (Int) -> Unit,
    onPopupBottomPaddingResolved: (Int) -> Unit,
    fabBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    var filtersVisible by remember { mutableStateOf(false) }
    val filterTransitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(filtersVisible) {
        filterTransitionState.targetState = filtersVisible
    }

    val density = LocalDensity.current
    val filterPopupOffset = remember(density, fabBottomPadding) {
        IntOffset(
            x = -with(density) { 24.dp.roundToPx() },
            y = -with(density) { (24.dp + fabBottomPadding).roundToPx() }
        )
    }

    // Popup bottom aligns with the filter FAB bottom (24dp padding + FAB height ~56dp = 80dp)
    val effectivePopupBottom = 24.dp + fabBottomPadding
    val popupBottomPaddingPx = remember(density, fabBottomPadding) { with(density) { effectivePopupBottom.roundToPx() } }
    LaunchedEffect(popupBottomPaddingPx) {
        onPopupBottomPaddingResolved(popupBottomPaddingPx)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                        controller.updateCamera(
                            latitude = cameraConfig.latitude,
                            longitude = cameraConfig.longitude,
                            zoom = cameraConfig.zoom,
                            animated = false
                        )
                    }
                )

                LaunchedEffect(mapController, cameraConfig) {
                    mapController?.updateCamera(
                        latitude = cameraConfig.latitude,
                        longitude = cameraConfig.longitude,
                        zoom = cameraConfig.zoom,
                        animated = false
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(uiState.error)
                }
            }

                // Filter FAB - declared BEFORE popup so popup draws on top
            FloatingActionButton(
                onClick = { filtersVisible = !filtersVisible },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp + fabBottomPadding, end = 24.dp),
                containerColor = Color(0xFF1C1C2E),
                contentColor = Color(0xFF4FC3F7)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = uiState.strings.poisFiltersLabel
                )
            }

            uiState.selectedPoi?.let { poi ->
                POIPopup(
                    poi = poi,
                    currentLanguage = uiState.currentLanguage,
                    onClose = onClosePopup,
                    onShowDetails = { onShowDetails(poi) },
                    onMeasured = onPopupMeasured,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = effectivePopupBottom)
                )
            }

            if (filterTransitionState.currentState || filterTransitionState.targetState) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    offset = filterPopupOffset,
                    onDismissRequest = { filtersVisible = false },
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnClickOutside = true,
                        dismissOnBackPress = true
                    )
                ) {
                    AnimatedVisibility(
                        visibleState = filterTransitionState,
                        enter = fadeIn(animationSpec = tween(durationMillis = 1000)) +
                            slideInVertically(animationSpec = tween(durationMillis = 1000)) { it / 2 },
                        exit = fadeOut(animationSpec = tween(durationMillis = 1000)) +
                            slideOutVertically(animationSpec = tween(durationMillis = 1000)) { it / 2 }
                    ) {
                        POIFilterBar(
                            visibleTypes = uiState.visibleTypes,
                            strings = uiState.strings,
                            onToggleType = onToggleType
                        )
                    }
                }
            }

    }
}

@Composable
private fun POIPopup(
    poi: PointOfInterest,
    currentLanguage: Language,
    onClose: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasured: (Int) -> Unit = {}
) {
    val backgroundColor = when (poi.type.name) {
        "BEACH" -> Color(0xFFE6F5FF)
        "NATURAL" -> Color(0xFFEAF7EA)
        "HISTORIC" -> Color(0xFFFFE6E6)
        else -> Color(0xFFF5F5F5)
    }

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
            .onSizeChanged { layout -> onMeasured(layout.height) }
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
            POIFilterChip(type = POIType.BEACH, label = strings.poiTypeBeach, selected = visibleTypes.contains(POIType.BEACH), onToggleType = onToggleType)
            POIFilterChip(type = POIType.NATURAL, label = strings.poiTypeNatural, selected = visibleTypes.contains(POIType.NATURAL), onToggleType = onToggleType)
            POIFilterChip(type = POIType.HISTORIC, label = strings.poiTypeHistoric, selected = visibleTypes.contains(POIType.HISTORIC), onToggleType = onToggleType)
            POIFilterChip(type = POIType.COMMERCIAL, label = strings.poiTypeCommercial, selected = visibleTypes.contains(POIType.COMMERCIAL), onToggleType = onToggleType)
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
