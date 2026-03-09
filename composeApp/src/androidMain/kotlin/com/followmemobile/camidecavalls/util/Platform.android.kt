package com.followmemobile.camidecavalls.util

import java.util.Locale

actual fun getSystemLanguageCode(): String {
    return Locale.getDefault().language
}

actual fun getLocalhostUrl(): String = "http://10.0.2.2:3002"
