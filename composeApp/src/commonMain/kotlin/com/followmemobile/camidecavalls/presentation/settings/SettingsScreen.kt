package com.followmemobile.camidecavalls.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import camidecavalls.composeapp.generated.resources.Res
import camidecavalls.composeapp.generated.resources.*
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreen
import com.followmemobile.camidecavalls.presentation.home.DrawerContent
import com.followmemobile.camidecavalls.presentation.home.DrawerScreen
import com.followmemobile.camidecavalls.presentation.home.HomeScreen
import com.followmemobile.camidecavalls.presentation.pois.POIsScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: SettingsScreenModel = koinInject()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    uiState = convertToHomeUiState(state),
                    currentScreen = DrawerScreen.SETTINGS,
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        navigator.replaceAll(AboutScreen())
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
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            SettingsContent(
                state = state,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onLanguageSelected = { screenModel.onLanguageSelected(it) }
            )
        }
    }
}

// Helper to convert SettingsState to HomeUiState for drawer
private fun convertToHomeUiState(settingsState: SettingsState): com.followmemobile.camidecavalls.presentation.home.HomeUiState {
    return com.followmemobile.camidecavalls.presentation.home.HomeUiState.Success(
        routes = emptyList(),
        currentLanguage = "en",
        strings = settingsState.strings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    onMenuClick: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val strings = state.strings

    val languages = remember {
        listOf(
            LanguageItem("ca"),
            LanguageItem("es"),
            LanguageItem("en"),
            LanguageItem("de"),
            LanguageItem("fr"),
            LanguageItem("it")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language Section Header
            item {
                Text(
                    text = strings.settingsLanguage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Language Options
            items(languages) { language ->
                LanguageOptionItem(
                    languageItem = language,
                    strings = strings,
                    isSelected = state.selectedLanguage == language.code,
                    onSelect = { onLanguageSelected(language.code) }
                )
            }
        }
    }
}

@Composable
fun LanguageOptionItem(
    languageItem: LanguageItem,
    strings: com.followmemobile.camidecavalls.domain.util.LocalizedStrings,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val languageName = when (languageItem.code) {
        "ca" -> strings.languageCa
        "es" -> strings.languageEs
        "en" -> strings.languageEn
        "de" -> strings.languageDe
        "fr" -> strings.languageFr
        "it" -> strings.languageIt
        else -> languageItem.code
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onSelect)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = languageName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

data class LanguageItem(
    val code: String
)
