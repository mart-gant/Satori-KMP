package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.model.*
import com.gantlab.satori.domain.usecase.GetChallengeResultsUseCase
import com.gantlab.satori.domain.usecase.GetReactionResultsUseCase
import com.gantlab.satori.domain.usecase.GetReportsDataUseCase
import com.gantlab.satori.domain.usecase.SaveChallengeResultUseCase
import com.gantlab.satori.domain.usecase.SaveReactionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReactionUiState(
    val results: List<DomainReactionResult> = emptyList(),
    val challengeResults: Map<String, List<DomainChallengeResult>> = emptyMap(),
    val reportsData: ReportsData? = null,
    val isLoading: Boolean = false,
    val bestMs: Long? = null,
    val averageMs: Long? = null,
    val rank: ReactionRank = ReactionRank.HUMAN
)

class ReactionViewModel(
    private val saveReactionUseCase: SaveReactionUseCase,
    private val saveChallengeResultUseCase: SaveChallengeResultUseCase,
    private val getReactionResultsUseCase: GetReactionResultsUseCase,
    private val getChallengeResultsUseCase: GetChallengeResultsUseCase,
    private val getReportsDataUseCase: GetReportsDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val results = getReactionResultsUseCase()
            val challenges = getChallengeResultsUseCase()
            val reports = getReportsDataUseCase()
            
            val best = if (results.isNotEmpty()) results.minOf { it.reactionTimeMs } else null
            val avg = if (results.isNotEmpty()) results.map { it.reactionTimeMs }.average().toLong() else null
            
            _uiState.update { 
                it.copy(
                    results = results,
                    challengeResults = challenges,
                    reportsData = reports,
                    isLoading = false,
                    bestMs = best,
                    averageMs = avg,
                    rank = ReactionRank.fromTime(best)
                )
            }
        }
    }

    fun saveReactionTime(timeMs: Long) {
        viewModelScope.launch {
            saveReactionUseCase(timeMs)
            refreshData()
        }
    }

    fun saveChallengeResult(type: String, score: Long) {
        viewModelScope.launch {
            saveChallengeResultUseCase(type, score)
            refreshData()
        }
    }
}
