package com.gantlab.satori

import androidx.compose.runtime.mutableStateListOf
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.createDatabase

class DatabaseViewModel(driverFactory: DriverFactory) {
    private val database = createDatabase(driverFactory)
    private val queries = database.satoriDatabaseQueries

    val results = mutableStateListOf<ReactionResult>()

    init {
        refresh()
    }

    fun refresh() {
        results.clear()
        results.addAll(queries.selectAllResults().executeAsList())
    }

    fun addFakeResult() {
        queries.insertResult(
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            reactionTimeMs = (100..500).random().toLong()
        )
        refresh()
    }
}
