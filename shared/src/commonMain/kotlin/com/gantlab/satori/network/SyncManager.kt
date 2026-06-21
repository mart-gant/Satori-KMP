package com.gantlab.satori.network

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncManager(
    private val repository: SatoriRepository,
    private val apiService: SatoriApiService,
    private val settings: SettingsManager
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    suspend fun syncAll() {
        val token = settings.authToken ?: return
        if (_isSyncing.value) return
        
        _isSyncing.value = true
        try {
            syncMood(token)
            syncReactions(token)
            syncChallenges(token)
            syncSelfAssessment(token)
            syncRoutines(token)
        } catch (e: Exception) {
            println("Sync failed: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun syncMood(token: String) {
        val unsynced = repository.getUnsyncedMood()
        unsynced.forEach { entry ->
            val response = apiService.postMood(token, entry.moodScore.toLong(), entry.energyScore.toLong(), entry.note)
            if (response != null) {
                repository.markMoodAsSynced(entry.id)
            }
        }

        val serverHistory = apiService.getMoodHistory(token)
        serverHistory.forEach { serverEntry ->
            if (!repository.moodExists(serverEntry.timestamp)) {
                repository.insertMoodWithTimestamp(
                    timestamp = serverEntry.timestamp,
                    moodScore = serverEntry.moodScore,
                    energyScore = serverEntry.energyScore,
                    note = serverEntry.note,
                    synced = true
                )
            }
        }
    }

    private suspend fun syncReactions(token: String) {
        val unsynced = repository.getUnsyncedResults()
        unsynced.forEach { result ->
            if (apiService.postReaction(token, result.timestamp, result.reactionTimeMs)) {
                repository.markResultAsSynced(result.id)
            }
        }

        val serverResults = apiService.getReactions(token)
        serverResults.forEach { serverResult ->
            if (!repository.reactionExists(serverResult.timestamp)) {
                repository.insertReactionWithTimestamp(
                    timestamp = serverResult.timestamp,
                    reactionTimeMs = serverResult.reactionTimeMs,
                    synced = true
                )
            }
        }
    }

    private suspend fun syncChallenges(token: String) {
        val unsynced = repository.getUnsyncedChallenges()
        unsynced.forEach { challenge ->
            if (apiService.postChallenge(token, challenge.timestamp, challenge.challengeType, challenge.score)) {
                repository.markChallengeAsSynced(challenge.id)
            }
        }

        val serverChallenges = apiService.getChallenges(token)
        serverChallenges.forEach { serverChallenge ->
            if (!repository.challengeExists(serverChallenge.timestamp, serverChallenge.challengeType)) {
                repository.insertChallengeWithTimestamp(
                    timestamp = serverChallenge.timestamp,
                    type = serverChallenge.challengeType,
                    score = serverChallenge.score,
                    synced = true
                )
            }
        }
    }

    private suspend fun syncSelfAssessment(token: String) {
        val unsynced = repository.getUnsyncedSelfAssessment()
        unsynced.forEach { assessment ->
            if (apiService.postSelfAssessment(token, assessment.timestamp, assessment.attentionScore, assessment.memoryScore, assessment.executiveScore)) {
                repository.markSelfAssessmentAsSynced(assessment.id)
            }
        }

        val serverAssessment = apiService.getSelfAssessmentHistory(token)
        serverAssessment.forEach { serverEntry ->
            if (!repository.selfAssessmentExists(serverEntry.timestamp)) {
                repository.insertSelfAssessmentWithTimestamp(
                    timestamp = serverEntry.timestamp,
                    attention = serverEntry.attentionScore,
                    memory = serverEntry.memoryScore,
                    executive = serverEntry.executiveScore,
                    synced = true
                )
            }
        }
    }

    private suspend fun syncRoutines(token: String) {
        val localRoutines = repository.getAllRoutines().map { routine ->
            val tasks = repository.getTasksForRoutine(routine.id).map { task ->
                RoutineTaskSyncRequest(task.taskName, task.scheduledTime, task.isCompletedToday)
            }
            RoutineSyncRequest(routine.id, routine.title, routine.icon, routine.isActive, tasks)
        }

        val serverRoutines = apiService.syncRoutines(token, localRoutines)

        serverRoutines.forEach { serverRoutine ->
            val exists = repository.getAllRoutines().any { it.title == serverRoutine.title }
            if (!exists) {
                repository.createRoutine(serverRoutine.title, serverRoutine.icon)
            }
        }
    }
}
