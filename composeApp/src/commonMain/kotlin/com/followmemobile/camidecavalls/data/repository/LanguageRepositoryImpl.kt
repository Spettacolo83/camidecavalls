package com.followmemobile.camidecavalls.data.repository

import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.util.getSystemLanguageCode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of LanguageRepository using multiplatform-settings
 */
class LanguageRepositoryImpl(
    private val settings: Settings
) : LanguageRepository {

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        val SUPPORTED_LANGUAGES = listOf("ca", "es", "en", "de", "fr", "it")
    }

    private val _currentLanguage = MutableStateFlow(getSavedOrSystemLanguage())

    override suspend fun getCurrentLanguage(): String {
        return _currentLanguage.value
    }

    private fun getSavedOrSystemLanguage(): String {
        return if (settings.hasKey(KEY_LANGUAGE)) {
            val saved = settings.getString(KEY_LANGUAGE, "")
            if (saved in SUPPORTED_LANGUAGES) saved else getSystemLanguage()
        } else {
            getSystemLanguage()
        }
    }

    override fun observeCurrentLanguage(): Flow<String> {
        return _currentLanguage.asStateFlow()
    }

    override suspend fun setLanguage(languageCode: String) {
        require(languageCode in SUPPORTED_LANGUAGES) {
            "Unsupported language: $languageCode"
        }
        settings.putString(KEY_LANGUAGE, languageCode)
        _currentLanguage.value = languageCode
    }

    override fun getSystemLanguage(): String {
        val systemLang = getSystemLanguageCode()
        return if (systemLang in SUPPORTED_LANGUAGES) systemLang else "en"
    }
}
