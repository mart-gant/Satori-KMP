package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncDataUseCase(
    private val repository: SatoriRepository,
    private val settings: SettingsManager,
    private val api: SatoriApiService? = null
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        val token = settings.authToken ?: return@withContext
        val apiService = api ?: return@withContext
        
        try {
            // Sync Mood
            apiService.getMoodHistory(token).forEach { m ->
                if (!repository.moodExists(m.timestamp)) {
                    repository.insertMoodWithTimestamp(m.timestamp, m.moodScore, m.energyScore, m.note, synced = true)
                }
            }
            // Sync Reactions
            apiService.getReactions(token).forEach { r ->
                if (!repository.reactionExists(r.timestamp)) {
                    repository.insertReactionWithTimestamp(r.timestamp, r.reactionTimeMs, synced = true)
                }
            }
            // Sync Challenges
            apiService.getChallenges(token).forEach { c ->
                if (!repository.challengeExists(c.timestamp, c.challengeType)) {
                    repository.insertChallengeWithTimestamp(c.timestamp, c.challengeType, c.score, synced = true)
                }
            }
            // Sync Self Assessment
            apiService.getSelfAssessmentHistory(token).forEach { s ->
                if (!repository.selfAssessmentExists(s.timestamp)) {
                    repository.insertSelfAssessmentWithTimestamp(s.timestamp, s.attentionScore, s.memoryScore, s.executiveScore, synced = true)
                }
            }
        } catch (e: Exception) {
            // Log properly
        }
    }
}
