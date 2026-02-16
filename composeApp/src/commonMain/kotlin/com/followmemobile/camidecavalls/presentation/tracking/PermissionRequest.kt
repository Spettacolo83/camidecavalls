package com.followmemobile.camidecavalls.presentation.tracking

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable for requesting location permissions.
 * Returns a lambda that can be called to request permissions.
 */
@Composable
expect fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit

/**
 * Platform-specific composable for requesting background location permission.
 * On Android 10+ (API 29+), ACCESS_BACKGROUND_LOCATION must be requested separately.
 * On iOS, this is a no-op (background mode is handled via CLLocationManager config).
 * Returns a lambda that can be called to request the permission.
 */
@Composable
expect fun rememberBackgroundPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit

/**
 * Platform-specific composable for requesting notification permission.
 * On Android 13+ (API 33+), POST_NOTIFICATIONS must be requested at runtime.
 * On iOS, this is a no-op (notifications not used for tracking).
 * Returns a lambda that can be called to request the permission.
 */
@Composable
expect fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit
