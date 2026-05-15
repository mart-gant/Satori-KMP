package com.gantlab.satori.data

import org.jetbrains.exposed.sql.Table

object MoodTable : Table("MoodEntry") {
    val id = long("id").autoIncrement()
    val timestamp = long("timestamp")
    val moodScore = long("moodScore")
    val energyScore = long("energyScore")
    val note = text("note").nullable()

    override val primaryKey = PrimaryKey(id)
}
