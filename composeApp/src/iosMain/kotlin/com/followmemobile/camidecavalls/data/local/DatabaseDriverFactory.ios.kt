package com.followmemobile.camidecavalls.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.followmemobile.camidecavalls.database.CamiDatabase

/**
 * iOS implementation of DatabaseDriverFactory.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = CamiDatabase.Schema,
            name = "cami_database.db"
        )
    }
}
