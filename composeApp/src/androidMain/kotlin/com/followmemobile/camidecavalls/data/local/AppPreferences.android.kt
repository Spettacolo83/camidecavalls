package com.followmemobile.camidecavalls.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of AppPreferences using SharedPreferences.
 */
actual class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "cami_app_prefs",
        Context.MODE_PRIVATE
    )

    actual fun getDatabaseVersion(): Int {
        return prefs.getInt(KEY_DATABASE_VERSION, 0)
    }

    actual fun setDatabaseVersion(version: Int) {
        prefs.edit().putInt(KEY_DATABASE_VERSION, version).apply()
    }

    actual fun getPOIVersion(): Int {
        return prefs.getInt(KEY_POI_VERSION, 0)
    }

    actual fun setPOIVersion(version: Int) {
        prefs.edit().putInt(KEY_POI_VERSION, version).apply()
    }

    companion object {
        private const val KEY_DATABASE_VERSION = "database_version"
        private const val KEY_POI_VERSION = "poi_version"
    }
}
