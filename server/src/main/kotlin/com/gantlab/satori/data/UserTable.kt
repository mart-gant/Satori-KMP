package com.gantlab.satori.data

import org.jetbrains.exposed.sql.Table

object UserTable : Table("User") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("passwordHash", 100)

    override val primaryKey = PrimaryKey(id)
}
