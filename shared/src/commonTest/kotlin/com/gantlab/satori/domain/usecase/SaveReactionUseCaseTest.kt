package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.ChallengeResult
import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.db.ReactionResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeReactionRepository : ReactionRepository {
    val savedReactions = mutableListOf<Long>()
    
    override suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean) {
        savedReactions.add(reactionTimeMs)
    }

    override suspend fun getAllResults(): List<ReactionResult> = emptyList()
    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {}
    override suspend fun reactionExists(timestamp: Long): Boolean = false
    override suspend fun insertChallengeResult(type: String, score: Long, synced: Boolean) {}
    override suspend fun getChallengeHistory(type: String): List<ChallengeResult> = emptyList()
    override suspend fun insertChallengeWithTimestamp(timestamp: Long, type: String, score: Long, synced: Boolean) {}
    override suspend fun challengeExists(timestamp: Long, type: String): Boolean = false
    override suspend fun getUnsyncedResults(): List<ReactionResult> = emptyList()
    override suspend fun markResultAsSynced(id: Long) {}
}

class SaveReactionUseCaseTest {

    @Test
    fun `should save reaction result to repository`() = runTest {
        val repository = FakeReactionRepository()
        val useCase = SaveReactionUseCase(repository)

        useCase(250L)

        assertEquals(1, repository.savedReactions.size)
        assertEquals(250L, repository.savedReactions[0])
    }
}
