package com.gantlab.satori.service

import com.gantlab.satori.data.*
import com.gantlab.satori.network.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SatoriService {

    fun addMood(userId: Long, request: MoodRequest): MoodResponse {
        val timestamp = System.currentTimeMillis()
        val id = transaction {
            MoodTable.insert {
                it[MoodTable.userId] = userId
                it[MoodTable.timestamp] = timestamp
                it[MoodTable.moodScore] = request.moodScore
                it[MoodTable.energyScore] = request.energyScore
                it[MoodTable.note] = request.note
            } get MoodTable.id
        }
        return MoodResponse(id, timestamp, request.moodScore, request.energyScore, request.note)
    }

    fun getMoodHistory(userId: Long): List<MoodResponse> {
        return transaction {
            MoodTable.selectAll().where { MoodTable.userId eq userId }.map {
                MoodResponse(
                    it[MoodTable.id],
                    it[MoodTable.timestamp],
                    it[MoodTable.moodScore],
                    it[MoodTable.energyScore],
                    it[MoodTable.note]
                )
            }
        }
    }

    fun addReactionResult(userId: Long, request: ReactionResultRequest) {
        transaction {
            ReactionTable.insert {
                it[ReactionTable.userId] = userId
                it[ReactionTable.timestamp] = request.timestamp
                it[ReactionTable.reactionTimeMs] = request.reactionTimeMs
            }
        }
    }

    fun getReactionResults(userId: Long): List<ReactionResultRequest> {
        return transaction {
            ReactionTable.selectAll().where { ReactionTable.userId eq userId }.map {
                ReactionResultRequest(it[ReactionTable.timestamp], it[ReactionTable.reactionTimeMs])
            }
        }
    }

    fun addChallengeResult(userId: Long, request: ChallengeResultRequest) {
        transaction {
            ChallengeTable.insert {
                it[ChallengeTable.userId] = userId
                it[ChallengeTable.timestamp] = request.timestamp
                it[ChallengeTable.challengeType] = request.challengeType
                it[ChallengeTable.score] = request.score
            }
        }
    }

    fun getChallengeResults(userId: Long): List<ChallengeResultRequest> {
        return transaction {
            ChallengeTable.selectAll().where { ChallengeTable.userId eq userId }.map {
                ChallengeResultRequest(it[ChallengeTable.timestamp], it[ChallengeTable.challengeType], it[ChallengeTable.score])
            }
        }
    }

    fun addSelfAssessment(userId: Long, request: SelfAssessmentRequest) {
        transaction {
            SelfAssessmentTable.insert {
                it[SelfAssessmentTable.userId] = userId
                it[SelfAssessmentTable.timestamp] = request.timestamp
                it[SelfAssessmentTable.attentionScore] = request.attentionScore
                it[SelfAssessmentTable.memoryScore] = request.memoryScore
                it[SelfAssessmentTable.executiveScore] = request.executiveScore
            }
        }
    }

    fun getSelfAssessmentHistory(userId: Long): List<SelfAssessmentRequest> {
        return transaction {
            SelfAssessmentTable.selectAll().where { SelfAssessmentTable.userId eq userId }.map {
                SelfAssessmentRequest(
                    it[SelfAssessmentTable.timestamp],
                    it[SelfAssessmentTable.attentionScore],
                    it[SelfAssessmentTable.memoryScore],
                    it[SelfAssessmentTable.executiveScore]
                )
            }
        }
    }

    fun syncRoutines(userId: Long, routines: List<RoutineSyncRequest>): List<RoutineSyncRequest> {
        return transaction {
            routines.forEach { routine ->
                val existing = ServerRoutineTable.selectAll().where { 
                    (ServerRoutineTable.userId eq userId) and (ServerRoutineTable.externalId eq routine.id) 
                }.singleOrNull()

                val internalId = if (existing == null) {
                    ServerRoutineTable.insert {
                        it[ServerRoutineTable.userId] = userId
                        it[ServerRoutineTable.externalId] = routine.id
                        it[title] = routine.title
                        it[icon] = routine.icon
                        it[isActive] = routine.isActive
                    } get ServerRoutineTable.id
                } else {
                    ServerRoutineTable.update({ ServerRoutineTable.id eq existing[ServerRoutineTable.id] }) {
                        it[title] = routine.title
                        it[icon] = routine.icon
                        it[isActive] = routine.isActive
                    }
                    existing[ServerRoutineTable.id]
                }

                // Sync Tasks
                routine.tasks.forEach { task ->
                    val taskExisting = ServerRoutineTaskTable.selectAll().where { 
                        (ServerRoutineTaskTable.routineId eq internalId) and (ServerRoutineTaskTable.taskName eq task.name)
                    }.singleOrNull()

                    if (taskExisting == null) {
                        ServerRoutineTaskTable.insert {
                            it[ServerRoutineTaskTable.routineId] = internalId
                            it[taskName] = task.name
                            it[scheduledTime] = task.time
                            it[isCompletedToday] = task.isCompleted
                        }
                    } else {
                        ServerRoutineTaskTable.update({ ServerRoutineTaskTable.id eq taskExisting[ServerRoutineTaskTable.id] }) {
                            it[scheduledTime] = task.time
                            it[isCompletedToday] = task.isCompleted
                        }
                    }
                }
            }
            
            // Return all routines from server for this user
            ServerRoutineTable.selectAll().where { ServerRoutineTable.userId eq userId }.map { r ->
                val tasks = ServerRoutineTaskTable.selectAll().where { ServerRoutineTaskTable.routineId eq r[ServerRoutineTable.id] }.map { t ->
                    RoutineTaskSyncRequest(t[ServerRoutineTaskTable.taskName], t[ServerRoutineTaskTable.scheduledTime], t[ServerRoutineTaskTable.isCompletedToday])
                }
                RoutineSyncRequest(r[ServerRoutineTable.externalId], r[ServerRoutineTable.title], r[ServerRoutineTable.icon], r[ServerRoutineTable.isActive], tasks)
            }
        }
    }
}
