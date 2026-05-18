package com.gantlab.satori

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gantlab.satori.data.MoodTable
import com.gantlab.satori.data.UserTable
import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.network.AuthResponse
import com.gantlab.satori.network.MoodRequest
import com.gantlab.satori.network.MoodResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

private const val JWT_SECRET = "satori-super-secret-key-12345"
private const val JWT_ISSUER = "http://0.0.0.0:8080/"
private const val JWT_AUDIENCE = "satori-users"

fun main() {
    Database.connect("jdbc:h2:file:./satori_db;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(UserTable, MoodTable)
    }

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JWT_AUDIENCE)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Satori Server is Running")
        }

        post("/register") {
            val request = call.receive<AuthRequest>()
            val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
            
            try {
                transaction {
                    UserTable.insert {
                        it[username] = request.username
                        it[passwordHash] = hashedPassword
                    }
                }
                call.respond(HttpStatusCode.Created, "User registered successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
            }
        }

        post("/login") {
            val request = call.receive<AuthRequest>()
            val user = transaction {
                UserTable.selectAll().where { UserTable.username eq request.username }.singleOrNull()
            }

            if (user != null && BCrypt.checkpw(request.password, user[UserTable.passwordHash])) {
                val token = JWT.create()
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .withClaim("username", user[UserTable.username])
                    .withClaim("userId", user[UserTable.id])
                    .withExpiresAt(Date(System.currentTimeMillis() + 3600000 * 24 * 30)) // 30 days
                    .sign(Algorithm.HMAC256(JWT_SECRET))
                
                call.respond(AuthResponse(token, user[UserTable.username]))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            }
        }

        authenticate("auth-jwt") {
            route("/mood") {
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    
                    val request = call.receive<MoodRequest>()
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
                    
                    call.respond(MoodResponse(id, timestamp, request.moodScore, request.energyScore, request.note))
                }

                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                    val history = transaction {
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
                    call.respond(history)
                }
            }
        }
    }
}
