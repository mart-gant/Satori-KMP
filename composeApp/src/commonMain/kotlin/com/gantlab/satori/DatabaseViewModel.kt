package com.gantlab.satori

import androidx.compose.runtime.mutableStateListOf
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.createDatabase
import com.gantlab.satori.utils.TimeUtils

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
            timestamp = TimeUtils.nowMs(),
            reactionTimeMs = (100..500).random().toLong()
        )
        refresh()
    }
}
