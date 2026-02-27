package com.followmemobile.camidecavalls.domain.usecase.tracking

import com.followmemobile.camidecavalls.domain.model.Language
import com.followmemobile.camidecavalls.domain.model.PointOfInterest
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import com.followmemobile.camidecavalls.domain.service.LocalNotificationManager
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Data class for a nearby POI with its computed distance.
 */
data class NearbyPoi(
    val poi: PointOfInterest,
    val distanceMeters: Double
)

/**
 * Manages POI proximity detection during GPS tracking.
 * Observes location updates, queries nearby POIs, and triggers notifications.
 */
class PoiProximityManager(
    private val poiRepository: POIRepository,
    private val localNotificationManager: LocalNotificationManager,
    private val languageRepository: LanguageRepository,
    private val settings: Settings,
    private val scope: CoroutineScope
) {
    companion object {
        const val MAP_VISIBILITY_RADIUS = 1500.0 // meters
        const val DEFAULT_NOTIFICATION_RADIUS = 500.0 // meters
        const val MAX_NEARBY_POIS = 3
        const val DEFAULT_MAX_VISIBLE_POIS = 3
        const val MIN_MOVEMENT_METERS = 50.0
        const val SETTINGS_KEY_NOTIFICATION_RADIUS = "poi_notification_radius"
        const val SETTINGS_KEY_NOTIFICATIONS_ENABLED = "poi_notifications_enabled"
        const val SETTINGS_KEY_MAX_VISIBLE_POIS = "poi_max_visible"
    }

    private val _nearbyPois = MutableStateFlow<List<NearbyPoi>>(emptyList())
    val nearbyPois: StateFlow<List<NearbyPoi>> = _nearbyPois.asStateFlow()

    private val notifiedPoiIds = mutableSetOf<Int>()
    private var lastCheckLocation: LocationData? = null
    private var observeJob: Job? = null

    /**
     * Start observing location updates for POI proximity.
     * Resets all session state.
     */
    fun startObserving(locationFlow: Flow<LocationData?>) {
        stopObserving()
        notifiedPoiIds.clear()
        lastCheckLocation = null
        _nearbyPois.value = emptyList()

        observeJob = scope.launch {
            locationFlow.collect { location ->
                if (location != null) {
                    onLocationUpdate(location)
                }
            }
        }
    }

    /**
     * Stop observing and clear all state.
     */
    fun stopObserving() {
        observeJob?.cancel()
        observeJob = null
        _nearbyPois.value = emptyList()
        notifiedPoiIds.clear()
        lastCheckLocation = null
    }

    private suspend fun onLocationUpdate(location: LocationData) {
        val lastLoc = lastCheckLocation
        if (lastLoc != null) {
            val moved = haversineDistance(
                lastLoc.latitude, lastLoc.longitude,
                location.latitude, location.longitude
            )
            if (moved < MIN_MOVEMENT_METERS) return
        }
        lastCheckLocation = location
        checkProximity(location)
    }

    private suspend fun checkProximity(location: LocationData) {
        val allNearby = poiRepository.getPOIsNearLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusMeters = MAP_VISIBILITY_RADIUS
        )

        val withDistance = allNearby.map { poi ->
            NearbyPoi(
                poi = poi,
                distanceMeters = haversineDistance(
                    location.latitude, location.longitude,
                    poi.latitude, poi.longitude
                )
            )
        }.sortedBy { it.distanceMeters }
            .take(settings.getInt(SETTINGS_KEY_MAX_VISIBLE_POIS, DEFAULT_MAX_VISIBLE_POIS))

        _nearbyPois.value = withDistance

        // Check notifications
        val notificationsEnabled = settings.getBoolean(SETTINGS_KEY_NOTIFICATIONS_ENABLED, true)
        if (!notificationsEnabled) return
        if (!localNotificationManager.hasNotificationPermission()) return

        val notificationRadius = settings.getInt(SETTINGS_KEY_NOTIFICATION_RADIUS, DEFAULT_NOTIFICATION_RADIUS.toInt()).toDouble()
        val languageCode = languageRepository.getCurrentLanguage()
        val language = Language.fromCode(languageCode)
        val strings = LocalizedStrings(languageCode)

        for (nearby in withDistance) {
            if (nearby.distanceMeters <= notificationRadius && nearby.poi.id !in notifiedPoiIds) {
                notifiedPoiIds.add(nearby.poi.id)

                val poiName = nearby.poi.getName(language)
                val poiTypeName = getPoiTypeName(nearby.poi, strings)
                val distanceStr = formatDistance(nearby.distanceMeters)

                localNotificationManager.showPoiNotification(
                    notificationId = nearby.poi.id,
                    title = poiName,
                    body = strings.poiProximityBody(poiTypeName, distanceStr)
                )
            }
        }
    }

    private fun getPoiTypeName(poi: PointOfInterest, strings: LocalizedStrings): String {
        return when (poi.type) {
            com.followmemobile.camidecavalls.domain.model.POIType.BEACH -> strings.poiTypeBeach
            com.followmemobile.camidecavalls.domain.model.POIType.NATURAL -> strings.poiTypeNatural
            com.followmemobile.camidecavalls.domain.model.POIType.HISTORIC -> strings.poiTypeHistoric
            com.followmemobile.camidecavalls.domain.model.POIType.COMMERCIAL -> strings.poiTypeCommercial
        }
    }

    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            val km = (meters / 100).toInt() / 10.0
            "$km km"
        } else {
            "${meters.toInt()} m"
        }
    }

    private fun haversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2).pow(2.0) +
                cos(lat1 * PI / 180.0) *
                cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * asin(sqrt(a))
        return earthRadiusMeters * c
    }
}
