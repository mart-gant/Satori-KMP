package com.gantlab.satori.domain.usecase

import com.gantlab.satori.Analytics
import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessReactionUseCase(
    private val repository: ReactionRepository,
    private val settings: SettingsManager,
    private val api: SatoriApiService,
    private val analytics: Analytics
) {
    suspend operator fun invoke(timeMs: Long) = withContext(Dispatchers.IO) {
        val timestamp = TimeUtils.nowMs()
        
        // 1. Database - Single source of truth
        repository.insertReactionWithTimestamp(timestamp, timeMs, synced = false)
        
        // 2. Side effects - decoupling from the main flow
        logAnalytics(timeMs)
        trySync(timestamp, timeMs)
    }

    private fun logAnalytics(timeMs: Long) {
        analytics.logEvent("reaction_saved", mapOf("time_ms" to timeMs.toString()))
    }

    private fun trySync(timestamp: Long, timeMs: Long) {
        val token = settings.authToken ?: return
        // We don't want to block the user if sync is slow
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.postReaction(token, timestamp, timeMs)
                repository.markResultAsSynced(timestamp) // Assuming we add this by timestamp
            } catch (e: Exception) {
                // Silent fail - will be synced later by Background Sync
            }
        }
    }
}
