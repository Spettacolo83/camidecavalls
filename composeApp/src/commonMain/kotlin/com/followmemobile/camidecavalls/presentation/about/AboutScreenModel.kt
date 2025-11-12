package com.followmemobile.camidecavalls.presentation.about

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for AboutScreen.
 * Handles language loading for localized content.
 */
class AboutScreenModel(
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        loadLanguage()
    }

    private fun loadLanguage() {
        screenModelScope.launch {
            val currentLang = languageRepository.getCurrentLanguage()
            _uiState.update { it.copy(strings = LocalizedStrings(currentLang)) }
        }
    }

    fun onMenuClick() {
        // Menu click is handled by parent navigation drawer
    }
}

/**
 * UI State for AboutScreen
 */
data class AboutUiState(
    val strings: LocalizedStrings = LocalizedStrings("en")
)
