package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.usecase.GetMoodDataUseCase
import com.gantlab.satori.domain.usecase.SaveMoodUseCase
import com.gantlab.satori.domain.usecase.UpdateMoodNoteUseCase
import com.gantlab.satori.domain.model.DomainMoodEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MoodUiState(
    val moodHistory: List<DomainMoodEntry> = emptyList(),
    val moodStreak: Int = 0,
    val isLoading: Boolean = false
)

class MoodViewModel(
    private val saveMoodUseCase: SaveMoodUseCase,
    private val getMoodDataUseCase: GetMoodDataUseCase,
    private val updateMoodNoteUseCase: UpdateMoodNoteUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val KEY_DRAFT_NOTE = "mood_draft_note"
    private val KEY_MOOD_SCORE = "mood_score"

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState = _uiState.asStateFlow()

    // Formularz odporny na process death
    val draftNote = savedStateHandle.getStateFlow(KEY_DRAFT_NOTE, "")
    val selectedMood = savedStateHandle.getStateFlow(KEY_MOOD_SCORE, 5L)

    init {
        refreshData()
    }

    fun updateDraftNote(note: String) {
        savedStateHandle[KEY_DRAFT_NOTE] = note
    }

    fun updateSelectedMood(score: Long) {
        savedStateHandle[KEY_MOOD_SCORE] = score
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
            savedStateHandle[KEY_DRAFT_NOTE] = ""
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
