package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.domain.usecase.GetMoodDataUseCase
import com.gantlab.satori.domain.usecase.SaveMoodUseCase
import com.gantlab.satori.domain.usecase.UpdateMoodNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoodUiState(
    val moodHistory: List<MoodEntry> = emptyList(),
    val moodStreak: Int = 0,
    val isLoading: Boolean = false
)

class MoodViewModel(
    private val saveMoodUseCase: SaveMoodUseCase,
    private val getMoodDataUseCase: GetMoodDataUseCase,
    private val updateMoodNoteUseCase: UpdateMoodNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = getMoodDataUseCase()
            _uiState.update { 
                it.copy(
                    moodHistory = data.history, 
                    moodStreak = data.streak,
                    isLoading = false
                ) 
            }
        }
    }

    fun saveMood(mood: Long, energy: Long, note: String) {
        viewModelScope.launch {
            saveMoodUseCase(mood, energy, note)
            refreshData()
        }
    }

    fun updateMoodNote(id: Long, note: String) {
        viewModelScope.launch {
            updateMoodNoteUseCase(id, note)
            refreshData()
        }
    }
}
