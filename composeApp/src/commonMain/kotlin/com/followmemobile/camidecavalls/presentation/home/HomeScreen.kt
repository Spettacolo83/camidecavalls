package com.followmemobile.camidecavalls.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.detail.RouteDetailScreen
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreen
import com.followmemobile.camidecavalls.presentation.pois.POIsScreen
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch
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

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = uiState,
                    currentScreen = DrawerScreen.ROUTES,
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(AboutScreen())
                    },
                    onRoutesClick = {
                        scope.launch { drawerState.close() }
                    },
                    onMapClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(FullMapScreen())
                    },
                    onPOIsClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(POIsScreen())
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
            HomeScreenContent(
                uiState = uiState,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onRouteClick = { route ->
                    navigator.push(RouteDetailScreen(route.id))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onMenuClick: () -> Unit,
    onRouteClick: (Route) -> Unit
) {
    val title = when (uiState) {
        is HomeUiState.Success -> uiState.strings.routesTitle
        is HomeUiState.Empty -> uiState.strings.routesTitle
        else -> "Routes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            when (uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeUiState.Empty -> {
                    Text(
                        text = uiState.strings.homeNoRoutes,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is HomeUiState.Success -> {
                    RouteList(
                        routes = uiState.routes,
                        currentLanguage = uiState.currentLanguage,
                        strings = uiState.strings,
                        onRouteClick = onRouteClick
                    )
                }

                is HomeUiState.Error -> {
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
                            text = uiState.message,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

enum class DrawerScreen {
    ABOUT, ROUTES, MAP, POIS, NOTEBOOK, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    uiState: HomeUiState,
    currentScreen: DrawerScreen,
    onAboutClick: () -> Unit,
    onRoutesClick: () -> Unit,
    onMapClick: () -> Unit,
    onPOIsClick: () -> Unit,
    onNotebookClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val strings = when (uiState) {
        is HomeUiState.Success -> uiState.strings
        is HomeUiState.Empty -> uiState.strings
        else -> com.followmemobile.camidecavalls.domain.util.LocalizedStrings("en")
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button (same position as hamburger menu)
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Default.Close, contentDescription = "Close Menu")
                }

                // App Title
                Text(
                    text = "Camí de Cavalls",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 48.dp) // Balance with close button
                )
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // About (Camí de Cavalls)
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                label = { Text(strings.menuAbout) },
                selected = currentScreen == DrawerScreen.ABOUT,
                onClick = onAboutClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Routes
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                label = { Text(strings.menuRoutes) },
                selected = currentScreen == DrawerScreen.ROUTES,
                onClick = onRoutesClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Map
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Map, contentDescription = null) },
                label = { Text(strings.menuMap) },
                selected = currentScreen == DrawerScreen.MAP,
                onClick = onMapClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // POIs
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Place, contentDescription = null) },
                label = { Text(strings.menuPOIs) },
                selected = currentScreen == DrawerScreen.POIS,
                onClick = onPOIsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Notebook
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Book, contentDescription = null) },
                label = { Text(strings.menuNotebook) },
                selected = currentScreen == DrawerScreen.NOTEBOOK,
                onClick = onNotebookClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Settings
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(strings.menuSettings) },
                selected = currentScreen == DrawerScreen.SETTINGS,
                onClick = onSettingsClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@Composable
private fun RouteList(
    routes: List<Route>,
    currentLanguage: String,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings,
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
    }
}

@Composable
private fun RouteItem(
    route: Route,
    currentLanguage: String,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings,
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
                    text = strings.routeStage(route.number),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                DifficultyChip(difficulty = route.difficulty, strings = strings)
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
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    difficulty: com.followmemobile.camidecavalls.domain.model.Difficulty,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings
) {
    val color = when (difficulty) {
        com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> MaterialTheme.colorScheme.tertiary
        com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> MaterialTheme.colorScheme.error
    }

    val text = when (difficulty) {
        com.followmemobile.camidecavalls.domain.model.Difficulty.LOW -> strings.difficultyLow
        com.followmemobile.camidecavalls.domain.model.Difficulty.MEDIUM -> strings.difficultyMedium
        com.followmemobile.camidecavalls.domain.model.Difficulty.HIGH -> strings.difficultyHigh
    }

    Text(
        text = text,
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
