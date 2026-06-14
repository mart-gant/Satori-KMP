package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ChallengeResult
import com.gantlab.satori.db.SatoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetChallengeResultsUseCase(private val repository: SatoriRepository) {
    suspend operator fun invoke(): Map<String, List<ChallengeResult>> = withContext(Dispatchers.Default) {
        val colorClash = repository.getChallengeHistory("color_clash")
        val memoryGame = repository.getChallengeHistory("memory_game")
        mapOf(
            "color_clash" to colorClash,
            "memory_game" to memoryGame
        )
    }
}
