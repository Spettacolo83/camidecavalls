package com.followmemobile.camidecavalls.data.local

import com.followmemobile.camidecavalls.database.CamiDatabase

/**
 * Wrapper class for the SQLDelight database.
 * Provides access to all database queries.
 */
class CamiDatabaseWrapper(
    databaseDriverFactory: DatabaseDriverFactory
) {
    private val _driver = databaseDriverFactory.createDriver()
    private val database = CamiDatabase(_driver)

    val routeQueries = database.routeQueries
    val poiQueries = database.pointOfInterestQueries
    val trackingSessionQueries = database.trackingSessionQueries
    val trackPointQueries = database.trackPointQueries

    // Expose driver for raw SQL operations (e.g., migrations)
    val driver get() = _driver
}
