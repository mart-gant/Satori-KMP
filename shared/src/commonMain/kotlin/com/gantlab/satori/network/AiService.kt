package com.gantlab.satori.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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

class AiService(private val httpClient: HttpClient) {
    private val apiKey = "" // Użytkownik powinien tu podać swój klucz

    suspend fun getInsights(dataSummary: String): String {
        if (apiKey.isEmpty()) return "Brak klucza API Gemini. Skonfiguruj go w AiService.kt, aby otrzymać inteligentną analizę."

        return try {
            val response: GeminiResponse = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(listOf(Content(listOf(Part(
                    "Jesteś asystentem aplikacji Satori, która pomaga w zarządzaniu przebodźcowaniem i trenowaniu funkcji poznawczych. " +
                    "Oto dane użytkownika z ostatniego okresu:\n$dataSummary\n" +
                    "Przeanalizuj te dane, znajdź korelacje (np. wpływ nastroju na czas reakcji) i podaj 3 konkretne, krótkie porady w języku polskim."
                ))))))
            }.body()
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Nie udało się uzyskać analizy."
        } catch (e: Exception) {
            "Błąd połączenia z AI: ${e.message}"
        }
    }
}
