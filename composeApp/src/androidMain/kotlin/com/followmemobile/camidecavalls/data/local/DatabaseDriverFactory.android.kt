package com.followmemobile.camidecavalls.data.local

import android.content.Context
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.followmemobile.camidecavalls.database.CamiDatabase

/**
 * Android implementation of DatabaseDriverFactory.
 * Database version 2: Added POI support with multilingual fields
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = CamiDatabase.Schema,
            context = context,
            name = "cami_database.db",
            callback = AndroidSqliteDriver.Callback(
                schema = CamiDatabase.Schema,
                AfterVersion(1) { driver ->
                    // Migration from version 1 to 2: Recreate POI table with correct schema
                    driver.execute(null, "DROP TABLE IF EXISTS PointOfInterestEntity", 0)
                    // Schema will be recreated by SQLDelight
                }
            )
        )
    }
}
