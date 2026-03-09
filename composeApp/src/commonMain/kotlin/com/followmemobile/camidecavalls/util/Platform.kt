package com.followmemobile.camidecavalls.util

/**
 * Get the system language code (e.g., "en", "ca", "es")
 */
expect fun getSystemLanguageCode(): String

/**
 * Localhost URL for dev.
 * Android emulator uses 10.0.2.2, iOS simulator uses localhost.
 */
expect fun getLocalhostUrl(): String
