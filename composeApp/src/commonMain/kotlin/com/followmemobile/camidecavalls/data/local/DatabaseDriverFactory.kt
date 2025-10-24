package com.followmemobile.camidecavalls.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect/Actual pattern for database driver creation.
 * Platform-specific implementations in androidMain and iosMain.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
