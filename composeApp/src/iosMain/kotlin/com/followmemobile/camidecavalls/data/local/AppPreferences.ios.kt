package com.followmemobile.camidecavalls.data.local

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of AppPreferences using NSUserDefaults.
 */
actual class AppPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getDatabaseVersion(): Int {
        return defaults.integerForKey(KEY_DATABASE_VERSION).toInt()
    }

    actual fun setDatabaseVersion(version: Int) {
        defaults.setInteger(version.toLong(), KEY_DATABASE_VERSION)
    }

    companion object {
        private const val KEY_DATABASE_VERSION = "database_version"
    }
}
