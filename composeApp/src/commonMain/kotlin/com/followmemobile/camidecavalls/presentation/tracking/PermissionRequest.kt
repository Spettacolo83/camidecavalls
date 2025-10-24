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
