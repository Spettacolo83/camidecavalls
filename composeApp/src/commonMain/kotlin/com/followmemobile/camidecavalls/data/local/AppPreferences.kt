package com.followmemobile.camidecavalls.data.local

/**
 * Platform-specific preferences storage for app settings and metadata.
 */
expect class AppPreferences {
    /**
     * Get the stored database version.
     * @return Database version, or 0 if not set
     */
    fun getDatabaseVersion(): Int

    /**
     * Set the database version.
     */
    fun setDatabaseVersion(version: Int)
}
