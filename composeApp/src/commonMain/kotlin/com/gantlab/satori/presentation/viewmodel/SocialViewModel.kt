package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.db.SocialScenario
import com.gantlab.satori.domain.model.Tip
import com.gantlab.satori.domain.usecase.GetOverstimulationTipsUseCase
import com.gantlab.satori.domain.usecase.GetSocialScenariosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SocialUiState(
    val scenarios: List<SocialScenario> = emptyList(),
    val isLoading: Boolean = false
)

class SocialViewModel(
    private val getSocialScenariosUseCase: GetSocialScenariosUseCase,
    private val getOverstimulationTipsUseCase: GetOverstimulationTipsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val scenarios = getSocialScenariosUseCase()
            _uiState.update { it.copy(scenarios = scenarios, isLoading = false) }
        }
    }

    fun getOverstimulationTips(): List<Tip> = getOverstimulationTipsUseCase()
}
