package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ReactionRepository

class SaveChallengeResultUseCase(private val repository: ReactionRepository) {
    suspend operator fun invoke(type: String, score: Long) {
        repository.insertChallengeResult(type, score)
    }
}
