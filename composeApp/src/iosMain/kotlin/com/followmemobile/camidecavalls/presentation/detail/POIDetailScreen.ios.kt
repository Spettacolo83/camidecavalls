package com.followmemobile.camidecavalls.presentation.detail

import platform.Foundation.NSURL
import platform.Foundation.NSString
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation to open a URL in the default browser
 */
actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    dispatch_async(dispatch_get_main_queue()) {
        UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>()) { success ->
            if (!success) {
                println("Failed to open URL: $url")
            }
        }
    }
}

/**
 * iOS implementation to open coordinates in Apple Maps
 */
actual fun openInMaps(latitude: Double, longitude: Double, name: String) {
    // Encode the name for URL safety
    @Suppress("CAST_NEVER_SUCCEEDS")
    val encodedName = (name as NSString)
        .stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: name.replace(" ", "+")

    val urlString = "maps://?q=$encodedName&ll=$latitude,$longitude"
    val url = NSURL.URLWithString(urlString) ?: return

    dispatch_async(dispatch_get_main_queue()) {
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) { success ->
            if (!success) {
                println("Failed to open Maps URL: $urlString")
            }
        }
    }
}
