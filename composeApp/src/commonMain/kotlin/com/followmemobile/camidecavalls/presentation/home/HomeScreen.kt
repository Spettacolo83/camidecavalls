package com.followmemobile.camidecavalls.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.presentation.detail.RouteDetailScreen
import org.koin.compose.koinInject

/**
 * Home screen displaying the list of all 20 routes of Camí de Cavalls.
 */
class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: HomeScreenModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by screenModel.uiState.collectAsState()

        HomeScreenContent(
            uiState = uiState,
            onRouteClick = { route ->
                navigator.push(RouteDetailScreen(route.id))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onRouteClick: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camí de Cavalls") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeUiState.Empty -> {
                    Text(
                        text = "No routes available",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is HomeUiState.Success -> {
                    RouteList(
                        routes = uiState.routes,
                        onRouteClick = onRouteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteList(
    routes: List<Route>,
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
                onClick = { onRouteClick(route) }
            )
        }
    }
}

@Composable
private fun RouteItem(
    route: Route,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Stage number and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stage ${route.number}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                DifficultyChip(difficulty = route.difficulty)
            }

            Text(
                text = route.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Route info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Distance",
                    value = "${route.distanceKm} km"
                )
                InfoItem(
                    label = "Elevation",
                    value = "+${route.elevationGainMeters}m"
                )
                InfoItem(
                    label = "Duration",
                    value = "${route.estimatedDurationMinutes / 60}h ${route.estimatedDurationMinutes % 60}m"
                )
            }
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: com.followmemobile.camidecavalls.domain.model.Difficulty) {
    val color = when (difficulty) {
        com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> MaterialTheme.colorScheme.tertiary
        com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> MaterialTheme.colorScheme.error
    }

    Text(
        text = difficulty.name,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun InfoItem(label: String, value: String) {
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
