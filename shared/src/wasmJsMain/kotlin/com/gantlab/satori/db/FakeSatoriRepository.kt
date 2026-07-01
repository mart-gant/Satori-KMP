package com.gantlab.satori.db

import com.gantlab.satori.domain.model.*

class FakeSatoriRepository : SatoriRepository {
    override suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean) {}
    override suspend fun getAllResults(): List<DomainReactionResult> = emptyList()
    override suspend fun getUnsyncedResults(): List<DomainReactionResult> = emptyList()
    override suspend fun markResultAsSynced(id: Long) {}
    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {}
    override suspend fun reactionExists(timestamp: Long): Boolean = false
    
    override suspend fun insertMood(moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {}
    override suspend fun getMoodHistory(): List<DomainMoodEntry> = emptyList()
    override suspend fun updateMoodNote(id: Long, note: String) {}
    override suspend fun getUnsyncedMood(): List<DomainMoodEntry> = emptyList()
    override suspend fun markMoodAsSynced(id: Long) {}
    override suspend fun moodExists(timestamp: Long): Boolean = false
    override suspend fun insertMoodWithTimestamp(timestamp: Long, moodScore: Long, energyScore: Long, note: String?, synced: Boolean) {}

    override suspend fun insertChallengeResult(challengeType: String, score: Long, synced: Boolean) {}
    override suspend fun getChallengeHistory(challengeType: String): List<DomainChallengeResult> = emptyList()
    override suspend fun getUnsyncedChallenges(): List<DomainChallengeResult> = emptyList()
    override suspend fun markChallengeAsSynced(id: Long) {}
    override suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean) {}
    override suspend fun challengeExists(timestamp: Long, type: String): Boolean = false

    override suspend fun getAllRoutines(): List<DomainRoutine> = emptyList()
    override suspend fun insertRoutine(title: String, icon: String?, isActive: Boolean) {}
    override suspend fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {}
    override suspend fun deleteRoutine(id: Long) {}
    override suspend fun getTasksForRoutine(routineId: Long): List<DomainRoutineTask> = emptyList()
    override suspend fun insertRoutineTask(routineId: Long, taskName: String, scheduledTime: String?) {}
    override suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) {}
    override suspend fun updateTaskDetails(id: Long, taskName: String, scheduledTime: String?) {}
    override suspend fun getTaskCompletions(sinceTimestamp: Long): List<DomainTaskCompletion> = emptyList()
    override suspend fun createRoutine(title: String, icon: String?) {}

    override suspend fun insertSelfAssessment(attention: Long, memory: Long, executive: Long, synced: Boolean) {}
    override suspend fun getSelfAssessmentHistory(): List<DomainSelfAssessmentResult> = emptyList()
    override suspend fun getUnsyncedSelfAssessment(): List<DomainSelfAssessmentResult> = emptyList()
    override suspend fun markSelfAssessmentAsSynced(id: Long) {}
    override suspend fun insertSelfAssessmentWithTimestamp(timestamp: Long, attention: Long, memory: Long, executive: Long, synced: Boolean) {}
    override suspend fun selfAssessmentExists(timestamp: Long): Boolean = false

    override suspend fun getAllScenarios(): List<DomainSocialScenario> = emptyList()
    override suspend fun insertScenario(title: String, description: String, steps: String, category: String) {}

    override suspend fun clearAllData() {}
    override suspend fun exportAllDataToCsv(): String = ""
}
