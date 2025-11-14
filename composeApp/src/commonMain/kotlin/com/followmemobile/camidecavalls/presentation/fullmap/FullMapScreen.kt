package com.followmemobile.camidecavalls.presentation.fullmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.detail.POIDetailContent
import com.followmemobile.camidecavalls.presentation.detail.openInMaps
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
 * Full Map Screen showing the entire island with routes and POI markers.
 *
 * Features:
 * - All 20 routes displayed simultaneously with simplified GPS data for performance
 * - All POIs rendered with colored markers and interactive popup
 * - Quick access to POI details via a modal bottom sheet
 */
class FullMapScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: FullMapScreenModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        var selectedPoiForDetail by remember { mutableStateOf<PointOfInterest?>(null) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = convertToRoutesUiState(uiState),
                    currentScreen = DrawerScreen.MAP,
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
            FullMapScreenContent(
                uiState = uiState,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onMapReady = { controller -> screenModel.onMapReady(controller) },
                onClosePoiPopup = { screenModel.closePoiPopup() },
                onShowPoiDetails = { poi -> selectedPoiForDetail = poi }
            )
        }

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

// Helper to convert FullMapUiState to RoutesUiState for drawer
private fun convertToRoutesUiState(fullMapUiState: FullMapUiState): com.followmemobile.camidecavalls.presentation.home.RoutesUiState {
    return com.followmemobile.camidecavalls.presentation.home.RoutesUiState.Success(
        routes = emptyList(),
        currentLanguage = "en",
        strings = fullMapUiState.strings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullMapScreenContent(
    uiState: FullMapUiState,
    onMenuClick: () -> Unit,
    onMapReady: (MapLayerController) -> Unit,
    onClosePoiPopup: () -> Unit,
    onShowPoiDetails: (PointOfInterest) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.strings.mapTitle) },
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
            val isLoading = uiState.isLoadingRoutes || uiState.isLoadingPOIs
            val errorMessage = uiState.errorRoutes ?: uiState.errorPOIs

            // MapLibre map with all 20 routes
            MapWithLayers(
                modifier = Modifier.fillMaxSize(),
                latitude = 39.95,  // Menorca center
                longitude = 4.05,
                zoom = 9.5,
                styleUrl = MapStyles.LIBERTY,
                onMapReady = { controller ->
                    onMapReady(controller)
                }
            )

            // Loading indicator while routes are being loaded
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message if loading failed
            if (errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(errorMessage)
                }
            }

            uiState.selectedPoi?.let { poi ->
                POIPopup(
                    poi = poi,
                    currentLanguage = uiState.currentLanguage,
                    onClose = onClosePoiPopup,
                    onShowDetails = { onShowPoiDetails(poi) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
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
    modifier: Modifier = Modifier
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
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onShowDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(detailsButtonText)
            }
        }
    }
}
