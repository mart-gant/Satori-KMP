package com.gantlab.satori.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun init(config: ApplicationConfig) {
        val driver = config.property("storage.driverClassName").getString()
        val url = config.property("storage.jdbcURL").getString()
        val user = config.property("storage.dbUser").getString()
        val password = config.property("storage.dbPassword").getString()

        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = user
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(
                UserTable,
                MoodTable,
                ReactionTable,
                ChallengeTable,
                ServerRoutineTable,
                ServerRoutineTaskTable,
                SelfAssessmentTable
            )
        }
    }
}
