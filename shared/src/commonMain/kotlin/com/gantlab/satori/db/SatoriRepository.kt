package com.gantlab.satori.db

import kotlinx.datetime.Clock

open class SatoriRepository(private val database: SatoriDatabase) {
    
    constructor(driverFactory: DriverFactory) : this(SatoriDatabase(driverFactory.createDriver()))

    private val dbQueries = database.satoriDatabaseQueries

    open fun insertReactionResult(reactionTimeMs: Long) {
        dbQueries.insertResult(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            reactionTimeMs = reactionTimeMs
        )
    }

    open fun getAllResults(): List<ReactionResult> {
        return dbQueries.selectAllResults().executeAsList()
    }
}
