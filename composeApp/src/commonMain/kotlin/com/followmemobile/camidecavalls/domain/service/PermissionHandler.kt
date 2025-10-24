package com.followmemobile.camidecavalls.domain.service

/**
 * Platform-specific permission handler for location permissions.
 */
interface PermissionHandler {
    /**
     * Check if location permission is granted
     */
    fun isLocationPermissionGranted(): Boolean

    /**
     * Request location permission from the user.
     * On Android: Shows system permission dialog
     * On iOS: Requests when-in-use authorization
     */
    suspend fun requestLocationPermission(): Boolean

    /**
     * Check if we should show rationale for requesting permission (Android only)
     */
    fun shouldShowPermissionRationale(): Boolean
}
