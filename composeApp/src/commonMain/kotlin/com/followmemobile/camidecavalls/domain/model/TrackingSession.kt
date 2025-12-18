package com.followmemobile.camidecavalls.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain model representing a tracking session (user's hike/run).
 * Stored locally in the Notebook.
 */
@Serializable
data class TrackingSession(
    val id: String,
    val routeId: Int?,
    val startTime: Instant,
    val endTime: Instant?,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0,
    val averageSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val elevationGainMeters: Int = 0,
    val elevationLossMeters: Int = 0,
    val trackPoints: List<TrackPoint> = emptyList(),
    val isCompleted: Boolean = false,
    val name: String = "",
    val notes: String = ""
)

@Serializable
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestamp: Instant,
    val speedKmh: Double? = null
)
