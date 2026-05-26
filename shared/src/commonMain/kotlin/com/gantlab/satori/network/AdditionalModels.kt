package com.gantlab.satori.network

import kotlinx.serialization.Serializable

@Serializable
data class ReactionResultRequest(val timestamp: Long, val reactionTimeMs: Long)

@Serializable
data class ChallengeResultRequest(val timestamp: Long, val challengeType: String, val score: Long)

@Serializable
data class RoutineRequest(val title: String, val icon: String?, val isActive: Boolean)

@Serializable
data class RoutineResponse(val id: Long, val title: String, val icon: String?, val isActive: Boolean)

@Serializable
data class RoutineTaskRequest(val routineId: Long, val taskName: String, val scheduledTime: String?)

@Serializable
data class RoutineTaskResponse(val id: Long, val routineId: Long, val taskName: String, val scheduledTime: String?, val isCompletedToday: Boolean)

@Serializable
data class SelfAssessmentRequest(val timestamp: Long, val attentionScore: Long, val memoryScore: Long, val executiveScore: Long)
