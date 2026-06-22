package com.gantlab.satori.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement

actual class DriverFactory {
    actual fun createDriver(): SqlDriver = InMemorySqlDriver()
}

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = InMemorySqlDriver()
}

private typealias DbRow = MutableMap<String, Any?>

private class InMemorySqlDriver : SqlDriver {
    private val reactionResults = mutableListOf<DbRow>()
    private val moodEntries = mutableListOf<DbRow>()
    private val challengeResults = mutableListOf<DbRow>()
    private val routines = mutableListOf<DbRow>()
    private val routineTasks = mutableListOf<DbRow>()
    private val socialScenarios = mutableListOf<DbRow>()
    private val taskCompletions = mutableListOf<DbRow>()
    private val selfAssessmentResults = mutableListOf<DbRow>()

    private var nextReactionId = 1L
    private var nextMoodId = 1L
    private var nextChallengeId = 1L
    private var nextRoutineId = 1L
    private var nextRoutineTaskId = 1L
    private var nextSocialScenarioId = 1L
    private var nextTaskCompletionId = 1L
    private var nextSelfAssessmentId = 1L

    private val listeners = mutableMapOf<String, MutableSet<Query.Listener>>()
    private var transaction: Transacter.Transaction? = null

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> {
        val statement = BoundStatement(parameters)
        binders?.invoke(statement)
        return mapper(ListCursor(queryRows(identifier, statement.values)))
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> {
        val statement = BoundStatement(parameters)
        binders?.invoke(statement)
        return QueryResult.Value(executeStatement(identifier, sql, statement.values))
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        val enclosing = transaction
        val current = object : Transacter.Transaction() {
            override val enclosingTransaction: Transacter.Transaction? = enclosing

            override fun endTransaction(successful: Boolean): QueryResult<Unit> {
                transaction = enclosing
                return QueryResult.Unit
            }
        }
        transaction = current
        return QueryResult.Value(current)
    }

    override fun currentTransaction(): Transacter.Transaction? = transaction

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach { key ->
            listeners.getOrPut(key) { mutableSetOf() }.add(listener)
        }
    }

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach { key ->
            listeners[key]?.remove(listener)
        }
    }

    override fun notifyListeners(vararg queryKeys: String) {
        queryKeys.forEach { key ->
            listeners[key]?.forEach { it.queryResultsChanged() }
        }
    }

    override fun close() = Unit

    private fun queryRows(identifier: Int?, values: List<Any?>): List<List<Any?>> = when (identifier) {
        SELECT_ALL_RESULTS -> reactionResults
            .sortedWith(compareByDescending<DbRow> { it.long("timestamp") }.thenByDescending { it.long("id") })
            .map { it.reactionResultRow() }
        SELECT_UNSYNCED_RESULTS -> reactionResults
            .filter { it.long("synced") == 0L }
            .map { it.reactionResultRow() }
        GET_MOOD_HISTORY -> moodEntries
            .sortedByDescending { it.long("timestamp") }
            .map { it.moodEntryRow() }
        SELECT_UNSYNCED_MOOD -> moodEntries
            .filter { it.long("synced") == 0L }
            .map { it.moodEntryRow() }
        GET_MOOD_BY_TIMESTAMP -> moodEntries
            .filter { it.long("timestamp") == values.long(0) }
            .map { it.moodEntryRow() }
        GET_CHALLENGE_HISTORY -> challengeResults
            .filter { it.string("challengeType") == values.string(0) }
            .sortedByDescending { it.long("timestamp") }
            .map { it.challengeResultRow() }
        SELECT_UNSYNCED_CHALLENGES -> challengeResults
            .filter { it.long("synced") == 0L }
            .map { it.challengeResultRow() }
        GET_ALL_ROUTINES -> routines.map { it.routineRow() }
        GET_TASKS_FOR_ROUTINE -> routineTasks
            .filter { it.long("routineId") == values.long(0) }
            .map { it.routineTaskRow() }
        GET_ALL_SCENARIOS -> socialScenarios.map { it.socialScenarioRow() }
        GET_COMPLETIONS_FOR_PERIOD -> taskCompletions
            .filter { it.long("timestamp") >= values.long(0) }
            .map { it.taskCompletionRow() }
        GET_SELF_ASSESSMENT_HISTORY -> selfAssessmentResults
            .sortedByDescending { it.long("timestamp") }
            .map { it.selfAssessmentRow() }
        SELECT_UNSYNCED_SELF_ASSESSMENT -> selfAssessmentResults
            .filter { it.long("synced") == 0L }
            .map { it.selfAssessmentRow() }
        else -> unsupportedQuery(identifier)
    }

    private fun executeStatement(identifier: Int?, sql: String, values: List<Any?>): Long = when (identifier) {
        null -> 0L
        INSERT_RESULT -> {
            reactionResults += mutableMapOf(
                "id" to nextReactionId++,
                "timestamp" to values.long(0),
                "reactionTimeMs" to values.long(1),
                "synced" to values.long(2),
                "updatedAt" to values.long(3)
            )
            1L
        }
        MARK_RESULT_AS_SYNCED -> updateById(reactionResults, values.long(0)) { it["synced"] = 1L }
        INSERT_MOOD -> {
            moodEntries += mutableMapOf(
                "id" to nextMoodId++,
                "timestamp" to values.long(0),
                "moodScore" to values.long(1),
                "energyScore" to values.long(2),
                "note" to values.string(3),
                "synced" to values.long(4),
                "updatedAt" to values.long(5)
            )
            1L
        }
        MARK_MOOD_AS_SYNCED -> updateById(moodEntries, values.long(0)) { it["synced"] = 1L }
        UPDATE_MOOD_NOTE -> updateById(moodEntries, values.long(2)) {
            it["note"] = values.string(0)
            it["updatedAt"] = values.long(1)
        }
        INSERT_CHALLENGE_RESULT -> {
            challengeResults += mutableMapOf(
                "id" to nextChallengeId++,
                "timestamp" to values.long(0),
                "challengeType" to values.string(1),
                "score" to values.long(2),
                "synced" to values.long(3),
                "updatedAt" to values.long(4)
            )
            1L
        }
        MARK_CHALLENGE_AS_SYNCED -> updateById(challengeResults, values.long(0)) { it["synced"] = 1L }
        INSERT_ROUTINE -> {
            routines += mutableMapOf(
                "id" to nextRoutineId++,
                "title" to values.string(0),
                "icon" to values.string(1),
                "isActive" to values.long(2)
            )
            1L
        }
        UPDATE_ROUTINE -> updateById(routines, values.long(3)) {
            it["title"] = values.string(0)
            it["icon"] = values.string(1)
            it["isActive"] = values.long(2)
        }
        INSERT_ROUTINE_TASK -> {
            routineTasks += mutableMapOf(
                "id" to nextRoutineTaskId++,
                "routineId" to values.long(0),
                "taskName" to values.string(1),
                "scheduledTime" to values.string(2),
                "isCompletedToday" to 0L
            )
            1L
        }
        UPDATE_TASK_COMPLETION -> updateById(routineTasks, values.long(1)) {
            it["isCompletedToday"] = values.long(0)
        }
        UPDATE_TASK_DETAILS -> updateById(routineTasks, values.long(2)) {
            it["taskName"] = values.string(0)
            it["scheduledTime"] = values.string(1)
        }
        DELETE_ROUTINE -> deleteRoutine(values.long(0))
        INSERT_SCENARIO -> {
            socialScenarios += mutableMapOf(
                "id" to nextSocialScenarioId++,
                "title" to values.string(0),
                "description" to values.string(1),
                "steps" to values.string(2),
                "category" to values.string(3)
            )
            1L
        }
        INSERT_TASK_COMPLETION -> {
            taskCompletions += mutableMapOf(
                "id" to nextTaskCompletionId++,
                "taskId" to values.long(0),
                "timestamp" to values.long(1)
            )
            1L
        }
        INSERT_SELF_ASSESSMENT -> {
            selfAssessmentResults += mutableMapOf(
                "id" to nextSelfAssessmentId++,
                "timestamp" to values.long(0),
                "attentionScore" to values.long(1),
                "memoryScore" to values.long(2),
                "executiveScore" to values.long(3),
                "synced" to values.long(4),
                "updatedAt" to values.long(5)
            )
            1L
        }
        MARK_SELF_ASSESSMENT_AS_SYNCED -> updateById(selfAssessmentResults, values.long(0)) {
            it["synced"] = 1L
        }
        DELETE_ALL_RESULTS -> reactionResults.clearAndReport()
        DELETE_ALL_MOODS -> moodEntries.clearAndReport()
        DELETE_ALL_CHALLENGES -> challengeResults.clearAndReport()
        DELETE_ALL_SELF_ASSESSMENTS -> selfAssessmentResults.clearAndReport()
        DELETE_ALL_ROUTINES -> {
            val count = routines.size.toLong()
            routines.clear()
            routineTasks.clear()
            taskCompletions.clear()
            count
        }
        else -> error("Unsupported SQL statement $identifier: $sql")
    }

    private fun deleteRoutine(id: Long): Long {
        val taskIds = routineTasks.filter { it.long("routineId") == id }.map { it.long("id") }.toSet()
        val removed = routines.removeAll { it.long("id") == id }
        routineTasks.removeAll { it.long("routineId") == id }
        taskCompletions.removeAll { it.long("taskId") in taskIds }
        return if (removed) 1L else 0L
    }

    private fun updateById(rows: List<DbRow>, id: Long, update: (DbRow) -> Unit): Long {
        val row = rows.firstOrNull { it.long("id") == id } ?: return 0L
        update(row)
        return 1L
    }

    private fun unsupportedQuery(identifier: Int?): Nothing {
        error("Unsupported SQL query: $identifier")
    }
}

private class BoundStatement(parameters: Int) : SqlPreparedStatement {
    val values: MutableList<Any?> = MutableList(parameters) { null }

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        values[index] = bytes
    }

    override fun bindLong(index: Int, long: Long?) {
        values[index] = long
    }

    override fun bindDouble(index: Int, double: Double?) {
        values[index] = double
    }

    override fun bindString(index: Int, string: String?) {
        values[index] = string
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        values[index] = boolean
    }
}

private class ListCursor(private val rows: List<List<Any?>>) : SqlCursor {
    private var index = -1

    override fun next(): QueryResult<Boolean> {
        index += 1
        return QueryResult.Value(index < rows.size)
    }

    override fun getString(index: Int): String? = current(index) as String?

    override fun getLong(index: Int): Long? = current(index) as Long?

    override fun getBytes(index: Int): ByteArray? = current(index) as ByteArray?

    override fun getDouble(index: Int): Double? = current(index) as Double?

    override fun getBoolean(index: Int): Boolean? = current(index) as Boolean?

    private fun current(column: Int): Any? = rows[index][column]
}

private fun DbRow.long(key: String): Long = this[key] as Long
private fun DbRow.string(key: String): String? = this[key] as String?
private fun List<Any?>.long(index: Int): Long = this[index] as Long
private fun List<Any?>.string(index: Int): String? = this[index] as String?
private fun MutableList<DbRow>.clearAndReport(): Long {
    val count = size.toLong()
    clear()
    return count
}

private fun DbRow.reactionResultRow(): List<Any?> =
    listOf(long("id"), long("timestamp"), long("reactionTimeMs"), long("synced"), long("updatedAt"))

private fun DbRow.moodEntryRow(): List<Any?> =
    listOf(long("id"), long("timestamp"), long("moodScore"), long("energyScore"), string("note"), long("synced"), long("updatedAt"))

private fun DbRow.challengeResultRow(): List<Any?> =
    listOf(long("id"), long("timestamp"), string("challengeType"), long("score"), long("synced"), long("updatedAt"))

private fun DbRow.routineRow(): List<Any?> =
    listOf(long("id"), string("title"), string("icon"), long("isActive"))

private fun DbRow.routineTaskRow(): List<Any?> =
    listOf(long("id"), long("routineId"), string("taskName"), string("scheduledTime"), long("isCompletedToday"))

private fun DbRow.socialScenarioRow(): List<Any?> =
    listOf(long("id"), string("title"), string("description"), string("steps"), string("category"))

private fun DbRow.taskCompletionRow(): List<Any?> =
    listOf(long("id"), long("taskId"), long("timestamp"))

private fun DbRow.selfAssessmentRow(): List<Any?> =
    listOf(long("id"), long("timestamp"), long("attentionScore"), long("memoryScore"), long("executiveScore"), long("synced"), long("updatedAt"))

private const val SELECT_ALL_RESULTS = 46_212_822
private const val SELECT_UNSYNCED_RESULTS = -1_443_923_710
private const val GET_MOOD_HISTORY = -537_041_076
private const val SELECT_UNSYNCED_MOOD = -1_679_623_829
private const val GET_MOOD_BY_TIMESTAMP = 625_710_391
private const val GET_CHALLENGE_HISTORY = 1_399_551_586
private const val SELECT_UNSYNCED_CHALLENGES = -730_781_724
private const val GET_ALL_ROUTINES = 559_709_855
private const val GET_TASKS_FOR_ROUTINE = -868_380_648
private const val GET_ALL_SCENARIOS = 1_725_564_531
private const val GET_COMPLETIONS_FOR_PERIOD = -1_448_967_996
private const val GET_SELF_ASSESSMENT_HISTORY = 1_206_649_973
private const val SELECT_UNSYNCED_SELF_ASSESSMENT = 1_489_268_898

private const val INSERT_RESULT = -1_615_785_893
private const val MARK_RESULT_AS_SYNCED = 2_097_182_491
private const val INSERT_MOOD = -475_563_339
private const val MARK_MOOD_AS_SYNCED = 636_841_077
private const val UPDATE_MOOD_NOTE = 1_584_833_111
private const val INSERT_CHALLENGE_RESULT = -585_091_102
private const val MARK_CHALLENGE_AS_SYNCED = 686_121_725
private const val INSERT_ROUTINE = 1_738_350_662
private const val UPDATE_ROUTINE = 753_907_766
private const val INSERT_ROUTINE_TASK = 403_650_283
private const val UPDATE_TASK_COMPLETION = -632_924_273
private const val UPDATE_TASK_DETAILS = -1_946_332_305
private const val DELETE_ROUTINE = 2_045_085_972
private const val INSERT_SCENARIO = 1_568_121_934
private const val INSERT_TASK_COMPLETION = -1_421_427_329
private const val INSERT_SELF_ASSESSMENT = 62_348_652
private const val MARK_SELF_ASSESSMENT_AS_SYNCED = 1_022_594_860
private const val DELETE_ALL_RESULTS = -1_801_803_579
private const val DELETE_ALL_MOODS = 749_107_435
private const val DELETE_ALL_CHALLENGES = 2_073_836_865
private const val DELETE_ALL_SELF_ASSESSMENTS = 1_341_546_708
private const val DELETE_ALL_ROUTINES = 320_005_440
