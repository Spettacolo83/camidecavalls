package com.followmemobile.camidecavalls.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Point of Interest (POI) along the Camí de Cavalls.
 * Supports multilingual content (Approach A: separate columns per language).
 */
@Serializable
data class PointOfInterest(
    val id: Int,
    val type: POIType,
    val latitude: Double,
    val longitude: Double,

    // Multilingual names (6 languages)
    val nameCa: String,
    val nameEs: String,
    val nameEn: String,  // English is default
    val nameFr: String,
    val nameDe: String,
    val nameIt: String,

    // Multilingual descriptions (6 languages)
    val descriptionCa: String,
    val descriptionEs: String,
    val descriptionEn: String,  // English is default
    val descriptionFr: String,
    val descriptionDe: String,
    val descriptionIt: String,

    // Image URL from official website
    val imageUrl: String,

    // Optional: Associated route if any
    val routeId: Int? = null,

    // For V2: commercial POIs
    val isAdvertisement: Boolean = false,

    // Action URL opened when user taps the action button
    val actionUrl: String? = null,

    // Multilingual action button text (6 languages)
    val actionButtonTextCa: String = "",
    val actionButtonTextEs: String = "",
    val actionButtonTextEn: String = "",
    val actionButtonTextFr: String = "",
    val actionButtonTextDe: String = "",
    val actionButtonTextIt: String = ""
) {
    /**
     * Get name in specified language
     */
    fun getName(language: Language): String {
        return when (language) {
            Language.CATALAN -> nameCa
            Language.SPANISH -> nameEs
            Language.ENGLISH -> nameEn
            Language.FRENCH -> nameFr
            Language.GERMAN -> nameDe
            Language.ITALIAN -> nameIt
        }
    }

    /**
     * Get description in specified language
     */
    fun getDescription(language: Language): String {
        return when (language) {
            Language.CATALAN -> descriptionCa
            Language.SPANISH -> descriptionEs
            Language.ENGLISH -> descriptionEn
            Language.FRENCH -> descriptionFr
            Language.GERMAN -> descriptionDe
            Language.ITALIAN -> descriptionIt
        }
    }

    /**
     * Get action button text in specified language.
     * Returns null if no action button text is set.
     */
    fun getActionButtonText(language: Language): String? {
        val text = when (language) {
            Language.CATALAN -> actionButtonTextCa
            Language.SPANISH -> actionButtonTextEs
            Language.ENGLISH -> actionButtonTextEn
            Language.FRENCH -> actionButtonTextFr
            Language.GERMAN -> actionButtonTextDe
            Language.ITALIAN -> actionButtonTextIt
        }
        return text.ifBlank { null }
    }
}

enum class POIType {
    BEACH,      // Zones costaneres (Blue marker - marker16.png)
    NATURAL,    // Espais protegits, flora, fauna (Green marker - marker17.png)
    HISTORIC,   // Torres, fars, fortificacions (Red marker - marker18.png)
    COMMERCIAL, // Bars, restaurants, shops
    DANGER      // Hazards, alerts on the trail
}
