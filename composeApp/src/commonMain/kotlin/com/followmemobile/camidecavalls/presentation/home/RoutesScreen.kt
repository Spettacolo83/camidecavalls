package com.followmemobile.camidecavalls.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import camidecavalls.composeapp.generated.resources.Res
import coil3.compose.SubcomposeAsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import org.koin.compose.koinInject

/**
 * Public composable for the ROUTES tab in the bottom navigation.
 * Shows the list of all 20 routes + the complete route.
 * No Scaffold or TopAppBar — padding is handled by MainScreen.
 */
@Composable
fun RoutesTabContent(
    onRouteClick: (Int) -> Unit
) {
    val screenModel: RoutesScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is RoutesUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is RoutesUiState.Empty -> {
                Text(
                    text = state.strings.homeNoRoutes,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is RoutesUiState.Success -> {
                RouteList(
                    routes = state.routes,
                    currentLanguage = state.currentLanguage,
                    strings = state.strings,
                    onRouteClick = { route -> onRouteClick(route.id) }
                )
            }

            is RoutesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading routes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteList(
    routes: List<Route>,
    currentLanguage: String,
    strings: LocalizedStrings,
    onRouteClick: (Route) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(routes, key = { it.id }) { route ->
            RouteItem(
                route = route,
                currentLanguage = currentLanguage,
                strings = strings,
                onClick = { onRouteClick(route) }
            )
        }

        // Complete Route as last item
        item(key = "complete-route") {
            CompleteRouteItem(
                routes = routes,
                strings = strings,
                onClick = { onRouteClick(Route(
                    id = 0,
                    number = 0,
                    name = strings.completeRouteName,
                    startPoint = "Maó",
                    endPoint = "Maó",
                    distanceKm = 185.0,
                    elevationGainMeters = routes.sumOf { it.elevationGainMeters },
                    elevationLossMeters = routes.sumOf { it.elevationLossMeters },
                    maxAltitudeMeters = routes.maxOfOrNull { it.maxAltitudeMeters } ?: 0,
                    minAltitudeMeters = routes.minOfOrNull { it.minAltitudeMeters } ?: 0,
                    asphaltPercentage = 0,
                    difficulty = com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH,
                    estimatedDurationMinutes = routes.sumOf { it.estimatedDurationMinutes },
                    description = ""
                )) }
            )
        }
    }
}

@Composable
private fun RouteItem(
    route: Route,
    currentLanguage: String,
    strings: LocalizedStrings,
    onClick: () -> Unit
) {
    // Load route image
    val routeImageBytes by produceState<ByteArray?>(initialValue = null) {
        value = try {
            Res.readBytes("files/images/routes/route_${route.id}.jpg")
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            routeImageBytes?.let { imageBytes ->
                SubcomposeAsyncImage(
                    model = imageBytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .background(
                            brush = Brush.horizontalGradient(
                                0.0f to Color.White.copy(alpha = 0.8f),
                                0.5f to Color.White.copy(alpha = 0.5f),
                                1.0f to Color.White.copy(alpha = 0.0f)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = strings.routeStage(route.number).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.7f to Color.White.copy(alpha = 0.9f),
                                1.0f to Color.White
                            )
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        label = strings.homeDistance,
                        value = "${route.distanceKm} km"
                    )
                    InfoItem(
                        label = strings.homeElevation,
                        value = "+${route.elevationGainMeters}m"
                    )
                    InfoItem(
                        label = strings.homeDuration,
                        value = "${route.estimatedDurationMinutes / 60}h ${route.estimatedDurationMinutes % 60}m"
                    )
                    DifficultyInfo(difficulty = route.difficulty, strings = strings)
                }
            }
        }
    }
}

@Composable
private fun CompleteRouteItem(
    routes: List<Route>,
    strings: LocalizedStrings,
    onClick: () -> Unit
) {
    val heroImageBytes by produceState<ByteArray?>(initialValue = null) {
        value = try {
            Res.readBytes("files/images/hero_cami.jpg")
        } catch (e: Exception) {
            null
        }
    }

    val totalDistance = 185.0
    val totalElevation = routes.sumOf { it.elevationGainMeters }
    val totalDuration = routes.sumOf { it.estimatedDurationMinutes }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            heroImageBytes?.let { imageBytes ->
                SubcomposeAsyncImage(
                    model = imageBytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.CenterEnd
                )
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .background(
                            brush = Brush.horizontalGradient(
                                0.0f to Color.White.copy(alpha = 0.8f),
                                0.5f to Color.White.copy(alpha = 0.5f),
                                1.0f to Color.White.copy(alpha = 0.0f)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = strings.completeRouteName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = strings.completeRouteSubtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.7f to Color.White.copy(alpha = 0.9f),
                                1.0f to Color.White
                            )
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        label = strings.homeDistance,
                        value = "$totalDistance km"
                    )
                    InfoItem(
                        label = strings.homeElevation,
                        value = "+${totalElevation}m"
                    )
                    InfoItem(
                        label = strings.homeDuration,
                        value = "${totalDuration / 60}h ${totalDuration % 60}m"
                    )
                    DifficultyInfo(
                        difficulty = com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH,
                        strings = strings
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyInfo(
    difficulty: com.followmemobile.camidecavalls.domain.model.Difficulty,
    strings: LocalizedStrings
) {
    val color = when (difficulty) {
        com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> Color(0xFF4CAF50)
        com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> Color(0xFFFF9800)
        com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> Color(0xFFF44336)
    }

    val text = when (difficulty) {
        com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> strings.difficultyLow
        com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> strings.difficultyMedium
        com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> strings.difficultyHigh
    }

    Column {
        Text(
            text = strings.routeDifficulty,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
