package com.gantlab.satori.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gantlab.satori.data.UserTable
import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.network.AuthResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService {
    private var jwtSecret = ""
    private var jwtIssuer = ""
    private var jwtAudience = ""
    private var googleClientId = ""

    fun initFromConfig(config: ApplicationConfig) {
        jwtSecret = config.property("jwt.secret").getString()
        jwtIssuer = config.property("jwt.issuer").getString()
        jwtAudience = config.property("jwt.audience").getString()
        googleClientId = config.propertyOrNull("google.clientId")?.getString() ?: ""
    }

    private fun generateToken(userId: Long, username: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("username", username)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000 * 24 * 30)) // 30 days
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    fun loginWithGoogle(idToken: String): AuthResponse? {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(listOf(googleClientId))
            .build()

        val token = try { verifier.verify(idToken) } catch (e: Exception) { null } ?: return null
        val payload = token.payload
        val email = payload.email
        val name = payload["name"] as? String ?: email

        return transaction {
            val user = UserTable.selectAll().where { UserTable.username eq email }.singleOrNull()
            val userId = if (user == null) {
                UserTable.insert {
                    it[username] = email
                    it[passwordHash] = "" // Google user has no password
                } get UserTable.id
            } else {
                user[UserTable.id]
            }
            
            AuthResponse(generateToken(userId, email), name)
        }
    }

    fun register(request: AuthRequest): Boolean {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        return try {
            transaction {
                UserTable.insert {
                    it[username] = request.username
                    it[passwordHash] = hashedPassword
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun login(request: AuthRequest): AuthResponse? {
        val user = transaction {
            UserTable.selectAll().where { UserTable.username eq request.username }.singleOrNull()
        }

        return if (user != null && user[UserTable.passwordHash].isNotEmpty() && BCrypt.checkpw(request.password, user[UserTable.passwordHash])) {
            AuthResponse(generateToken(user[UserTable.id], user[UserTable.username]), user[UserTable.username])
        } else {
            null
        }
    }

    fun deleteAccount(userId: Long): Boolean {
        return transaction {
            UserTable.deleteWhere { UserTable.id eq userId } > 0
        }
    }

    fun getJwtSecret() = jwtSecret
    fun getJwtIssuer() = jwtIssuer
    fun getJwtAudience() = jwtAudience
}
