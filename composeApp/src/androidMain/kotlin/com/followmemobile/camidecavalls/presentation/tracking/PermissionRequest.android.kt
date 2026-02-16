package com.followmemobile.camidecavalls.presentation.tracking

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Android implementation using ActivityResultContracts.
 * Returns a lambda that launches the permission request dialog.
 */
@Composable
actual fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if at least one of the required permissions was granted
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPermissionResult(granted)
    }

    return {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

/**
 * Android implementation for background location permission.
 * On Android 10+ (API 29+), ACCESS_BACKGROUND_LOCATION must be requested separately
 * AFTER foreground location is already granted.
 */
@Composable
actual fun rememberBackgroundPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            // Pre-Android 10: foreground location permission is sufficient
            onPermissionResult(true)
        }
    }
}

/**
 * Android implementation for notification permission.
 * On Android 13+ (API 33+), POST_NOTIFICATIONS must be requested at runtime.
 */
@Composable
actual fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Pre-Android 13: no runtime permission needed for notifications
            onPermissionResult(true)
        }
    }
}
