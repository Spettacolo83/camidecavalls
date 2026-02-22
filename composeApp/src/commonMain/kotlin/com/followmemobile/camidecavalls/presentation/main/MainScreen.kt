package com.followmemobile.camidecavalls.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.followmemobile.camidecavalls.presentation.about.AboutCamiScreen
import com.followmemobile.camidecavalls.presentation.detail.RouteDetailScreen
import com.followmemobile.camidecavalls.presentation.home.RoutesTabContent
import com.followmemobile.camidecavalls.presentation.notebook.NotebookTabContent
import com.followmemobile.camidecavalls.presentation.notebook.SessionDetailScreen
import com.followmemobile.camidecavalls.presentation.pois.POIsTabContent
import com.followmemobile.camidecavalls.presentation.settings.LanguageSettingsScreen
import com.followmemobile.camidecavalls.presentation.settings.SettingsHubContent
import com.followmemobile.camidecavalls.presentation.tracking.MapTabContent
import org.koin.compose.koinInject

/**
 * Root screen with floating bottom navigation bar.
 * Contains 5 tabs: MAP, ROUTES, POI, NOTEBOOK, SETTINGS.
 *
 * MAP and POI tabs have full-screen maps that extend behind the floating bar.
 * Other tabs get safe area padding (top for status bar, bottom for floating bar).
 */
class MainScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val languageRepository: LanguageRepository = koinInject()
        val language by languageRepository.observeCurrentLanguage()
            .collectAsState(initial = languageRepository.getSystemLanguage())
        val strings = LocalizedStrings(language)

        // Tab state - MAP is default
        var currentTab by rememberSaveable { mutableStateOf(BottomTab.MAP) }
        // Route selected from ROUTES tab for MAP tab
        var selectedRouteId by rememberSaveable { mutableStateOf<Int?>(null) }

        // Track recording state
        val trackingManager: TrackingManager = koinInject()
        val trackingState by trackingManager.trackingState.collectAsState()

        // Hide bottom bar during active tracking (Recording/Paused)
        val showBottomBar = trackingState !is TrackingState.Recording
                && trackingState !is TrackingState.Paused

        // Observe RouteSelectionManager for cross-tab navigation
        val routeSelectionManager: RouteSelectionManager = koinInject()
        val routeSelection by routeSelectionManager.selectedRouteId.collectAsState()

        LaunchedEffect(routeSelection) {
            val selection = routeSelection
            if (selection != null) {
                // routeId=0 means "complete route" → load all routes on map (pass null)
                selectedRouteId = if (selection == 0) null else selection
                currentTab = BottomTab.MAP
                routeSelectionManager.consume()
            }
        }

        // Use Scaffold to get safe area insets, but no bottomBar
        Scaffold { paddingValues ->
            val safeAreaTop = paddingValues.calculateTopPadding()
            val safeAreaBottom = paddingValues.calculateBottomPadding()

            // Floating bar bottom position: above system nav area + margin
            val barBottomMargin = safeAreaBottom + 8.dp
            // Content bottom padding: bar height (~64dp) + bar bottom margin + spacing
            val contentBottomPadding = safeAreaBottom + 80.dp

            Box(modifier = Modifier.fillMaxSize()) {
                // Tab content
                // Extra bottom padding for FABs in map tabs to clear the floating bar
                val fabBottomPadding = if (showBottomBar) contentBottomPadding else 0.dp

                when (currentTab) {
                    // Map tabs: full screen, content extends behind bar and to screen edges
                    BottomTab.MAP -> MapTabContent(
                        routeId = selectedRouteId,
                        fabBottomPadding = fabBottomPadding
                    )

                    BottomTab.POI -> POIsTabContent(
                        fabBottomPadding = fabBottomPadding
                    )

                    // Non-map tabs: TopAppBar + content + space for floating bar
                    BottomTab.ROUTES -> TabWithTopBar(
                        title = strings.bottomBarRoutes,
                        safeAreaTop = safeAreaTop,
                        bottomPadding = if (showBottomBar) contentBottomPadding else safeAreaBottom
                    ) {
                        RoutesTabContent(
                            onRouteClick = { routeId ->
                                navigator.push(RouteDetailScreen(routeId))
                            }
                        )
                    }

                    BottomTab.NOTEBOOK -> TabWithTopBar(
                        title = strings.bottomBarNotebook,
                        safeAreaTop = safeAreaTop,
                        bottomPadding = if (showBottomBar) contentBottomPadding else safeAreaBottom
                    ) {
                        NotebookTabContent(
                            onSessionClick = { sessionId ->
                                navigator.push(SessionDetailScreen(sessionId))
                            },
                            onSwitchToMapTab = {
                                currentTab = BottomTab.MAP
                            }
                        )
                    }

                    BottomTab.SETTINGS -> TabWithTopBar(
                        title = strings.bottomBarSettings,
                        safeAreaTop = safeAreaTop,
                        bottomPadding = if (showBottomBar) contentBottomPadding else safeAreaBottom
                    ) {
                        SettingsHubContent(
                            strings = strings,
                            onLanguageClick = { navigator.push(LanguageSettingsScreen()) },
                            onAboutClick = { navigator.push(AboutCamiScreen()) }
                        )
                    }
                }

                // Floating bottom bar overlay
                if (showBottomBar) {
                    WeWardBottomBar(
                        currentTab = currentTab,
                        strings = strings,
                        onTabSelected = { tab -> currentTab = tab },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = barBottomMargin
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabWithTopBar(
    title: String,
    safeAreaTop: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
    ) {
        // Status bar area colored with the same color as the TopAppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(safeAreaTop)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        TopAppBar(
            title = { Text(title) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
