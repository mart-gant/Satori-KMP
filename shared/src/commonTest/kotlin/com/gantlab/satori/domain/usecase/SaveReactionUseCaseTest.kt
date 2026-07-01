package com.gantlab.satori.domain.usecase

import com.gantlab.satori.Analytics
import com.gantlab.satori.db.*
import com.gantlab.satori.domain.model.*
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeReactionRepository : ReactionRepository {
    val savedReactions = mutableListOf<Long>()
    
    override suspend fun insertReactionResult(reactionTimeMs: Long, synced: Boolean) {
        savedReactions.add(reactionTimeMs)
    }

    override suspend fun getAllResults(): List<DomainReactionResult> = emptyList()
    override suspend fun insertReactionWithTimestamp(timestamp: Long, reactionTimeMs: Long, synced: Boolean) {}
    override suspend fun reactionExists(timestamp: Long): Boolean = false
    override suspend fun getUnsyncedResults(): List<DomainReactionResult> = emptyList()
    override suspend fun markResultAsSynced(id: Long) {}
}

class FakeAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {}
}

class SaveReactionUseCaseTest {

    @Test
    fun `should save reaction result to repository`() = runTest {
        val repository = FakeReactionRepository()
        val settings = SettingsManager(MapSettings())
        val analytics = FakeAnalytics()
        
        val useCase = SaveReactionUseCase(repository, settings, analytics)

        useCase(250L)

        assertEquals(1, repository.savedReactions.size)
        assertEquals(250L, repository.savedReactions[0])
    }
}
