package com.gantlab.satori.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SatoriApiService {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private val baseUrl = "http://10.0.2.2:8080" 

    suspend fun register(authRequest: AuthRequest): Boolean {
        return try {
            val response = client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(authRequest)
            }
            response.status == HttpStatusCode.Created
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(authRequest: AuthRequest): AuthResponse? {
        return try {
            client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(authRequest)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun postMood(token: String, moodScore: Long, energyScore: Long, note: String?): MoodResponse? {
        return try {
            client.post("$baseUrl/mood") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MoodRequest(moodScore, energyScore, note))
            }.body()
        } catch (e: Exception) {
            println("NETWORK ERROR: ${e.message}")
            null
        }
    }

    suspend fun getMoodHistory(token: String): List<MoodResponse> {
        return try {
            client.get("$baseUrl/mood") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            println("NETWORK ERROR: ${e.message}")
            emptyList()
        }
    }
}
