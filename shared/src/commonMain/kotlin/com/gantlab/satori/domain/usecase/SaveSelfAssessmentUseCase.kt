package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SaveSelfAssessmentUseCase(
    private val repository: SatoriRepository,
    private val settings: SettingsManager,
    private val api: SatoriApiService? = null
) {
    suspend operator fun invoke(attention: Long, memory: Long, executive: Long) = withContext(Dispatchers.Default) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        
        // Local persistence
        repository.insertSelfAssessmentWithTimestamp(timestamp, attention, memory, executive)
        
        // Remote sync
        settings.authToken?.let { token ->
            try {
                api?.postSelfAssessment(token, timestamp, attention, memory, executive)
                // In a perfect world, we'd mark it as synced in DB here
            } catch (e: Exception) {
                // Silently fail, background sync should handle it
            }
        }
    }
}
