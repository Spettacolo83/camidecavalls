package com.followmemobile.camidecavalls.domain.model

/**
 * Supported languages in the app
 *
 * Default: English (en)
 */
enum class Language(
    val code: String,
    val nativeName: String
) {
    ENGLISH("en", "English"),
    CATALAN("ca", "Català"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    ITALIAN("it", "Italiano");

    companion object {
        /**
         * Default language (English)
         */
        val DEFAULT = ENGLISH

        /**
         * Get language from code, or return default if not found
         */
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code == code } ?: DEFAULT
        }

        /**
         * All available languages
         */
        fun all(): List<Language> = entries
    }
}
