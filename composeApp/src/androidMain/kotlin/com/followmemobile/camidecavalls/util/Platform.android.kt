package com.followmemobile.camidecavalls.util

import com.followmemobile.camidecavalls.BuildConfig
import java.util.Locale

actual fun getSystemLanguageCode(): String {
    return Locale.getDefault().language
}

actual fun getLocalhostUrl(): String = "http://10.0.2.2:3002"

actual fun getPlatformApiBaseUrl(): String = BuildConfig.API_BASE_URL

actual fun isPlatformDebugBuild(): Boolean = BuildConfig.IS_DEBUG_ENV
