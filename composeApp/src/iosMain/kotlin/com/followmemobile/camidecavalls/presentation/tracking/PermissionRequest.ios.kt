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
