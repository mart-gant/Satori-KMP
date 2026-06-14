package com.gantlab.satori.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class SatoriApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun register(authRequest: AuthRequest): Boolean = safeApiCall {
        val response = client.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
        response.status == HttpStatusCode.Created
    } ?: false

    suspend fun login(authRequest: AuthRequest): AuthResponse? = safeApiCall {
        client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }.body()
    }

    suspend fun googleLogin(idToken: String): AuthResponse? = safeApiCall {
        client.post("$baseUrl/google-login") {
            contentType(ContentType.Text.Plain)
            setBody(idToken)
        }.body()
    }

    suspend fun deleteAccount(token: String): Boolean = safeApiCall {
        val response = client.delete("$baseUrl/delete-account") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        response.status == HttpStatusCode.OK
    } ?: false

    suspend fun postMood(token: String, moodScore: Long, energyScore: Long, note: String?): MoodResponse? = safeApiCall {
        client.post("$baseUrl/mood") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(MoodRequest(moodScore, energyScore, note))
        }.body()
    }

    suspend fun getMoodHistory(token: String): List<MoodResponse> = safeApiCall {
        client.get("$baseUrl/mood") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<MoodResponse>>()
    } ?: emptyList()

    suspend fun postReaction(token: String, timestamp: Long, reactionTimeMs: Long): Boolean = safeApiCall {
        val response = client.post("$baseUrl/reaction") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ReactionResultRequest(timestamp, reactionTimeMs))
        }
        response.status == HttpStatusCode.Created
    } ?: false

    suspend fun getReactions(token: String): List<ReactionResultRequest> = safeApiCall {
        client.get("$baseUrl/reaction") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<ReactionResultRequest>>()
    } ?: emptyList()

    suspend fun postChallenge(token: String, timestamp: Long, type: String, score: Long): Boolean = safeApiCall {
        val response = client.post("$baseUrl/challenge") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ChallengeResultRequest(timestamp, type, score))
        }
        response.status == HttpStatusCode.Created
    } ?: false

    suspend fun getChallenges(token: String): List<ChallengeResultRequest> = safeApiCall {
        client.get("$baseUrl/challenge") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<ChallengeResultRequest>>()
    } ?: emptyList()

    suspend fun postSelfAssessment(token: String, timestamp: Long, attention: Long, memory: Long, executive: Long): Boolean = safeApiCall {
        val response = client.post("$baseUrl/self-assessment") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SelfAssessmentRequest(timestamp, attention, memory, executive))
        }
        response.status == HttpStatusCode.Created
    } ?: false

    suspend fun getSelfAssessmentHistory(token: String): List<SelfAssessmentRequest> = safeApiCall {
        client.get("$baseUrl/self-assessment") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<SelfAssessmentRequest>>()
    } ?: emptyList()

    suspend fun syncRoutines(token: String, routines: List<RoutineSyncRequest>): List<RoutineSyncRequest> = safeApiCall {
        client.post("$baseUrl/routines/sync") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(routines)
        }.body<List<RoutineSyncRequest>>()
    } ?: emptyList()

    private suspend fun <T> safeApiCall(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            // Here you could log to Crashlytics or a custom logger
            println("API Error: ${e.message}")
            null
        }
    }
}
