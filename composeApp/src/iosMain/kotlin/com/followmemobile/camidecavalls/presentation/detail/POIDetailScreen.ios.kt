package com.followmemobile.camidecavalls.presentation.detail

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation to open coordinates in Apple Maps
 */
@OptIn(ExperimentalForeignApi::class)
actual fun openInMaps(latitude: Double, longitude: Double, name: String) {
    // Create Apple Maps URL with coordinates and name
    // Format: maps://?q=Name&ll=latitude,longitude
    val urlString = "maps://?q=${name.replace(" ", "+")}&ll=$latitude,$longitude"

    val url = NSURL.URLWithString(urlString)

    url?.let {
        if (UIApplication.sharedApplication.canOpenURL(it)) {
            UIApplication.sharedApplication.openURL(it)
        } else {
            println("❌ Cannot open Maps URL: $urlString")
        }
    }
}
