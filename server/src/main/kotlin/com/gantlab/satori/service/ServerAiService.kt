package com.gantlab.satori.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiRequest(val contents: List<Content>)
@Serializable
data class Content(val parts: List<Part>)
@Serializable
data class Part(val text: String)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>)
@Serializable
data class Candidate(val content: Content)

class ServerAiService(private val client: HttpClient) {
    private var apiKey = ""

    fun initFromConfig(config: ApplicationConfig) {
        apiKey = config.propertyOrNull("gemini.apiKey")?.getString() ?: ""
    }

    suspend fun generateInsight(prompt: String): String {
        if (apiKey.isBlank()) return "AI Analysis is currently unavailable (API Key missing)."

        return try {
            val response = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(listOf(Content(listOf(Part(prompt))))))
            }.body<GeminiResponse>()

            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No insight generated."
        } catch (e: Exception) {
            "Error generating AI insight: ${e.message}"
        }
    }
}
