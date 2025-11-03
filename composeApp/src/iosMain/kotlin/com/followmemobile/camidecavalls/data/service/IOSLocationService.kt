package com.followmemobile.camidecavalls.data.service

import com.followmemobile.camidecavalls.domain.service.LocationConfig
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.LocationPriority
import com.followmemobile.camidecavalls.domain.service.LocationService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.datetime.Clock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * iOS implementation of LocationService using CoreLocation.
 * Optimized for battery consumption and works offline (GPS only).
 */
@OptIn(ExperimentalForeignApi::class)
class IOSLocationService : LocationService {

    private val locationManager = CLLocationManager()
    private var locationDelegate: LocationDelegate? = null
    private var currentConfig: LocationConfig? = null

    override val locationUpdates: Flow<LocationData?> = callbackFlow {
        val delegate = LocationDelegate(
            onLocationUpdate = { location ->
                trySend(location)
            },
            onAuthorizationChange = { authorized ->
                if (!authorized) {
                    trySend(null)
                }
            }
        )

        locationDelegate = delegate
        locationManager.delegate = delegate

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
            locationDelegate = null
        }
    }

    override fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

    override fun hasLocationPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }

    override suspend fun startTracking(config: LocationConfig) {
        currentConfig = config

        // Request permission if not determined
        // Request "Always" authorization for background tracking
        val authStatus = CLLocationManager.authorizationStatus()
        if (authStatus == kCLAuthorizationStatusNotDetermined) {
            // First request "When In Use", then the user can upgrade to "Always" later
            locationManager.requestWhenInUseAuthorization()
            // Note: The delegate will be called when user responds, no need to check immediately
        } else if (authStatus == kCLAuthorizationStatusDenied ||
                   authStatus == kCLAuthorizationStatusRestricted) {
            throw IllegalStateException("Location permission denied. Please enable location services in Settings.")
        }

        // Configure location manager for battery optimization
        locationManager.apply {
            // Set desired accuracy based on priority
            desiredAccuracy = mapAccuracy(config.priority)

            // Distance filter: minimum distance (in meters) a device must move before update
            // Higher values = better battery life
            distanceFilter = config.minDistanceMeters.toDouble()

            // Pause location updates automatically when the location is unlikely to change
            // This saves significant battery
            pausesLocationUpdatesAutomatically = true

            // Only show blue bar when actually using location (better UX and battery)
            showsBackgroundLocationIndicator = false

            // Enable background location updates for continuous tracking
            allowsBackgroundLocationUpdates = true

            // Activity type for better battery management
            // CLActivityTypeFitness is optimal for hiking/trekking
            activityType = platform.CoreLocation.CLActivityTypeFitness
        }

        // Start location updates - will work once permission is granted
        locationManager.startUpdatingLocation()
    }

    override suspend fun stopTracking() {
        locationManager.stopUpdatingLocation()
        currentConfig = null
    }

    override suspend fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return locationManager.location?.let { location ->
            toLocationData(location)
        }
    }

    /**
     * Map our LocationPriority to iOS CLLocationAccuracy
     */
    private fun mapAccuracy(priority: LocationPriority): Double = when (priority) {
        LocationPriority.HIGH_ACCURACY -> kCLLocationAccuracyBest
        LocationPriority.BALANCED -> kCLLocationAccuracyNearestTenMeters
        LocationPriority.LOW_POWER -> kCLLocationAccuracyHundredMeters
    }

    /**
     * Convert CLLocation to our LocationData model
     * Note: Removed accuracy checks to be consistent with Android behavior
     * iOS sets negative accuracy values when data is invalid/unavailable
     */
    private fun toLocationData(location: CLLocation): LocationData {
        return location.coordinate.useContents {
            LocationData(
                latitude = latitude,
                longitude = longitude,
                // Always include altitude (consistent with Android)
                altitude = location.altitude,
                accuracy = if (location.horizontalAccuracy >= 0) location.horizontalAccuracy.toFloat() else null,
                // ALWAYS include speed (0 if negative, consistent with Android)
                speed = maxOf(0f, location.speed.toFloat()),
                // Always include bearing when available
                bearing = if (location.course >= 0) location.course.toFloat() else null,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    /**
     * Delegate for handling location updates and authorization changes
     */
    private inner class LocationDelegate(
        private val onLocationUpdate: (LocationData) -> Unit,
        private val onAuthorizationChange: (Boolean) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            (didUpdateLocations.lastOrNull() as? CLLocation)?.let { location ->
                onLocationUpdate(toLocationData(location))
            }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            println("Location error: ${didFailWithError.localizedDescription}")
        }

        override fun locationManager(
            manager: CLLocationManager,
            didChangeAuthorizationStatus: CLAuthorizationStatus
        ) {
            val authorized = didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedAlways
            onAuthorizationChange(authorized)
        }
    }
}
