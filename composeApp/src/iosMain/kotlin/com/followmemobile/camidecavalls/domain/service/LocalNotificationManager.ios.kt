package com.followmemobile.camidecavalls.domain.service

import com.followmemobile.camidecavalls.presentation.main.PoiNavigationManager
import platform.Foundation.NSNumber
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

/**
 * Internal delegate that extends NSObject and implements UNUserNotificationCenterDelegateProtocol.
 * Separated from the actual class to avoid expect/actual + NSObject hierarchy issues in K/N.
 */
private class NotificationCenterDelegate(
    private val poiNavigationManager: PoiNavigationManager
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    // Show notification banner + sound even when app is in foreground
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionSound)
    }

    // Handle notification tap — extract poiId and navigate
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        val poiId = (userInfo["poiId"] as? NSNumber)?.intValue
        if (poiId != null && poiId > 0) {
            poiNavigationManager.navigateToPoi(poiId)
        }
        withCompletionHandler()
    }
}

/**
 * iOS implementation of LocalNotificationManager.
 * Uses UNUserNotificationCenter for immediate local notifications.
 * Sets up a delegate for foreground display and tap handling.
 */
actual class LocalNotificationManager(
    private val poiNavigationManager: PoiNavigationManager
) {
    // Hold strong reference to delegate (UNUserNotificationCenter.delegate is weak)
    private val delegate = NotificationCenterDelegate(poiNavigationManager)

    init {
        UNUserNotificationCenter.currentNotificationCenter().delegate = delegate
    }

    actual fun showPoiNotification(notificationId: Int, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setUserInfo(mapOf("poiId" to NSNumber(int = notificationId)))
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "poi-proximity-$notificationId",
            content = content,
            trigger = null // Immediate delivery
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }

    actual fun hasNotificationPermission(): Boolean {
        // iOS silently drops notifications if not granted; return true to allow attempt
        return true
    }
}
