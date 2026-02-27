package com.followmemobile.camidecavalls.presentation.tracking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * iOS implementation using PermissionHandler.
 * Returns a lambda that requests permission using CLLocationManager.
 */
@Composable
actual fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val permissionHandler: PermissionHandler = koinInject()
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            val granted = permissionHandler.requestLocationPermission()
            onPermissionResult(granted)
        }
    }
}

/**
 * iOS: Background location is handled via CLLocationManager configuration
 * (allowsBackgroundLocationUpdates = true). "When In Use" + UIBackgroundModes "location"
 * is sufficient. Always report as granted.
 */
@Composable
actual fun rememberBackgroundPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    return {
        // iOS handles background via CLLocationManager config, no separate permission needed
        onPermissionResult(true)
    }
}

/**
 * iOS: Request notification permission for POI proximity alerts.
 * Uses UNUserNotificationCenter to request Alert + Sound authorization.
 */
@Composable
actual fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    return {
        val center = platform.UserNotifications.UNUserNotificationCenter.currentNotificationCenter()
        val options = platform.UserNotifications.UNAuthorizationOptionAlert or
                platform.UserNotifications.UNAuthorizationOptionSound
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            onPermissionResult(granted)
        }
    }
}
