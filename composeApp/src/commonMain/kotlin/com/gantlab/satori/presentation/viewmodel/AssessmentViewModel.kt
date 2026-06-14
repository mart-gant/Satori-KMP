package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.db.SelfAssessmentResult
import com.gantlab.satori.domain.usecase.GetSelfAssessmentHistoryUseCase
import com.gantlab.satori.domain.usecase.SaveSelfAssessmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssessmentUiState(
    val selfAssessmentHistory: List<SelfAssessmentResult> = emptyList(),
    val isLoading: Boolean = false
)

class AssessmentViewModel(
    private val saveSelfAssessmentUseCase: SaveSelfAssessmentUseCase,
    private val getSelfAssessmentHistoryUseCase: GetSelfAssessmentHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val history = getSelfAssessmentHistoryUseCase()
            _uiState.update { it.copy(selfAssessmentHistory = history, isLoading = false) }
        }
    }

    fun saveSelfAssessment(attention: Long, memory: Long, executive: Long) {
        viewModelScope.launch {
            saveSelfAssessmentUseCase(attention, memory, executive)
            refreshData()
        }
    }
}
