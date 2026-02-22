package com.followmemobile.camidecavalls.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject

/**
 * Language settings screen - pushed from Settings hub.
 * Renamed from SettingsScreen to LanguageSettingsScreen conceptually,
 * but class kept as LanguageSettingsScreen for navigation.
 */
class LanguageSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: SettingsScreenModel = koinInject()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LanguageSettingsContent(
            state = state,
            onBackClick = { navigator.pop() },
            onLanguageSelected = { screenModel.onLanguageSelected(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSettingsContent(
    state: SettingsState,
    onBackClick: () -> Unit,
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
                title = { Text(strings.settingsLanguageOption) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
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
            item {
                Text(
                    text = strings.settingsLanguage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

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
