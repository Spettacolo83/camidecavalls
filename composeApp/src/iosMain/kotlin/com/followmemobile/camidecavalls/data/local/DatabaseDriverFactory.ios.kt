package com.followmemobile.camidecavalls.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.followmemobile.camidecavalls.database.CamiDatabase

/**
 * iOS implementation of DatabaseDriverFactory.
 * Database version 2: Added POI support with multilingual fields
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            CamiDatabase.Schema,
            "cami_database.db"
        )
    }
}
