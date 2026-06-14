package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.db.ReactionResult

class GetReactionResultsUseCase(private val repository: ReactionRepository) {
    suspend operator fun invoke(): List<ReactionResult> {
        return repository.getAllResults()
    }
}
