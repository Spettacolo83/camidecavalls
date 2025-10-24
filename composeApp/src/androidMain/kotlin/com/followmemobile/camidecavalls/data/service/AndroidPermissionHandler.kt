package com.followmemobile.camidecavalls.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of PermissionHandler.
 * Note: requestLocationPermission() should be called from a composable context
 * using rememberLauncherForActivityResult.
 */
class AndroidPermissionHandler(
    private val context: Context
) : PermissionHandler {

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestLocationPermission(): Boolean {
        // This method is a placeholder - actual permission request
        // must be done from a Composable using rememberLauncherForActivityResult
        return isLocationPermissionGranted()
    }

    override fun shouldShowPermissionRationale(): Boolean {
        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
