package com.gantlab.satori.db

import com.gantlab.satori.domain.model.*

interface ReactionRepository {
    suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean = false)
    suspend fun getAllResults(): List<DomainReactionResult>
    suspend fun getUnsyncedResults(): List<DomainReactionResult>
    suspend fun markResultAsSynced(id: Long)
    suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean = false)
    suspend fun reactionExists(timestamp: Long): Boolean
}

interface MoodRepository {
    suspend fun insertMood(moodScore: Long, energyScore: Long, note: String?, synced: Boolean = false)
    suspend fun getMoodHistory(): List<DomainMoodEntry>
    suspend fun updateMoodNote(id: Long, note: String)
    suspend fun getUnsyncedMood(): List<DomainMoodEntry>
    suspend fun markMoodAsSynced(id: Long)
    suspend fun moodExists(timestamp: Long): Boolean
    suspend fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?, synced: Boolean = false)
}

interface ChallengeRepository {
    suspend fun insertChallengeResult(challengeType: String, score: Long, synced: Boolean = false)
    suspend fun getChallengeHistory(challengeType: String): List<DomainChallengeResult>
    suspend fun getUnsyncedChallenges(): List<DomainChallengeResult>
    suspend fun markChallengeAsSynced(id: Long)
    suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean = false)
    suspend fun challengeExists(timestamp: Long, type: String): Boolean
}

interface RoutineRepository {
    suspend fun getAllRoutines(): List<DomainRoutine>
    suspend fun insertRoutine(title: String, icon: String?, isActive: Boolean = true)
    suspend fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean)
    suspend fun deleteRoutine(id: Long)
    suspend fun getTasksForRoutine(routineId: Long): List<DomainRoutineTask>
    suspend fun insertRoutineTask(routineId: Long, taskName: String, scheduledTime: String?)
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean)
    suspend fun updateTaskDetails(id: Long, taskName: String, scheduledTime: String?)
    suspend fun getTaskCompletions(sinceTimestamp: Long): List<DomainTaskCompletion>
    suspend fun createRoutine(title: String, icon: String?)
}

interface AssessmentRepository {
    suspend fun insertSelfAssessment(attention: Long, memory: Long, executive: Long, synced: Boolean = false)
    suspend fun getSelfAssessmentHistory(): List<DomainSelfAssessmentResult>
    suspend fun getUnsyncedSelfAssessment(): List<DomainSelfAssessmentResult>
    suspend fun markSelfAssessmentAsSynced(id: Long)
    suspend fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long, synced: Boolean = false)
    suspend fun selfAssessmentExists(timestamp: Long): Boolean
}

interface ScenarioRepository {
    suspend fun getAllScenarios(): List<DomainSocialScenario>
    suspend fun insertScenario(title: String, description: String, steps: String, category: String)
}
