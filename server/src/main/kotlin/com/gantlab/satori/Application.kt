package com.gantlab.satori

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gantlab.satori.data.DatabaseConfig
import com.gantlab.satori.di.serverModule
import com.gantlab.satori.routes.authRoutes
import com.gantlab.satori.routes.satoriRoutes
import com.gantlab.satori.service.AuthService
import com.gantlab.satori.service.ServerAiService
import com.gantlab.satori.network.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Inicjalizacja bazy danych z konfiguracji
    DatabaseConfig.init(environment.config)

    install(Koin) {
        modules(serverModule)
    }

    install(ContentNegotiation) {
        json()
    }

    // Walidacja danych wejściowych
    install(RequestValidation) {
        validate<AuthRequest> { request ->
            when {
                request.username.isBlank() -> ValidationResult.Invalid("Username cannot be empty")
                request.password.length < 6 -> ValidationResult.Invalid("Password must be at least 6 characters long")
                else -> ValidationResult.Valid
            }
        }
        validate<MoodRequest> { request ->
            when {
                request.moodScore !in 1..10 -> ValidationResult.Invalid("Mood score must be between 1 and 10")
                request.energyScore !in 1..10 -> ValidationResult.Invalid("Energy score must be between 1 and 10")
                else -> ValidationResult.Valid
            }
        }
        validate<ReactionResultRequest> { request ->
            if (request.reactionTimeMs <= 0) ValidationResult.Invalid("Reaction time must be positive")
            else ValidationResult.Valid
        }
    }

    // Globalna obsługa błędów
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.reasons.joinToString()))
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Internal Server Error")))
        }
    }

    val authService by inject<AuthService>()
    val aiService by inject<ServerAiService>()
    authService.initFromConfig(environment.config)
    aiService.initFromConfig(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(authService.getJwtSecret()))
                    .withAudience(authService.getJwtAudience())
                    .withIssuer(authService.getJwtIssuer())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(authService.getJwtAudience())) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/") {
            call.respondText("Satori Server is Running")
        }
        
        authRoutes()
        satoriRoutes()
    }
}
