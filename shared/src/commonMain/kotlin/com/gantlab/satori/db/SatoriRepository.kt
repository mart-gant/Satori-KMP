package com.gantlab.satori.db

import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

open class SatoriRepository(
    private val database: SatoriDatabase,
    private val api: SatoriApiService? = null,
    private val settings: SettingsManager? = null
) : ReactionRepository {
    
    constructor(driverFactory: DriverFactory, api: SatoriApiService, settings: SettingsManager) : 
        this(SatoriDatabase(driverFactory.createDriver()), api, settings)

    private val dbQueries = database.satoriDatabaseQueries

    override suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertResult(
                timestamp = now,
                reactionTimeMs = reactionTimeMs,
                synced = if (synced) 1L else 0L,
                updatedAt = now
            )
        }
        if (!synced) trySyncReaction(now, reactionTimeMs)
    }

    private suspend fun trySyncReaction(timestamp: Long, timeMs: Long) {
        val token = settings?.authToken ?: return
        val apiService = api ?: return
        try {
            apiService.postReaction(token, timestamp, timeMs)
            // Ideally we need the ID to mark it as synced. 
            // For now, let's mark the latest one with this timestamp as synced.
            withContext(Dispatchers.Default) {
                val latest = dbQueries.selectAllResults().executeAsList().firstOrNull { it.timestamp == timestamp }
                latest?.let { dbQueries.markResultAsSynced(it.id) }
            }
        } catch (e: Exception) {
            // Keep as unsynced
        }
    }

    override suspend fun getAllResults(): List<ReactionResult> = withContext(Dispatchers.Default) {
        dbQueries.selectAllResults().executeAsList()
    }

    override suspend fun getUnsyncedResults(): List<ReactionResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedResults().executeAsList()
    }

    override suspend fun markResultAsSynced(id: Long) = withContext(Dispatchers.Default) {
        dbQueries.markResultAsSynced(id)
    }

    // --- Mood & Energy ---

    override suspend fun getUnsyncedMood(): List<MoodEntry> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedMood().executeAsList()
    }

    override suspend fun markMoodAsSynced(id: Long) = withContext(Dispatchers.Default) {
        dbQueries.markMoodAsSynced(id)
    }

    open suspend fun insertMood(moodScore: Long, energyScore: Long, note: String?, synced: Boolean = false) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertMood(
                timestamp = now,
                moodScore = moodScore,
                energyScore = energyScore,
                note = note,
                synced = if (synced) 1L else 0L,
                updatedAt = now
            )
        }
        if (!synced) trySyncMood(now, moodScore, energyScore, note ?: "")
    }

    private suspend fun trySyncMood(timestamp: Long, mood: Long, energy: Long, note: String) {
        val token = settings?.authToken ?: return
        val apiService = api ?: return
        try {
            apiService.postMood(token, mood, energy, note)
            withContext(Dispatchers.Default) {
                val latest = dbQueries.getMoodByTimestamp(timestamp).executeAsOneOrNull()
                latest?.let { dbQueries.markMoodAsSynced(it.id) }
            }
        } catch (e: Exception) { }
    }

    open fun getMoodHistory(): List<MoodEntry> {
        return dbQueries.getMoodHistory().executeAsList()
    }

    open fun moodExists(timestamp: Long): Boolean {
        return dbQueries.getMoodByTimestamp(timestamp).executeAsOneOrNull() != null
    }

    open suspend fun updateMoodNote(id: Long, note: String?) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.updateMoodNote(note, now, id)
        }
    }

    open suspend fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?, synced: Boolean = false) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertMood(timestamp, moodScore, energyScore, note, if (synced) 1L else 0L, now)
        }
    }

    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertResult(timestamp, reactionTimeMs, if (synced) 1L else 0L, now)
    }

    override suspend fun reactionExists(timestamp: Long): Boolean {
        return dbQueries.selectAllResults().executeAsList().any { it.timestamp == timestamp }
    }

    // --- Mind Challenges ---

    override suspend fun getUnsyncedChallenges(): List<ChallengeResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedChallenges().executeAsList()
    }

    override suspend fun markChallengeAsSynced(id: Long) = withContext(Dispatchers.Default) {
        dbQueries.markChallengeAsSynced(id)
    }

    override suspend fun insertChallengeResult(type: String, score: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertChallengeResult(
            timestamp = now,
            challengeType = type,
            score = score,
            synced = if (synced) 1L else 0L,
            updatedAt = now
        )
    }

    override suspend fun getChallengeHistory(type: String): List<ChallengeResult> {
        return dbQueries.getChallengeHistory(type).executeAsList()
    }

    override suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertChallengeResult(timestamp, type, score, if (synced) 1L else 0L, now)
    }

    override suspend fun challengeExists(timestamp: Long, type: String): Boolean {
        return getChallengeHistory(type).any { it.timestamp == timestamp }
    }

    // --- Routines ---

    open fun createRoutine(title: String, icon: String? = null) {
        dbQueries.insertRoutine(title, icon, 1)
    }

    open fun getAllRoutines(): List<Routine> {
        return dbQueries.getAllRoutines().executeAsList()
    }

    open fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {
        dbQueries.updateRoutine(title, icon, if (isActive) 1L else 0L, id)
    }

    open fun addTaskToRoutine(routineId: Long, name: String, time: String?) {
        dbQueries.insertRoutineTask(routineId, name, time)
    }

    open fun getTasksForRoutine(routineId: Long): List<RoutineTask> {
        return dbQueries.getTasksForRoutine(routineId).executeAsList()
    }

    open fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        dbQueries.updateTaskCompletion(if (isCompleted) 1L else 0L, taskId)
        if (isCompleted) {
            dbQueries.insertTaskCompletion(taskId, Clock.System.now().toEpochMilliseconds())
        }
    }

    open fun getTaskCompletions(sinceTimestamp: Long): List<TaskCompletion> {
        return dbQueries.getCompletionsForPeriod(sinceTimestamp).executeAsList()
    }

    open fun updateTaskDetails(taskId: Long, name: String, time: String?) {
        dbQueries.updateTaskDetails(name, time, taskId)
    }

    open fun deleteRoutine(id: Long) {
        dbQueries.deleteRoutine(id)
    }

    // --- Social Scenarios ---

    open fun getAllScenarios(): List<SocialScenario> {
        return dbQueries.getAllScenarios().executeAsList()
    }

    open fun insertScenario(title: String, description: String, steps: String, category: String) {
        dbQueries.insertScenario(title, description, steps, category)
    }

    // --- Self-Assessment ---

    override suspend fun getUnsyncedSelfAssessment(): List<SelfAssessmentResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedSelfAssessment().executeAsList()
    }

    override suspend fun markSelfAssessmentAsSynced(id: Long) = withContext(Dispatchers.Default) {
        dbQueries.markSelfAssessmentAsSynced(id)
    }

    open fun insertSelfAssessment(attention: Long, memory: Long, executive: Long, synced: Boolean = false) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertSelfAssessment(
            timestamp = now,
            attentionScore = attention,
            memoryScore = memory,
            executiveScore = executive,
            synced = if (synced) 1L else 0L,
            updatedAt = now
        )
    }

    open fun getSelfAssessmentHistory(): List<SelfAssessmentResult> {
        return dbQueries.getSelfAssessmentHistory().executeAsList()
    }

    open fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long, synced: Boolean = false) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertSelfAssessment(timestamp, attention, memory, executive, if (synced) 1L else 0L, now)
    }

    open fun selfAssessmentExists(timestamp: Long): Boolean {
        return getSelfAssessmentHistory().any { it.timestamp == timestamp }
    }

    // --- Cleanup ---

    open fun clearAllData() {
        dbQueries.deleteAllResults()
        dbQueries.deleteAllMoods()
        dbQueries.deleteAllChallenges()
        dbQueries.deleteAllSelfAssessments()
        dbQueries.deleteAllRoutines()
    }

    // --- Data Export ---

    open suspend fun exportAllDataToCsv(): String {
        val csv = StringBuilder()
        
        // Mood
        csv.append("--- MOOD HISTORY ---\n")
        csv.append("Timestamp,Date,Mood,Energy,Note\n")
        getMoodHistory().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},$instant,${it.moodScore},${it.energyScore},\"${it.note ?: ""}\"\n")
        }
        
        // Reaction Results
        csv.append("\n--- REACTION TESTS ---\n")
        csv.append("Timestamp,Date,ReactionTimeMs\n")
        getAllResults().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},$instant,${it.reactionTimeMs}\n")
        }
        
        // Challenge Results
        csv.append("\n--- MIND CHALLENGES ---\n")
        csv.append("Timestamp,Date,Type,Score\n")
        val challenges = listOf("color_clash", "memory_game")
        challenges.forEach { type ->
            getChallengeHistory(type).forEach {
                val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
                csv.append("${it.timestamp},$instant,${it.challengeType},${it.score}\n")
            }
        }
        
        // Self-Assessment
        csv.append("\n--- SELF ASSESSMENT ---\n")
        csv.append("Timestamp,Date,Attention,Memory,Executive\n")
        getSelfAssessmentHistory().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},$instant,${it.attentionScore},${it.memoryScore},${it.executiveScore}\n")
        }

        return csv.toString()
    }
}
