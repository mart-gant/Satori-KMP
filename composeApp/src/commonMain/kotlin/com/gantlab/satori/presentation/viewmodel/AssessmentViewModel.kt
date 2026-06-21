package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.model.DomainSelfAssessmentResult
import com.gantlab.satori.domain.usecase.GetSelfAssessmentHistoryUseCase
import com.gantlab.satori.domain.usecase.SaveSelfAssessmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssessmentUiState(
    val selfAssessmentHistory: List<DomainSelfAssessmentResult> = emptyList(),
    val isLoading: Boolean = false
)

class AssessmentViewModel(
    private val saveSelfAssessmentUseCase: SaveSelfAssessmentUseCase,
    private val getSelfAssessmentHistoryUseCase: GetSelfAssessmentHistoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val KEY_ATTENTION = "assessment_attention"
    private val KEY_MEMORY = "assessment_memory"
    private val KEY_EXECUTIVE = "assessment_executive"

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState = _uiState.asStateFlow()

    // Stan formularza odporny na Process Death
    val attentionScore = savedStateHandle.getStateFlow(KEY_ATTENTION, 3L)
    val memoryScore = savedStateHandle.getStateFlow(KEY_MEMORY, 3L)
    val executiveScore = savedStateHandle.getStateFlow(KEY_EXECUTIVE, 3L)

    init {
        refreshData()
    }

    fun updateAttention(score: Long) {
        savedStateHandle[KEY_ATTENTION] = score
    }

    fun updateMemory(score: Long) {
        savedStateHandle[KEY_MEMORY] = score
    }

    fun updateExecutive(score: Long) {
        savedStateHandle[KEY_EXECUTIVE] = score
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val history = getSelfAssessmentHistoryUseCase()
            _uiState.update { it.copy(selfAssessmentHistory = history, isLoading = false) }
        }
    }

    fun saveSelfAssessment() {
        viewModelScope.launch {
            saveSelfAssessmentUseCase(
                attentionScore.value,
                memoryScore.value,
                executiveScore.value
            )
            // Przywracamy domyślne po zapisie
            resetForm()
            refreshData()
        }
    }

    private fun resetForm() {
        savedStateHandle[KEY_ATTENTION] = 3L
        savedStateHandle[KEY_MEMORY] = 3L
        savedStateHandle[KEY_EXECUTIVE] = 3L
    }
}
