package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.model.Recommendation
import com.gantlab.satori.domain.usecase.AiInsightResult
import com.gantlab.satori.domain.usecase.GetAiInsightUseCase
import com.gantlab.satori.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val recommendations: List<Recommendation> = emptyList(),
    val dailySatoriScore: Int = 0,
    val aiInsight: String? = null,
    val isLoadingAi: Boolean = false
)

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getAiInsightUseCase: GetAiInsightUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val data = getDashboardDataUseCase()
            _uiState.update { 
                it.copy(
                    recommendations = data.recommendations,
                    dailySatoriScore = data.satoriScore
                )
            }
        }
    }

    fun getAiInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAi = true, aiInsight = "Generowanie analizy...") }
            
            when (val result = getAiInsightUseCase()) {
                is AiInsightResult.Success -> {
                    _uiState.update { it.copy(aiInsight = result.insight, isLoadingAi = false) }
                }
                is AiInsightResult.Error -> {
                    _uiState.update { it.copy(aiInsight = result.message, isLoadingAi = false) }
                }
                AiInsightResult.PermissionDenied -> {
                    _uiState.update { 
                        it.copy(
                            aiInsight = "Brak zgody na analizę AI. Przejdź do profilu, aby ją włączyć.",
                            isLoadingAi = false 
                        ) 
                    }
                }
                AiInsightResult.Loading -> { /* Handled above */ }
            }
        }
    }
}
