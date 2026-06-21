package com.gantlab.satori.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainReactionResult(
    val id: Long,
    val timestamp: Long,
    val reactionTimeMs: Long,
    val synced: Boolean,
)

@Serializable
data class DomainMoodEntry(
    val id: Long,
    val timestamp: Long,
    val moodScore: Int,
    val energyScore: Int,
    val note: String?,
    val synced: Boolean,
)

@Serializable
data class DomainChallengeResult(
    val id: Long,
    val timestamp: Long,
    val challengeType: String,
    val score: Long,
    val synced: Boolean,
)

@Serializable
data class DomainRoutine(
    val id: Long,
    val title: String,
    val icon: String?,
    val isActive: Boolean,
)

@Serializable
data class DomainRoutineTask(
    val id: Long,
    val routineId: Long,
    val taskName: String,
    val scheduledTime: String?,
    val isCompletedToday: Boolean,
)

@Serializable
data class DomainSelfAssessmentResult(
    val id: Long,
    val timestamp: Long,
    val attentionScore: Long,
    val memoryScore: Long,
    val executiveScore: Long,
    val synced: Boolean,
)

@Serializable
data class DomainTaskCompletion(
    val id: Long,
    val taskId: Long,
    val timestamp: Long,
)

@Serializable
data class DomainSocialScenario(
    val id: Long,
    val title: String,
    val description: String,
    val steps: String,
    val category: String,
)
