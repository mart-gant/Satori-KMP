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

    // --- Mood & Energy ---

    open fun insertMood(moodScore: Long, energyScore: Long, note: String?) {
        dbQueries.insertMood(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            moodScore = moodScore,
            energyScore = energyScore,
            note = note
        )
    }

    open fun getMoodHistory(): List<MoodEntry> {
        return dbQueries.getMoodHistory().executeAsList()
    }

    // --- Mind Challenges ---

    open fun insertChallengeResult(type: String, score: Long) {
        dbQueries.insertChallengeResult(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            challengeType = type,
            score = score
        )
    }

    open fun getChallengeHistory(type: String): List<ChallengeResult> {
        return dbQueries.getChallengeHistory(type).executeAsList()
    }

    // --- Routines ---

    open fun createRoutine(title: String, icon: String? = null) {
        dbQueries.insertRoutine(title, icon, 1)
    }

    open fun getAllRoutines(): List<Routine> {
        return dbQueries.getAllRoutines().executeAsList()
    }

    open fun addTaskToRoutine(routineId: Long, name: String, time: String?) {
        dbQueries.insertRoutineTask(routineId, name, time)
    }

    open fun getTasksForRoutine(routineId: Long): List<RoutineTask> {
        return dbQueries.getTasksForRoutine(routineId).executeAsList()
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

    open fun insertSelfAssessment(attention: Long, memory: Long, executive: Long) {
        dbQueries.insertSelfAssessment(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            attentionScore = attention,
            memoryScore = memory,
            executiveScore = executive
        )
    }

    open fun getSelfAssessmentHistory(): List<SelfAssessmentResult> {
        return dbQueries.getSelfAssessmentHistory().executeAsList()
    }

    // --- Data Export ---

    open fun exportMoodToCsv(): String {
        val history = getMoodHistory()
        val csv = StringBuilder("Timestamp,Date,Mood,Energy,Note\n")
        history.forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},${instant},${it.moodScore},${it.energyScore},\"${it.note ?: ""}\"\n")
        }
        return csv.toString()
    }
}
