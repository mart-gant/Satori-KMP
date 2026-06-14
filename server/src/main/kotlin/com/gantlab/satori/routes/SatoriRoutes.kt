package com.gantlab.satori.routes

import com.gantlab.satori.network.*
import com.gantlab.satori.service.SatoriService
import com.gantlab.satori.service.ServerAiService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.satoriRoutes() {
    val satoriService by inject<SatoriService>()
    val aiService by inject<ServerAiService>()

    authenticate("auth-jwt") {
        route("/ai") {
            post("/insight") {
                val prompt = call.receiveText()
                val insight = aiService.generateInsight(prompt)
                call.respondText(insight)
            }
        }
        
        route("/mood") {
// ...
            post {
                val userId = call.getUserId() ?: return@post
                val request = call.receive<MoodRequest>()
                call.respond(satoriService.addMood(userId, request))
            }
            get {
                val userId = call.getUserId() ?: return@get
                call.respond(satoriService.getMoodHistory(userId))
            }
        }

        route("/reaction") {
            post {
                val userId = call.getUserId() ?: return@post
                val request = call.receive<ReactionResultRequest>()
                satoriService.addReactionResult(userId, request)
                call.respond(HttpStatusCode.Created)
            }
            get {
                val userId = call.getUserId() ?: return@get
                call.respond(satoriService.getReactionResults(userId))
            }
        }

        route("/challenge") {
            post {
                val userId = call.getUserId() ?: return@post
                val request = call.receive<ChallengeResultRequest>()
                satoriService.addChallengeResult(userId, request)
                call.respond(HttpStatusCode.Created)
            }
            get {
                val userId = call.getUserId() ?: return@get
                call.respond(satoriService.getChallengeResults(userId))
            }
        }

        route("/self-assessment") {
            post {
                val userId = call.getUserId() ?: return@post
                val request = call.receive<SelfAssessmentRequest>()
                satoriService.addSelfAssessment(userId, request)
                call.respond(HttpStatusCode.Created)
            }
            get {
                val userId = call.getUserId() ?: return@get
                call.respond(satoriService.getSelfAssessmentHistory(userId))
            }
        }

        route("/routines") {
            post("/sync") {
                val userId = call.getUserId() ?: return@post
                val request = call.receive<List<RoutineSyncRequest>>()
                call.respond(satoriService.syncRoutines(userId, request))
            }
        }
    }
}

private suspend fun ApplicationCall.getUserId(): Long? {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asLong()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized)
    }
    return userId
}
