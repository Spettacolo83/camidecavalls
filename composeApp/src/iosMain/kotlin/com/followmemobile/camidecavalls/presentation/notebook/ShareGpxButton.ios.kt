package com.followmemobile.camidecavalls.presentation.notebook

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ShareGpxButton(
    gpxContent: String,
    fileName: String,
    strings: LocalizedStrings
) {
    FilledTonalButton(
        onClick = {
            // Sanitize filename - replace invalid characters
            val safeFileName = fileName
                .replace("/", "-")
                .replace("\\", "-")
                .replace(":", "-")

            // Dispatch file writing to background queue to avoid blocking UI
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
                // Write GPX content to temporary file
                val tempDir = NSTemporaryDirectory()
                val filePath = "$tempDir$safeFileName"

                // Convert Kotlin String to NSData and write to file
                val data = gpxContent.encodeToByteArray().toNSData()
                val success = data?.writeToFile(filePath, atomically = true) ?: false

                if (success) {
                    val fileUrl = NSURL.fileURLWithPath(filePath)

                    // Present share sheet on main queue
                    dispatch_async(dispatch_get_main_queue()) {
                        val activityVC = UIActivityViewController(
                            activityItems = listOf(fileUrl),
                            applicationActivities = null
                        )

                        // Get the topmost view controller
                        val topViewController = getTopViewController()
                        topViewController?.presentViewController(activityVC, animated = true, completion = null)
                    }
                }
            }
        }
    ) {
        Icon(Icons.Default.Share, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(strings.sessionShare)
    }
}

/**
 * Get the topmost view controller in the hierarchy.
 * This finds the currently visible view controller, even if modals are presented.
 */
@Suppress("DEPRECATION")
private fun getTopViewController(): UIViewController? {
    val rootVC = getRootViewController() ?: return null
    return findTopViewController(rootVC)
}

/**
 * Recursively find the topmost presented view controller.
 */
private fun findTopViewController(viewController: UIViewController): UIViewController {
    // If there's a presented view controller, go deeper
    viewController.presentedViewController?.let { presented ->
        return findTopViewController(presented)
    }
    return viewController
}

/**
 * Get the root view controller using the modern scene-based API.
 * Falls back to deprecated keyWindow for older iOS versions.
 */
@Suppress("DEPRECATION")
private fun getRootViewController(): UIViewController? {
    // Try to get from connected scenes (iOS 13+)
    val scenes = UIApplication.sharedApplication.connectedScenes
    for (scene in scenes) {
        if (scene is UIWindowScene) {
            // Find the key window in this scene
            val windows = scene.windows
            for (window in windows) {
                if (window is UIWindow && window.isKeyWindow()) {
                    return window.rootViewController
                }
            }
            // If no key window found, try the first window
            val firstWindow = windows.firstOrNull() as? UIWindow
            if (firstWindow != null) {
                return firstWindow.rootViewController
            }
        }
    }

    // Fallback to deprecated API for older iOS
    return UIApplication.sharedApplication.keyWindow?.rootViewController
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (this.isEmpty()) return null
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
