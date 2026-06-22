package com.gantlab.satori.domain.usecase

import com.gantlab.satori.Analytics
import com.gantlab.satori.AnalyticsEvents
import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SaveReactionUseCase(
    private val repository: ReactionRepository,
    private val settings: SettingsManager,
    private val analytics: Analytics,
    private val api: SatoriApiService? = null
) {
    suspend operator fun invoke(reactionTimeMs: Long) = withContext(Dispatchers.Default) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        
        // 1. Persistence
        repository.insertReactionResult(reactionTimeMs)
        
        // 2. Analytics (Side Effect)
        analytics.logEvent(AnalyticsEvents.TEST_FINISHED, mapOf("result_ms" to reactionTimeMs.toString()))
        
        // 3. Network Sync (Side Effect - could be offloaded to a BackgroundWorker in a real app)
        val token = settings.authToken
        if (token != null && api != null) {
            try {
                api.postReaction(token, timestamp, reactionTimeMs)
            } catch (e: Exception) {
                // Silently fail, repo will handle retry via sync flags
            }
        }
    }
}
