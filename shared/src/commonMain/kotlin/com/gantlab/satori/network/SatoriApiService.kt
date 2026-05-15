package com.gantlab.satori.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SatoriApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    // Replace with your server IP for physical devices, or localhost for emulator
    private val baseUrl = "http://10.0.2.2:8080" 

    suspend fun postMood(moodScore: Long, energyScore: Long, note: String?): MoodResponse? {
        return try {
            client.post("$baseUrl/mood") {
                contentType(ContentType.Application.Json)
                setBody(MoodRequest(moodScore, energyScore, note))
            }.body()
        } catch (e: Exception) {
            println("NETWORK ERROR: ${e.message}")
            null
        }
    }

    suspend fun getMoodHistory(): List<MoodResponse> {
        return try {
            client.get("$baseUrl/mood").body()
        } catch (e: Exception) {
            println("NETWORK ERROR: ${e.message}")
            emptyList()
        }
    }
}
