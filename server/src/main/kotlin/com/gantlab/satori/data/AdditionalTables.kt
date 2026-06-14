package com.gantlab.satori.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object ReactionTable : Table("ReactionResult") {
    val id = long("id").autoIncrement()
    val userId = long("userId").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")
    val reactionTimeMs = long("reactionTimeMs")
    override val primaryKey = PrimaryKey(id)
}

object ChallengeTable : Table("ChallengeResult") {
    val id = long("id").autoIncrement()
    val userId = long("userId").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")
    val challengeType = varchar("challengeType", 50)
    val score = long("score")
    override val primaryKey = PrimaryKey(id)
}

object ServerRoutineTable : Table("Routine") {
    val id = long("id").autoIncrement()
    val externalId = long("externalId") 
    val userId = long("userId").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 100)
    val icon = varchar("icon", 10).nullable()
    val isActive = bool("isActive").default(true)
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, externalId)
    }
}

object ServerRoutineTaskTable : Table("RoutineTask") {
    val id = long("id").autoIncrement()
    val routineId = long("routineId").references(ServerRoutineTable.id, onDelete = ReferenceOption.CASCADE)
    val taskName = varchar("taskName", 100)
    val scheduledTime = varchar("scheduledTime", 10).nullable()
    val isCompletedToday = bool("isCompletedToday").default(false)
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(routineId, taskName)
    }
}

object SelfAssessmentTable : Table("SelfAssessmentResult") {
    val id = long("id").autoIncrement()
    val userId = long("userId").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")
    val attentionScore = long("attentionScore")
    val memoryScore = long("memoryScore")
    val executiveScore = long("executiveScore")
    override val primaryKey = PrimaryKey(id)
}
