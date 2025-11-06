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
import camidecavalls.composeapp.generated.resources.Res
import camidecavalls.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class SettingsScreen : Screen {
    
    @Composable
    override fun Content() {
        val screenModel: SettingsScreenModel = koinInject()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        SettingsContent(
            state = state,
            onBackClick = { navigator.pop() },
            onLanguageSelected = { screenModel.onLanguageSelected(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    onBackClick: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = remember {
        listOf(
            LanguageItem("ca", "language_ca"),
            LanguageItem("es", "language_es"),
            LanguageItem("en", "language_en"),
            LanguageItem("de", "language_de"),
            LanguageItem("fr", "language_fr"),
            LanguageItem("it", "language_it")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                    text = stringResource(Res.string.settings_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Language Options
            items(languages) { language ->
                LanguageOptionItem(
                    languageItem = language,
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
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val languageName = when (languageItem.stringKey) {
        "language_ca" -> stringResource(Res.string.language_ca)
        "language_es" -> stringResource(Res.string.language_es)
        "language_en" -> stringResource(Res.string.language_en)
        "language_de" -> stringResource(Res.string.language_de)
        "language_fr" -> stringResource(Res.string.language_fr)
        "language_it" -> stringResource(Res.string.language_it)
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
    val code: String,
    val stringKey: String
)
