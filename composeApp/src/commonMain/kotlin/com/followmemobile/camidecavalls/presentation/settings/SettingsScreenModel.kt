package com.followmemobile.camidecavalls.presentation.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        screenModelScope.launch {
            val currentLang = languageRepository.getCurrentLanguage()
            _state.value = _state.value.copy(
                selectedLanguage = currentLang,
                strings = LocalizedStrings(currentLang)
            )
        }
    }

    fun onLanguageSelected(languageCode: String) {
        screenModelScope.launch {
            try {
                languageRepository.setLanguage(languageCode)
                _state.value = _state.value.copy(
                    selectedLanguage = languageCode,
                    strings = LocalizedStrings(languageCode)
                )
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}

data class SettingsState(
    val selectedLanguage: String = "en",
    val strings: LocalizedStrings = LocalizedStrings("en")
)
