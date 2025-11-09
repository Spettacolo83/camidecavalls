package com.followmemobile.camidecavalls.presentation.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation to open coordinates in Google Maps
 */
actual fun openInMaps(latitude: Double, longitude: Double, name: String) {
    val context = AndroidContextProvider.context

    // Create Google Maps URI with coordinates and label
    // Format: geo:latitude,longitude?q=latitude,longitude(Label)
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(name)})")

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        // Request to open in Google Maps if available
        setPackage("com.google.android.apps.maps")
        // Add FLAG_ACTIVITY_NEW_TASK since we're starting from outside an Activity context
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    // Check if Google Maps is installed
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        println("✅ Opened Google Maps for: $name at $latitude, $longitude")
    } else {
        // Fallback: Open in any map app that can handle geo URIs
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
        println("✅ Opened maps for: $name at $latitude, $longitude")
    }
}

/**
 * Helper object to provide Android Context using Koin
 */
private object AndroidContextProvider : KoinComponent {
    val context: Context by inject()
}
