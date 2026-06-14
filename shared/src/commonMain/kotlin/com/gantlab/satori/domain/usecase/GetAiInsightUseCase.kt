package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.network.AiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AiInsightResult {
    data class Success(val insight: String) : AiInsightResult()
    data class Error(val message: String) : AiInsightResult()
    object PermissionDenied : AiInsightResult()
    object Loading : AiInsightResult()
}

class GetAiInsightUseCase(
    private val repository: SatoriRepository,
    private val settings: SettingsManager,
    private val ai: AiService?
) {
    suspend operator fun invoke(): AiInsightResult = withContext(Dispatchers.Default) {
        if (!settings.aiConsentGranted) {
            return@withContext AiInsightResult.PermissionDenied
        }

        val service = ai ?: return@withContext AiInsightResult.Error("AI Service is not available.")
        
        try {
            val dataSummary = repository.exportAllDataToCsv()
            val insight = service.getInsights(dataSummary)
            AiInsightResult.Success(insight)
        } catch (e: Exception) {
            AiInsightResult.Error(e.message ?: "Unknown error occurred during AI analysis.")
        }
    }
}
