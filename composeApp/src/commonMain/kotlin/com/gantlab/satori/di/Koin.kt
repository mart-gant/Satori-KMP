package com.gantlab.satori.di

import com.gantlab.satori.db.ReactionRepository
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.network.AiService
import com.gantlab.satori.network.GeminiAiService
import com.gantlab.satori.getAnalytics
import com.gantlab.satori.Analytics
import com.gantlab.satori.DatabaseViewModel
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.notifications.NotificationManager
import com.gantlab.satori.notifications.DummyNotificationManager
import com.gantlab.satori.domain.usecase.*
import com.gantlab.satori.presentation.viewmodel.*
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.Module
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.factoryOf

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val commonModule: Module = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
        }
    }

    single<String>(qualifier = org.koin.core.qualifier.named("baseUrl")) { "http://10.0.2.2:8080" }

    single<SettingsManager> { SettingsManager() }
    single<NotificationManager> { DummyNotificationManager() }
    single<SatoriApiService> { SatoriApiService(get(), get(org.koin.core.qualifier.named("baseUrl"))) }
    single<AiService> { GeminiAiService(get(), "") }
    single<SatoriRepository> { 
        SatoriRepository(
            database = com.gantlab.satori.db.SatoriDatabase(get<DriverFactory>().createDriver()),
            api = get(),
            settings = get()
        )
    }
    single<ReactionRepository> { get<SatoriRepository>() }
    single<Analytics> { getAnalytics() }
    
    // Use Cases
    factoryOf(::SaveReactionUseCase)
    factoryOf(::GetReactionResultsUseCase)
    factoryOf(::GetChallengeResultsUseCase)
    factoryOf(::SaveMoodUseCase)
    factoryOf(::UpdateMoodNoteUseCase)
    factoryOf(::GetMoodDataUseCase)
    factoryOf(::SaveChallengeResultUseCase)
    factoryOf(::SyncDataUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::ExportDataUseCase)
    factoryOf(::GetDashboardDataUseCase)
    factoryOf(::GetAiInsightUseCase)
    factoryOf(::SaveSelfAssessmentUseCase)
    factoryOf(::GetSelfAssessmentHistoryUseCase)
    factoryOf(::GetRoutineDataUseCase)
    factoryOf(::UpdateRoutineTaskUseCase)
    factoryOf(::GetSocialScenariosUseCase)
    factoryOf(::GetOverstimulationTipsUseCase)
    
    factoryOf(::ReactionViewModel)
    factoryOf(::MoodViewModel)
    factoryOf(::RoutineViewModel)
    factoryOf(::AuthViewModel)
    factoryOf(::SettingsViewModel)
    factoryOf(::SocialViewModel)
    factoryOf(::AssessmentViewModel)
    factoryOf(::DashboardViewModel)
    factoryOf(::DatabaseViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

fun initKoinIos(
    factory: DriverFactory,
    notifications: NotificationManager
): KoinApplication = initKoin {
    modules(module {
        single<DriverFactory> { factory }
        single<NotificationManager> { notifications }
    })
}
