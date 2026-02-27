package com.followmemobile.camidecavalls.domain.service

/**
 * Platform-specific notification manager for POI proximity alerts.
 * On Android: uses NotificationManager with a dedicated channel.
 * On iOS: uses UNUserNotificationCenter for immediate delivery.
 */
expect class LocalNotificationManager {
    fun showPoiNotification(notificationId: Int, title: String, body: String)
    fun hasNotificationPermission(): Boolean
}
