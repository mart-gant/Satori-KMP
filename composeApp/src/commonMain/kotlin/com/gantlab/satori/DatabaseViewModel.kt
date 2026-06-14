package com.gantlab.satori

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.utils.TimeUtils
import kotlinx.coroutines.launch

class DatabaseViewModel(private val repository: SatoriRepository) : ViewModel() {
    
    val results = mutableStateListOf<ReactionResult>()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            results.clear()
            results.addAll(repository.getAllResults())
        }
    }

    fun addFakeResult() {
        viewModelScope.launch {
            repository.insertReactionResult(
                reactionTimeMs = (100..500).random().toLong(),
                synced = false
            )
            refresh()
        }
    }

    fun addFakeMood() {
        viewModelScope.launch {
            repository.insertMood(
                moodScore = (1..5).random().toLong(),
                energyScore = (1..5).random().toLong(),
                note = "Automatyczny wpis testowy",
                synced = false
            )
        }
    }

    fun addFakeChallengeResult(type: String) {
        viewModelScope.launch {
            repository.insertChallengeResult(
                type = type,
                score = (50..200).random().toLong(),
                synced = false
            )
        }
    }

    fun clearAllData() {
        repository.clearAllData()
        refresh()
    }
}
