package com.followmemobile.camidecavalls.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Point of Interest (POI) along the Camí de Cavalls.
 * Can be natural attractions or historic sites.
 */
@Serializable
data class PointOfInterest(
    val id: Int,
    val name: String,
    val type: POIType,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val images: List<String> = emptyList(),
    val routeId: Int? = null, // Associated route if any
    val isAdvertisement: Boolean = false // For V2: commercial POIs
)

enum class POIType {
    NATURAL,    // Espais protegits, flora, fauna
    HISTORIC,   // Torres, fars, fortificacions
    COMMERCIAL  // For V2: Bars, restaurants, shops
}
