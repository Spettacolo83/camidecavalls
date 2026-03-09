package com.followmemobile.camidecavalls.util

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getSystemLanguageCode(): String {
    return NSLocale.currentLocale.languageCode
}

actual fun getLocalhostUrl(): String = "http://localhost:3002"

actual fun getPlatformApiBaseUrl(): String {
    return NSBundle.mainBundle.objectForInfoDictionaryKey("API_BASE_URL") as? String
        ?: "https://camidecavalls.followtheflowai.com"
}

actual fun isPlatformDebugBuild(): Boolean {
    val value = NSBundle.mainBundle.objectForInfoDictionaryKey("IS_DEBUG_ENV") as? String
    return value == "true"
}
