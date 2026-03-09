package com.followmemobile.camidecavalls.util

/**
 * App environment configuration resolved at build time.
 *
 * - Android: debug build type → DEV, release build type → PRODUCTION
 *   (configured via buildConfigField in build.gradle.kts)
 *
 * - iOS: Debug Xcode configuration → DEV, Release → PRODUCTION
 *   (configured via User-Defined Build Settings in project.pbxproj)
 *
 * No manual switching needed — just build with the right variant/configuration.
 */
object AppConfig {
    /** Backend base URL for the current build environment */
    val baseUrl: String
        get() = getPlatformApiBaseUrl()

    /** Whether this is a development build */
    val isDebug: Boolean
        get() = isPlatformDebugBuild()

    /** Force POI sync on every app start (DEV only) */
    val forcePOISync: Boolean
        get() = isDebug

    /** Clear sync cache on app start (DEV only) */
    val clearSyncCache: Boolean
        get() = isDebug
}

/**
 * @deprecated Use [AppConfig] instead. Kept for backward compatibility.
 */
object DebugConfig {
    val FORCE_POI_SYNC get() = AppConfig.forcePOISync
    val CLEAR_SYNC_CACHE get() = AppConfig.clearSyncCache
    val DEV_BASE_URL get() = if (AppConfig.isDebug) AppConfig.baseUrl else ""
}
