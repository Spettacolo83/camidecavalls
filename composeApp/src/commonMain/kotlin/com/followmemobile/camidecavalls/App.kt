package com.followmemobile.camidecavalls

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.followmemobile.camidecavalls.presentation.home.HomeScreen

@Composable
fun App() {
    KoinInitializer {
        MaterialTheme {
            Navigator(HomeScreen())
        }
    }
}

/**
 * Platform-specific Koin initialization wrapper.
 * On Android: Koin is already initialized in Application class, so this is just a passthrough.
 * On iOS: Koin is initialized here using KoinApplication.
 */
@Composable
expect fun KoinInitializer(content: @Composable () -> Unit)
