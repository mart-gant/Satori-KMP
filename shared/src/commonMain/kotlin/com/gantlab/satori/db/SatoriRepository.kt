package com.gantlab.satori.db

import com.gantlab.satori.domain.model.*
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

open class SatoriRepository(
    private val database: SatoriDatabase,
    private val api: SatoriApiService? = null,
    private val settings: SettingsManager? = null
) : ReactionRepository, MoodRepository, ChallengeRepository, RoutineRepository, AssessmentRepository, ScenarioRepository {
    
    constructor(driverFactory: DriverFactory, api: SatoriApiService, settings: SettingsManager) : 
        this(SatoriDatabase(driverFactory.createDriver()), api, settings)

    protected val dbQueries = database.satoriDatabaseQueries

    // --- Reaction ---

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
            withContext(Dispatchers.Default) {
                val latest = dbQueries.selectAllResults().executeAsList().firstOrNull { it.timestamp == timestamp }
                latest?.let { dbQueries.markResultAsSynced(it.id) }
            }
        } catch (e: Exception) {
            // Keep as unsynced
        }
    }

    override suspend fun getAllResults(): List<DomainReactionResult> = withContext(Dispatchers.Default) {
        dbQueries.selectAllResults().executeAsList().map { it.toDomainReaction() }
    }

    override suspend fun getUnsyncedResults(): List<DomainReactionResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedResults().executeAsList().map { it.toDomainReaction() }
    }

    override suspend fun markResultAsSynced(id: Long) {
        withContext(Dispatchers.Default) {
            dbQueries.markResultAsSynced(id)
        }
    }

    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.insertResult(timestamp, reactionTimeMs, if (synced) 1L else 0L, timestamp)
        }
    }

    override suspend fun reactionExists(timestamp: Long): Boolean = withContext(Dispatchers.Default) {
        dbQueries.selectAllResults().executeAsList().any { it.timestamp == timestamp }
    }

    // --- Mood & Energy ---

    override suspend fun insertMood(moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {
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

    override suspend fun getMoodHistory(): List<DomainMoodEntry> = withContext(Dispatchers.Default) {
        dbQueries.getMoodHistory().executeAsList().map { it.toDomainMood() }
    }

    override suspend fun moodExists(timestamp: Long): Boolean = withContext(Dispatchers.Default) {
        dbQueries.getMoodByTimestamp(timestamp).executeAsOneOrNull() != null
    }

    override suspend fun updateMoodNote(id: Long, note: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.updateMoodNote(note, now, id)
        }
    }

    override suspend fun getUnsyncedMood(): List<DomainMoodEntry> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedMood().executeAsList().map { it.toDomainMood() }
    }

    override suspend fun markMoodAsSynced(id: Long) {
        withContext(Dispatchers.Default) {
            dbQueries.markMoodAsSynced(id)
        }
    }

    override suspend fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.insertMood(timestamp, moodScore, energyScore, note, if (synced) 1L else 0L, timestamp)
        }
    }

    // --- Mind Challenges ---

    override suspend fun insertChallengeResult(challengeType: String, score: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertChallengeResult(
                timestamp = now,
                challengeType = challengeType,
                score = score,
                synced = if (synced) 1L else 0L,
                updatedAt = now
            )
        }
    }

    override suspend fun getChallengeHistory(challengeType: String): List<DomainChallengeResult> = withContext(Dispatchers.Default) {
        dbQueries.getChallengeHistory(challengeType).executeAsList().map { it.toDomainChallenge() }
    }

    override suspend fun getUnsyncedChallenges(): List<DomainChallengeResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedChallenges().executeAsList().map { it.toDomainChallenge() }
    }

    override suspend fun markChallengeAsSynced(id: Long) {
        withContext(Dispatchers.Default) {
            dbQueries.markChallengeAsSynced(id)
        }
    }

    override suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.insertChallengeResult(timestamp, type, score, if (synced) 1L else 0L, timestamp)
        }
    }

    override suspend fun challengeExists(timestamp: Long, type: String): Boolean = withContext(Dispatchers.Default) {
        dbQueries.getChallengeHistory(type).executeAsList().any { it.timestamp == timestamp }
    }

    // --- Routines ---

    override suspend fun getAllRoutines(): List<DomainRoutine> = withContext(Dispatchers.Default) {
        dbQueries.getAllRoutines().executeAsList().map { it.toDomainRoutine() }
    }

    override suspend fun insertRoutine(title: String, icon: String?, isActive: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.insertRoutine(title, icon, if (isActive) 1L else 0L)
        }
    }

    override suspend fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.updateRoutine(title, icon, if (isActive) 1L else 0L, id)
        }
    }

    override suspend fun deleteRoutine(id: Long) {
        withContext(Dispatchers.Default) {
            dbQueries.deleteRoutine(id)
        }
    }

    override suspend fun getTasksForRoutine(routineId: Long): List<DomainRoutineTask> = withContext(Dispatchers.Default) {
        dbQueries.getTasksForRoutine(routineId).executeAsList().map { it.toDomainTask() }
    }

    override suspend fun insertRoutineTask(routineId: Long, taskName: String, scheduledTime: String?) {
        withContext(Dispatchers.Default) {
            dbQueries.insertRoutineTask(routineId, taskName, scheduledTime)
        }
    }

    override suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.updateTaskCompletion(if (isCompleted) 1L else 0L, id)
            if (isCompleted) {
                dbQueries.insertTaskCompletion(id, Clock.System.now().toEpochMilliseconds())
            }
        }
    }

    override suspend fun updateTaskDetails(id: Long, taskName: String, scheduledTime: String?) {
        withContext(Dispatchers.Default) {
            dbQueries.updateTaskDetails(taskName, scheduledTime, id)
        }
    }

    override suspend fun getTaskCompletions(sinceTimestamp: Long): List<DomainTaskCompletion> = withContext(Dispatchers.Default) {
        dbQueries.getCompletionsForPeriod(sinceTimestamp).executeAsList().map { it.toDomainCompletion() }
    }

    override suspend fun createRoutine(title: String, icon: String?) {
        withContext(Dispatchers.Default) {
            dbQueries.insertRoutine(title, icon, 1L)
        }
    }

    // --- Self-Assessment ---

    override suspend fun insertSelfAssessment(attention: Long, memory: Long, executive: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertSelfAssessment(
                timestamp = now,
                attentionScore = attention,
                memoryScore = memory,
                executiveScore = executive,
                synced = if (synced) 1L else 0L,
                updatedAt = now
            )
        }
    }

    override suspend fun getSelfAssessmentHistory(): List<DomainSelfAssessmentResult> = withContext(Dispatchers.Default) {
        dbQueries.getSelfAssessmentHistory().executeAsList().map { it.toDomainAssessment() }
    }

    override suspend fun getUnsyncedSelfAssessment(): List<DomainSelfAssessmentResult> = withContext(Dispatchers.Default) {
        dbQueries.selectUnsyncedSelfAssessment().executeAsList().map { it.toDomainAssessment() }
    }

    override suspend fun markSelfAssessmentAsSynced(id: Long) {
        withContext(Dispatchers.Default) {
            dbQueries.markSelfAssessmentAsSynced(id)
        }
    }

    override suspend fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long, synced: Boolean) {
        withContext(Dispatchers.Default) {
            dbQueries.insertSelfAssessment(timestamp, attention, memory, executive, if (synced) 1L else 0L, timestamp)
        }
    }

    override suspend fun selfAssessmentExists(timestamp: Long): Boolean = withContext(Dispatchers.Default) {
        dbQueries.getSelfAssessmentHistory().executeAsList().any { it.timestamp == timestamp }
    }

    // --- Social Scenarios ---

    override suspend fun getAllScenarios(): List<DomainSocialScenario> = withContext(Dispatchers.Default) {
        dbQueries.getAllScenarios().executeAsList().map { it.toDomainScenario() }
    }

    override suspend fun insertScenario(title: String, description: String, steps: String, category: String) {
        withContext(Dispatchers.Default) {
            dbQueries.insertScenario(title, description, steps, category)
        }
    }

    // --- Cleanup ---

    open suspend fun clearAllData() = withContext(Dispatchers.Default) {
        dbQueries.deleteAllResults()
        dbQueries.deleteAllMoods()
        dbQueries.deleteAllChallenges()
        dbQueries.deleteAllSelfAssessments()
        dbQueries.deleteAllRoutines()
    }

    // --- Data Export ---

    open suspend fun exportAllDataToCsv(): String = withContext(Dispatchers.Default) {
        val csv = StringBuilder()
        
        // Mood
        csv.append("--- MOOD HISTORY ---\n")
        csv.append("Timestamp,Mood,Energy,Note\n")
        getMoodHistory().forEach {
            csv.append("${it.timestamp},${it.moodScore},${it.energyScore},\"${it.note ?: ""}\"\n")
        }
        
        // Reaction Results
        csv.append("\n--- REACTION TESTS ---\n")
        csv.append("Timestamp,ReactionTimeMs\n")
        getAllResults().forEach {
            csv.append("${it.timestamp},${it.reactionTimeMs}\n")
        }

        // Challenges
        csv.append("\n--- CHALLENGES ---\n")
        csv.append("Timestamp,Type,Score\n")
        val types = listOf("color_clash", "memory_game")
        types.forEach { type ->
            getChallengeHistory(type).forEach {
                csv.append("${it.timestamp},${it.challengeType},${it.score}\n")
            }
        }
        
        csv.toString()
    }
}
