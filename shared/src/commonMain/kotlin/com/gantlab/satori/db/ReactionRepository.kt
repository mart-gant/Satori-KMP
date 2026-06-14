package com.gantlab.satori.db

interface ReactionRepository {
    suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean = false)
    suspend fun getAllResults(): List<ReactionResult>
    suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean = false)
    suspend fun reactionExists(timestamp: Long): Boolean
    suspend fun insertChallengeResult(type: String, score: Long, synced: Boolean = false)
    suspend fun getChallengeHistory(type: String): List<ChallengeResult>
    suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean = false)
    suspend fun challengeExists(timestamp: Long, type: String): Boolean
    suspend fun getUnsyncedResults(): List<ReactionResult>
    suspend fun markResultAsSynced(id: Long)
    
    // Mood
    suspend fun getUnsyncedMood(): List<MoodEntry>
    suspend fun markMoodAsSynced(id: Long)
    
    // Challenges
    suspend fun getUnsyncedChallenges(): List<ChallengeResult>
    suspend fun markChallengeAsSynced(id: Long)
    
    // Self-Assessment
    suspend fun getUnsyncedSelfAssessment(): List<SelfAssessmentResult>
    suspend fun markSelfAssessmentAsSynced(id: Long)
}
