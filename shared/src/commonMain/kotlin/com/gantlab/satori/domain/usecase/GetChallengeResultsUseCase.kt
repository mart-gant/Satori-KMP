package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ChallengeRepository
import com.gantlab.satori.domain.model.DomainChallengeResult

class GetChallengeResultsUseCase(private val repository: ChallengeRepository) {
    suspend operator fun invoke(): Map<String, List<DomainChallengeResult>> {
        val types = listOf("color_clash", "memory_game")
        return types.associateWith { type ->
            repository.getChallengeHistory(type)
        }
    }
}
