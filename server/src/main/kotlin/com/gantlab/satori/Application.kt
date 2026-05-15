package com.gantlab.satori

import com.gantlab.satori.data.MoodTable
import com.gantlab.satori.network.MoodRequest
import com.gantlab.satori.network.MoodResponse
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    // Initialize Database
    Database.connect("jdbc:h2:file:./satori_db;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(MoodTable)
    }

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Satori Server is Running")
        }

        route("/mood") {
            post {
                val request = call.receive<MoodRequest>()
                val timestamp = System.currentTimeMillis()
                
                val id = transaction {
                    MoodTable.insert {
                        it[MoodTable.timestamp] = timestamp
                        it[MoodTable.moodScore] = request.moodScore
                        it[MoodTable.energyScore] = request.energyScore
                        it[MoodTable.note] = request.note
                    } get MoodTable.id
                }
                
                call.respond(MoodResponse(id, timestamp, request.moodScore, request.energyScore, request.note))
            }

            get {
                val history = transaction {
                    MoodTable.selectAll().map {
                        MoodResponse(
                            it[MoodTable.id],
                            it[MoodTable.timestamp],
                            it[MoodTable.moodScore],
                            it[MoodTable.energyScore],
                            it[MoodTable.note]
                        )
                    }
                }
                call.respond(history)
            }
        }
    }
}
