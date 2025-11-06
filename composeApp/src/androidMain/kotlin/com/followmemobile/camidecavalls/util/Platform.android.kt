package com.followmemobile.camidecavalls.util

import java.util.Locale

actual fun getSystemLanguageCode(): String {
    return Locale.getDefault().language
}
