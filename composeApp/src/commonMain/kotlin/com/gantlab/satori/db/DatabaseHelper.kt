package com.gantlab.satori.db

import app.cash.sqldelight.db.SqlDriver

class DatabaseHelper(
    private val driverFactory: DriverFactory
) {
    private var database: SatoriDatabase? = null

    private fun getDatabase(): SatoriDatabase {
        if (database == null) {
            database = createDatabase(driverFactory)
        }
        return database!!
    }

    fun getResults(): List<ReactionResult> {
        return getDatabase().satoriDatabaseQueries.selectAllResults().executeAsList()
    }

    fun insertResult(timestamp: Long, reactionTimeMs: Long) {
        getDatabase().satoriDatabaseQueries.insertResult(timestamp, reactionTimeMs)
    }
}
