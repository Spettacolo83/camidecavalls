package com.followmemobile.camidecavalls.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a route/stage (tappa) of the Camí de Cavalls.
 * Each of the 20 stages has its own characteristics.
 */
@Serializable
data class Route(
    val id: Int,
    val number: Int, // 1-20
    val name: String, // e.g., "Maó - es Grau"
    val startPoint: String,
    val endPoint: String,
    val distanceKm: Double,
    val elevationGainMeters: Int,
    val elevationLossMeters: Int,
    val maxAltitudeMeters: Int,
    val minAltitudeMeters: Int,
    val asphaltPercentage: Int,
    val difficulty: Difficulty,
    val estimatedDurationMinutes: Int,
    val description: String, // Default description (used as fallback)
    val gpxData: String? = null, // GPX track data
    val imageUrl: String? = null,
    // Multilingual descriptions
    val descriptionCa: String? = null,
    val descriptionEs: String? = null,
    val descriptionEn: String? = null,
    val descriptionDe: String? = null,
    val descriptionFr: String? = null,
    val descriptionIt: String? = null
) {
    /**
     * Get description in specified language, fallback to default description
     */
    fun getLocalizedDescription(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "ca" -> descriptionCa
            "es" -> descriptionEs
            "en" -> descriptionEn
            "de" -> descriptionDe
            "fr" -> descriptionFr
            "it" -> descriptionIt
            else -> null
        } ?: description
    }
}

enum class Difficulty {
    LOW,    // Baixa
    MEDIUM, // Mitjana
    HIGH    // Alta
}
