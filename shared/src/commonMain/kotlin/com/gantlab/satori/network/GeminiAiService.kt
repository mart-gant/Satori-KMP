package com.gantlab.satori.network

import com.gantlab.satori.settings.SettingsManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GeminiAiService(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val settings: SettingsManager
) : AiService {

    override suspend fun getInsights(dataSummary: String): String {
        val token = settings.authToken ?: return "Zaloguj się, aby otrzymać analizę AI."

        return try {
            val response: String = httpClient.post("$baseUrl/ai/insight") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Text.Plain)
                setBody(buildPrompt(dataSummary))
            }.body()
            response
        } catch (e: Exception) {
            "Błąd połączenia z serwerem AI: ${e.message}"
        }
    }

    private fun buildPrompt(dataSummary: String): String {
        return "Jesteś asystentem aplikacji Satori, która pomaga w zarządzaniu przebodźcowaniem i trenowaniu funkcji poznawczych. " +
               "Oto dane użytkownika z ostatniego okresu:\n$dataSummary\n" +
               "Przeanalizuj te dane, znajdź korelacje (np. wpływ nastroju na czas reakcji) i podaj 3 konkretne, krótkie porady w języku polskim."
    }
}
