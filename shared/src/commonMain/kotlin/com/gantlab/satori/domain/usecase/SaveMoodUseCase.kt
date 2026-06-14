package com.gantlab.satori.domain.usecase

import com.gantlab.satori.Analytics
import com.gantlab.satori.db.SatoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveMoodUseCase(
    private val repository: SatoriRepository,
    private val analytics: Analytics
) {
    suspend operator fun invoke(mood: Long, energy: Long, note: String) = withContext(Dispatchers.Default) {
        repository.insertMood(mood, energy, note)
        analytics.logEvent("mood_logged", mapOf("mood" to mood.toString(), "energy" to energy.toString()))
    }
}
