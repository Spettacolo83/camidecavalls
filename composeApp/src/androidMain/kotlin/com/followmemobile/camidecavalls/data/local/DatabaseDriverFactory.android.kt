package com.followmemobile.camidecavalls.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.followmemobile.camidecavalls.database.CamiDatabase

/**
 * Android implementation of DatabaseDriverFactory.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = CamiDatabase.Schema,
            context = context,
            name = "cami_database.db"
        )
    }
}
