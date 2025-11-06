package com.followmemobile.camidecavalls.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app language preferences
 */
interface LanguageRepository {
    /**
     * Get current language code (e.g., "en", "ca", "es")
     * Returns system default if not set
     */
    suspend fun getCurrentLanguage(): String

    /**
     * Observe current language changes
     */
    fun observeCurrentLanguage(): Flow<String>

    /**
     * Set current language
     */
    suspend fun setLanguage(languageCode: String)

    /**
     * Get system default language
     */
    fun getSystemLanguage(): String
}
