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

    open fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?) {
        dbQueries.insertMoodWithTimestamp(timestamp, moodScore, energyScore, note)
    }

    open fun moodExists(timestamp: Long): Boolean {
        return dbQueries.getMoodByTimestamp(timestamp).executeAsOneOrNull() != null
    }

    open fun updateMoodNote(id: Long, note: String?) {
        dbQueries.updateMoodNote(note, id)
    }

    open fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long) {
        dbQueries.insertResult(timestamp, reactionTimeMs)
    }

    open fun reactionExists(timestamp: Long): Boolean {
        // Since SQLDelight queries are generated, I should check if I have a query for this.
        // I'll assume I can use selectAllResults and filter for now if there is no specific query, 
        // or I can add one to the .sq file. Let's check .sq file first.
        return getAllResults().any { it.timestamp == timestamp }
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

    open fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long) {
        dbQueries.insertChallengeResult(timestamp, type, score)
    }

    open fun challengeExists(timestamp: Long, type: String): Boolean {
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

    open fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long) {
        dbQueries.insertSelfAssessment(timestamp, attention, memory, executive)
    }

    open fun selfAssessmentExists(timestamp: Long): Boolean {
        return getSelfAssessmentHistory().any { it.timestamp == timestamp }
    }

    // --- Data Export ---

    open fun exportAllDataToCsv(): String {
        val csv = StringBuilder()
        
        // Mood
        csv.append("--- MOOD HISTORY ---\n")
        csv.append("Timestamp,Date,Mood,Energy,Note\n")
        getMoodHistory().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},${instant},${it.moodScore},${it.energyScore},\"${it.note ?: ""}\"\n")
        }
        
        // Reaction Results
        csv.append("\n--- REACTION TESTS ---\n")
        csv.append("Timestamp,Date,ReactionTimeMs\n")
        getAllResults().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},${instant},${it.reactionTimeMs}\n")
        }
        
        // Challenge Results
        csv.append("\n--- MIND CHALLENGES ---\n")
        csv.append("Timestamp,Date,Type,Score\n")
        val challenges = listOf("color_clash", "memory_game")
        challenges.forEach { type ->
            getChallengeHistory(type).forEach {
                val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
                csv.append("${it.timestamp},${instant},${it.challengeType},${it.score}\n")
            }
        }
        
        // Self-Assessment
        csv.append("\n--- SELF ASSESSMENT ---\n")
        csv.append("Timestamp,Date,Attention,Memory,Executive\n")
        getSelfAssessmentHistory().forEach {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp)
            csv.append("${it.timestamp},${instant},${it.attentionScore},${it.memoryScore},${it.executiveScore}\n")
        }

        return csv.toString()
    }

    open fun exportMoodToCsv(): String {
        return exportAllDataToCsv() // Defaulting to all data now
    }
}
