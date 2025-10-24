package com.followmemobile.camidecavalls.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.followmemobile.camidecavalls.domain.service.LocationConfig
import com.followmemobile.camidecavalls.domain.service.LocationData
import com.followmemobile.camidecavalls.domain.service.LocationPriority
import com.followmemobile.camidecavalls.domain.service.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of LocationService using FusedLocationProviderClient.
 * Optimized for battery consumption and works offline (GPS only).
 */
class AndroidLocationService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    private var locationCallback: LocationCallback? = null

    override val locationUpdates: Flow<LocationData?> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = if (location.hasAltitude()) location.altitude else null,
                            accuracy = if (location.hasAccuracy()) location.accuracy else null,
                            speed = if (location.hasSpeed()) location.speed else null,
                            bearing = if (location.hasBearing()) location.bearing else null,
                            timestamp = location.time
                        )
                    )
                }
            }
        }

        locationCallback = callback

        awaitClose {
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (e: Exception) {
                // Ignore if already stopped
            }
            locationCallback = null
        }
    }

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun startTracking(config: LocationConfig) {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        val locationRequest = LocationRequest.Builder(
            mapPriority(config.priority),
            config.updateIntervalMs
        ).apply {
            setMinUpdateIntervalMillis(config.fastestIntervalMs)
            setMinUpdateDistanceMeters(config.minDistanceMeters)

            // Battery optimization: Use coarse granularity for balanced/low power modes
            // FINE granularity only for high accuracy (uses more battery)
            setGranularity(
                if (config.priority == LocationPriority.HIGH_ACCURACY) {
                    Granularity.GRANULARITY_FINE
                } else {
                    Granularity.GRANULARITY_COARSE
                }
            )

            // Wait for accurate location before delivering
            // Reduces battery by avoiding delivering inaccurate locations
            setWaitForAccurateLocation(true)

            // Maximum wait time for a location (battery optimization)
            setMaxUpdateDelayMillis(config.updateIntervalMs * 2)
        }.build()

        locationCallback?.let { callback ->
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                throw SecurityException("Location permission not granted")
            }
        }
    }

    override suspend fun stopTracking() {
        locationCallback?.let { callback ->
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (e: Exception) {
                // Ignore if already stopped
            }
        }
    }

    override suspend fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            suspendCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        val locationData = location?.let {
                            LocationData(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                altitude = if (it.hasAltitude()) it.altitude else null,
                                accuracy = if (it.hasAccuracy()) it.accuracy else null,
                                speed = if (it.hasSpeed()) it.speed else null,
                                bearing = if (it.hasBearing()) it.bearing else null,
                                timestamp = it.time
                            )
                        }
                        continuation.resume(locationData)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * Map our LocationPriority to Google Play Services Priority
     */
    private fun mapPriority(priority: LocationPriority): Int = when (priority) {
        LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
        LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
    }
}
