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
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of LocationService using FusedLocationProviderClient.
 * Optimized for battery consumption and works offline (GPS only).
 *
 * For emulator testing, use GPX files with timestamps in Extended Controls > Location.
 * GPX files are available in test-routes/android/emulator/
 */
class AndroidLocationService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    private val _locationFlow = kotlinx.coroutines.flow.MutableStateFlow<LocationData?>(null)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = if (location.hasAccuracy()) location.accuracy else null,
                    speed = if (location.hasSpeed()) location.speed else null,
                    bearing = if (location.hasBearing()) location.bearing else null,
                    timestamp = location.time
                )
                _locationFlow.value = locationData
            }
        }
    }

    override val locationUpdates: Flow<LocationData?> = _locationFlow

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

            // For hiking: always use FINE granularity for GPS precision
            setGranularity(Granularity.GRANULARITY_FINE)

            // Don't wait - deliver location immediately for faster initial fix
            setWaitForAccurateLocation(false)

            // Maximum wait time for a location
            setMaxUpdateDelayMillis(config.updateIntervalMs * 2)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Try to get last location immediately for faster initial fix
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val locationData = LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = if (it.hasAltitude()) it.altitude else null,
                        accuracy = if (it.hasAccuracy()) it.accuracy else null,
                        speed = if (it.hasSpeed()) it.speed else null,
                        bearing = if (it.hasBearing()) it.bearing else null,
                        timestamp = it.time
                    )
                    _locationFlow.value = locationData
                }
            }
        } catch (e: SecurityException) {
            throw SecurityException("Location permission not granted")
        }
    }

    override suspend fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
