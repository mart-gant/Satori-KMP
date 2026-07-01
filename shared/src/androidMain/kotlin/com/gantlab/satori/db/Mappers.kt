package com.gantlab.satori.db

import com.gantlab.satori.domain.model.*
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.ChallengeResult
import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask
import com.gantlab.satori.db.SelfAssessmentResult
import com.gantlab.satori.db.SocialScenario

fun ReactionResult.toDomain() = DomainReactionResult(
    id = id,
    timestamp = timestamp,
    reactionTimeMs = reactionTimeMs,
    synced = synced == 1L
)

fun MoodEntry.toDomain() = DomainMoodEntry(
    id = id,
    timestamp = timestamp,
    moodScore = moodScore.toInt(),
    energyScore = energyScore.toInt(),
    note = note,
    synced = synced == 1L
)

fun ChallengeResult.toDomain() = DomainChallengeResult(
    id = id,
    timestamp = timestamp,
    challengeType = challengeType,
    score = score,
    synced = synced == 1L
)

fun Routine.toDomain() = DomainRoutine(
    id = id,
    title = title,
    icon = icon,
    isActive = isActive == 1L
)

fun RoutineTask.toDomain() = DomainRoutineTask(
    id = id,
    routineId = routineId,
    taskName = taskName,
    scheduledTime = scheduledTime,
    isCompletedToday = isCompletedToday == 1L
)

fun SelfAssessmentResult.toDomain() = DomainSelfAssessmentResult(
    id = id,
    timestamp = timestamp,
    attentionScore = attentionScore,
    memoryScore = memoryScore,
    executiveScore = executiveScore,
    synced = synced == 1L
)

fun SocialScenario.toDomain() = DomainSocialScenario(
    id = id,
    title = title,
    description = description,
    steps = steps,
    category = category
)
