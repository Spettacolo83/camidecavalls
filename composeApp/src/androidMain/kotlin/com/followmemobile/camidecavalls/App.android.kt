package com.followmemobile.camidecavalls

import androidx.compose.runtime.Composable

/**
 * Android implementation of KoinInitializer.
 * Koin is already initialized in CamiApp (Application class), so this is just a passthrough.
 */
@Composable
actual fun KoinInitializer(content: @Composable () -> Unit) {
    content()
}
