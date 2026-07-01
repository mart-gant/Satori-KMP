package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ChallengeRepository

class SaveChallengeResultUseCase(private val repository: ChallengeRepository) {
    suspend operator fun invoke(type: String, score: Long) {
        repository.insertChallengeResult(type, score)
    }
}
