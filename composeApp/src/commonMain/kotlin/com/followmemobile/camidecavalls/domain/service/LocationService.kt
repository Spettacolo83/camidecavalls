package com.followmemobile.camidecavalls.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Location data model
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float?,
    val speed: Float?,
    val bearing: Float?,
    val timestamp: Long
)

/**
 * Location tracking configuration for battery optimization
 */
data class LocationConfig(
    /**
     * Minimum time interval between location updates in milliseconds.
     * Higher values = better battery life.
     * Recommended: 5000ms (5 seconds) for active tracking
     */
    val updateIntervalMs: Long = 5000L,

    /**
     * Fastest rate for location updates in milliseconds.
     * This is the absolute maximum frequency.
     * Recommended: 2000ms (2 seconds)
     */
    val fastestIntervalMs: Long = 2000L,

    /**
     * Minimum distance between location updates in meters.
     * Device won't update location if user hasn't moved this far.
     * Set to 0 to receive all updates regardless of movement (for testing/debugging).
     * For production: 5-10 meters recommended for hiking trails.
     */
    val minDistanceMeters: Float = 0f,

    /**
     * Priority for location accuracy vs battery consumption.
     * HIGH_ACCURACY is required for hiking trails to get GPS precision.
     * Uses more battery but essential for accurate track recording.
     */
    val priority: LocationPriority = LocationPriority.HIGH_ACCURACY
)

/**
 * Location accuracy priority
 */
enum class LocationPriority {
    /**
     * High accuracy - uses GPS, consumes most battery.
     * Use only when precise positioning is critical.
     */
    HIGH_ACCURACY,

    /**
     * Balanced power and accuracy - uses GPS + Network.
     * Recommended for trekking: good accuracy with reasonable battery consumption.
     */
    BALANCED,

    /**
     * Low power - uses WiFi and cell towers, less accurate.
     * Use when rough position is sufficient.
     */
    LOW_POWER
}

/**
 * Platform-specific location service.
 * Implementations should optimize for battery life and work offline (GPS only).
 */
interface LocationService {
    /**
     * Flow of location updates.
     * Emits null if location services are disabled or permission denied.
     */
    val locationUpdates: Flow<LocationData?>

    /**
     * Check if location services are enabled on the device
     */
    fun isLocationEnabled(): Boolean

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean

    /**
     * Start receiving location updates with the given configuration.
     * Uses battery-optimized settings by default.
     */
    suspend fun startTracking(config: LocationConfig = LocationConfig())

    /**
     * Stop receiving location updates to save battery
     */
    suspend fun stopTracking()

    /**
     * Get last known location (cached, doesn't consume battery)
     */
    suspend fun getLastKnownLocation(): LocationData?
}
