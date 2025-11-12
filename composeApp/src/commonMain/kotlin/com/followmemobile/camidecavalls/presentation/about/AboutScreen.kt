package com.followmemobile.camidecavalls.presentation.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.SubcomposeAsyncImage
import org.jetbrains.compose.resources.ExperimentalResourceApi
import camidecavalls.composeapp.generated.resources.Res
import com.followmemobile.camidecavalls.presentation.detail.RouteDetailScreen
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreen
import com.followmemobile.camidecavalls.presentation.home.DrawerContent
import com.followmemobile.camidecavalls.presentation.home.DrawerScreen
import com.followmemobile.camidecavalls.presentation.home.HomeScreen
import com.followmemobile.camidecavalls.presentation.pois.POIsScreen
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * AboutScreen - Landing page of the app showing information about Camí de Cavalls.
 *
 * Features:
 * - Hero section with welcome message
 * - Detailed description of the trail
 * - UNESCO Biosphere Reserve information
 * - Key stats (length, stages)
 * - Call to action to explore routes
 */
class AboutScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: AboutScreenModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val uiState = screenModel.uiState.collectAsState().value

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = convertToHomeUiState(uiState),
                    currentScreen = DrawerScreen.ABOUT,
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                    },
                    onRoutesClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(HomeScreen())
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
            AboutScreenContent(
                uiState = uiState,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onExploreClick = {
                    // Navigate to first route as example
                    navigator.push(RouteDetailScreen(1))
                }
            )
        }
    }
}

// Helper to convert AboutUiState to HomeUiState for drawer
private fun convertToHomeUiState(aboutUiState: AboutUiState): com.followmemobile.camidecavalls.presentation.home.HomeUiState {
    return com.followmemobile.camidecavalls.presentation.home.HomeUiState.Success(
        routes = emptyList(),
        currentLanguage = "en",
        strings = aboutUiState.strings
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
private fun AboutScreenContent(
    uiState: AboutUiState,
    onMenuClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Load hero image from resources
    val heroImageBytes by produceState<ByteArray?>(initialValue = null) {
        value = Res.readBytes("files/images/hero_cami.jpg")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.strings.aboutTitle) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Hero Image
            heroImageBytes?.let { imageBytes ->
                SubcomposeAsyncImage(
                    model = imageBytes,
                    contentDescription = "Camí de Cavalls",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Welcome
                Text(
                    text = uiState.strings.aboutWelcome,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = uiState.strings.aboutLength,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = uiState.strings.aboutStages,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = uiState.strings.aboutDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                    )
                }

                // UNESCO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "UNESCO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = uiState.strings.aboutUNESCO,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                        )
                    }
                }

                // Call to Action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.strings.aboutCTA,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Button(
                            onClick = onExploreClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = uiState.strings.aboutExplore,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
