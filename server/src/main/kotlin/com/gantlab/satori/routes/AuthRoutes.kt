package com.gantlab.satori.routes

import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    post("/register") {
        val request = call.receive<AuthRequest>()
        if (authService.register(request)) {
            call.respond(HttpStatusCode.Created, "User registered successfully")
        } else {
            call.respond(HttpStatusCode.Conflict, "Username already exists")
        }
    }

    post("/login") {
        val request = call.receive<AuthRequest>()
        val response = authService.login(request)
        if (response != null) {
            call.respond(response)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
        }
    }

    post("/google-login") {
        val idToken = call.receiveText()
        val response = authService.loginWithGoogle(idToken)
        if (response != null) {
            call.respond(response)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid Google token")
        }
    }

    authenticate("auth-jwt") {
        delete("/delete-account") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asLong()
            if (userId != null && authService.deleteAccount(userId)) {
                call.respond(HttpStatusCode.OK, "Account deleted")
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
