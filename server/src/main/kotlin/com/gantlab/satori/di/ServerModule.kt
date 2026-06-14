package com.gantlab.satori.di

import com.gantlab.satori.service.AuthService
import com.gantlab.satori.service.SatoriService
import com.gantlab.satori.service.ServerAiService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module

val serverModule = module {
    single { 
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single { AuthService() }
    single { SatoriService() }
    single { ServerAiService(get()) }
}
