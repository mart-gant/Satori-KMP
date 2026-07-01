package com.gantlab.satori.db

import com.gantlab.satori.domain.model.*

fun ReactionResult.toDomainReaction() = DomainReactionResult(
    id = this.id,
    timestamp = this.timestamp,
    reactionTimeMs = this.reactionTimeMs,
    synced = this.synced == 1L
)

fun MoodEntry.toDomainMood() = DomainMoodEntry(
    id = this.id,
    timestamp = this.timestamp,
    moodScore = this.moodScore.toInt(),
    energyScore = this.energyScore.toInt(),
    note = this.note,
    synced = this.synced == 1L
)

fun ChallengeResult.toDomainChallenge() = DomainChallengeResult(
    id = this.id,
    timestamp = this.timestamp,
    challengeType = this.challengeType,
    score = this.score,
    synced = this.synced == 1L
)

fun Routine.toDomainRoutine() = DomainRoutine(
    id = this.id,
    title = this.title,
    icon = this.icon,
    isActive = this.isActive == 1L
)

fun RoutineTask.toDomainTask() = DomainRoutineTask(
    id = this.id,
    routineId = this.routineId,
    taskName = this.taskName,
    scheduledTime = this.scheduledTime,
    isCompletedToday = this.isCompletedToday == 1L
)

fun RoutineTaskHistory.toDomainCompletion() = DomainTaskCompletion(
    id = this.id,
    taskId = this.taskId,
    timestamp = this.timestamp
)

fun SelfAssessmentResult.toDomainAssessment() = DomainSelfAssessmentResult(
    id = this.id,
    timestamp = this.timestamp,
    attentionScore = this.attentionScore,
    memoryScore = this.memoryScore,
    executiveScore = this.executiveScore,
    synced = this.synced == 1L
)

fun SocialScenario.toDomainScenario() = DomainSocialScenario(
    id = this.id,
    title = this.title,
    description = this.description,
    steps = this.steps,
    category = this.category
)
