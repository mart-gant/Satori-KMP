package com.gantlab.satori.network

interface AiService {
    suspend fun getInsights(dataSummary: String): String
}
