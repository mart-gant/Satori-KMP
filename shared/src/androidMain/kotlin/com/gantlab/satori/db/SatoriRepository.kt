package com.gantlab.satori.db

import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class AndroidSatoriRepository(
    private val database: SatoriDatabase,
    @Suppress("UnusedPrivateProperty")
    private val api: SatoriApiService? = null,
    @Suppress("UnusedPrivateProperty")
    private val settings: SettingsManager? = null,
) : SatoriRepository {

    private val dbQueries = database.satoriDatabaseQueries

    // REACTION
    override suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        withContext(Dispatchers.Default) {
            dbQueries.insertResult(now, reactionTimeMs, if (synced) 1L else 0L, now)
        }
    }

    override suspend fun getAllResults(): List<DomainReactionResult> = withContext(Dispatchers.Default) {
        dbQueries.selectAllResults().executeAsList().map { it.toDomain() }
    }

    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {
        dbQueries.insertResult(timestamp, reactionTimeMs, if (synced) 1L else 0L, timestamp)
    }

    override suspend fun reactionExists(timestamp: Long): Boolean {
        return dbQueries.selectAllResults().executeAsList().any { it.timestamp == timestamp }
    }

    override suspend fun getUnsyncedResults(): List<DomainReactionResult> = 
        dbQueries.selectUnsyncedResults().executeAsList().map { it.toDomain() }

    override suspend fun markResultAsSynced(id: Long) { dbQueries.markResultAsSynced(id) }

    // MOOD
    override suspend fun insertMood(moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertMood(now, moodScore, energyScore, note, if (synced) 1L else 0L, now)
    }

    override suspend fun getMoodHistory(): List<DomainMoodEntry> = 
        dbQueries.getMoodHistory().executeAsList().map { it.toDomain() }

    override suspend fun updateMoodNote(id: Long, note: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.updateMoodNote(note, now, id)
    }

    override suspend fun getUnsyncedMood(): List<DomainMoodEntry> = 
        dbQueries.selectUnsyncedMood().executeAsList().map { it.toDomain() }

    override suspend fun markMoodAsSynced(id: Long) { dbQueries.markMoodAsSynced(id) }

    override suspend fun moodExists(timestamp: Long): Boolean =
        dbQueries.getMoodHistory().executeAsList().any { it.timestamp == timestamp }

    override suspend fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {
        dbQueries.insertMood(timestamp, moodScore, energyScore, note, if (synced) 1L else 0L, timestamp)
    }

    // CHALLENGES
    override suspend fun insertChallengeResult(challengeType: String, score: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertChallengeResult(now, challengeType, score, if (synced) 1L else 0L, now)
    }

    override suspend fun getChallengeHistory(challengeType: String): List<DomainChallengeResult> =
        dbQueries.getChallengeHistory(challengeType).executeAsList().map { it.toDomain() }

    override suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean) {
        dbQueries.insertChallengeResult(timestamp, type, score, if (synced) 1L else 0L, timestamp)
    }

    override suspend fun challengeExists(timestamp: Long, type: String): Boolean {
        return dbQueries.getChallengeHistory(type).executeAsList().any { it.timestamp == timestamp }
    }

    override suspend fun getUnsyncedChallenges(): List<DomainChallengeResult> = 
        dbQueries.selectUnsyncedChallenges().executeAsList().map { it.toDomain() }

    override suspend fun markChallengeAsSynced(id: Long) { dbQueries.markChallengeAsSynced(id) }

    // ROUTINES
    override suspend fun getAllRoutines(): List<DomainRoutine> = 
        dbQueries.getAllRoutines().executeAsList().map { it.toDomain() }

    override suspend fun insertRoutine(title: String, icon: String?, isActive: Boolean) {
        dbQueries.insertRoutine(title, icon, if (isActive) 1L else 0L)
    }

    override suspend fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {
        dbQueries.updateRoutine(title, icon, if (isActive) 1L else 0L, id)
    }

    override suspend fun deleteRoutine(id: Long) {
        dbQueries.deleteRoutine(id)
    }

    override suspend fun getTasksForRoutine(routineId: Long): List<DomainRoutineTask> =
        dbQueries.getTasksForRoutine(routineId).executeAsList().map { it.toDomain() }

    override suspend fun insertRoutineTask(routineId: Long, taskName: String, scheduledTime: String?) {
        dbQueries.insertRoutineTask(routineId, taskName, scheduledTime)
    }

    override suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) {
        dbQueries.updateTaskCompletion(if (isCompleted) 1L else 0L, id)
    }

    override suspend fun updateTaskDetails(id: Long, taskName: String, scheduledTime: String?) {
        dbQueries.updateTaskDetails(taskName, scheduledTime, id)
    }

    override suspend fun getTaskCompletions(sinceTimestamp: Long): List<DomainTaskCompletion> =
        dbQueries.getCompletionsForPeriod(sinceTimestamp).executeAsList().map { DomainTaskCompletion(it.id, it.taskId, it.timestamp) }

    override suspend fun createRoutine(title: String, icon: String?) {
        dbQueries.insertRoutine(title, icon, 1L)
    }

    // ASSESSMENT
    override suspend fun insertSelfAssessment(attention: Long, memory: Long, executive: Long, synced: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        dbQueries.insertSelfAssessment(now, attention, memory, executive, if (synced) 1L else 0L, now)
    }

    override suspend fun getSelfAssessmentHistory(): List<DomainSelfAssessmentResult> =
        dbQueries.getSelfAssessmentHistory().executeAsList().map { it.toDomain() }

    override suspend fun getUnsyncedSelfAssessment(): List<DomainSelfAssessmentResult> =
        dbQueries.selectUnsyncedSelfAssessment().executeAsList().map { it.toDomain() }

    override suspend fun markSelfAssessmentAsSynced(id: Long) { dbQueries.markSelfAssessmentAsSynced(id) }

    override suspend fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long, synced: Boolean) {
        dbQueries.insertSelfAssessment(timestamp, attention, memory, executive, if (synced) 1L else 0L, timestamp)
    }

    override suspend fun selfAssessmentExists(timestamp: Long): Boolean = 
        dbQueries.getSelfAssessmentHistory().executeAsList().any { it.timestamp == timestamp }

    // SCENARIOS
    override suspend fun getAllScenarios(): List<DomainSocialScenario> =
        dbQueries.getAllScenarios().executeAsList().map { it.toDomain() }

    override suspend fun insertScenario(title: String, description: String, steps: String, category: String) {
        dbQueries.insertScenario(title, description, steps, category)
    }

    // CLEANUP & EXPORT
    override suspend fun clearAllData() {
        dbQueries.deleteAllResults()
        dbQueries.deleteAllMoods()
        dbQueries.deleteAllChallenges()
        dbQueries.deleteAllSelfAssessments()
        dbQueries.deleteAllRoutines()
    }

    override suspend fun exportAllDataToCsv(): String {
        return "Eksport danych dostępny w wersji Android"
    }
}
