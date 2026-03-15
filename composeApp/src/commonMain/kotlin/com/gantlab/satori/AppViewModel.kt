package com.gantlab.satori

import androidx.lifecycle.ViewModel
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.gantlab.satori.db.ReactionResult

class AppViewModel(
    private val repository: SatoriRepository,
    private val settings: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                isOnboardingCompleted = settings.isOnboardingCompleted,
                nickname = settings.nickname,
                highContrast = settings.highContrast,
                largeFont = settings.largeFont,
                animationsEnabled = settings.animationsEnabled
            )
        }
        loadResults()
    }

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        _uiState.update { it.copy(isOnboardingCompleted = true) }
    }

    fun saveReactionTime(timeMs: Long) {
        repository.insertReactionResult(timeMs)
        loadResults()
    }

    fun loadResults() {
        val results = repository.getAllResults()
        val best = if (results.isNotEmpty()) results.minOf { it.reactionTimeMs } else null
        val avg = if (results.isNotEmpty()) results.map { it.reactionTimeMs }.average().toLong() else null
        
        _uiState.update { 
            it.copy(
                results = results,
                bestResult = best,
                averageResult = avg,
                rank = calculateRank(best)
            ) 
        }
    }

    private fun calculateRank(bestMs: Long?): String {
        if (bestMs == null) return "Nowicjusz"
        return when {
            bestMs < 200 -> "Ninja"
            bestMs < 250 -> "Gepard"
            bestMs < 300 -> "Sokół"
            bestMs < 400 -> "Człowiek"
            else -> "Leniwiec"
        }
    }

    fun updateNickname(name: String) {
        settings.nickname = name
        _uiState.update { it.copy(nickname = name) }
    }

    fun toggleHighContrast(enabled: Boolean) {
        settings.highContrast = enabled
        _uiState.update { it.copy(highContrast = enabled) }
    }

    fun toggleLargeFont(enabled: Boolean) {
        settings.largeFont = enabled
        _uiState.update { it.copy(largeFont = enabled) }
    }

    fun toggleAnimations(enabled: Boolean) {
        settings.animationsEnabled = enabled
        _uiState.update { it.copy(animationsEnabled = enabled) }
    }
}

data class AppState(
    val isOnboardingCompleted: Boolean = false,
    val nickname: String = "",
    val highContrast: Boolean = false,
    val largeFont: Boolean = false,
    val animationsEnabled: Boolean = true,
    val results: List<ReactionResult> = emptyList(),
    val bestResult: Long? = null,
    val averageResult: Long? = null,
    val rank: String = "Nowicjusz"
)
