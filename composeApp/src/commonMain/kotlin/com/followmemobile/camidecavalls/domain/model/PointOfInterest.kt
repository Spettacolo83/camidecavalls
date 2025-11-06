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
    val isAdvertisement: Boolean = false
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
}

enum class POIType {
    BEACH,      // Platges i cales (Blue marker - marker16.png)
    NATURAL,    // Espais protegits, flora, fauna (Green marker - marker17.png)
    HISTORIC,   // Torres, fars, fortificacions (Red marker - marker18.png)
    COMMERCIAL  // For V2: Bars, restaurants, shops
}
