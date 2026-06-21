package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.domain.model.DomainReactionResult

class GetReactionResultsUseCase(private val repository: ReactionRepository) {
    suspend operator fun invoke(): List<DomainReactionResult> {
        return repository.getAllResults()
    }
}
