package com.gantlab.satori.db

import kotlinx.datetime.Clock

class SatoriRepository(driverFactory: DriverFactory) {
    private val database = SatoriDatabase(driverFactory.createDriver())
    private val dbQueries = database.satoriDatabaseQueries

    fun insertReactionResult(reactionTimeMs: Long) {
        dbQueries.insertResult(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            reactionTimeMs = reactionTimeMs
        )
    }

    fun getAllResults(): List<ReactionResult> {
        return dbQueries.selectAllResults().executeAsList()
    }
}
