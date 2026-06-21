package com.gantlab.satori.di

import com.gantlab.satori.db.*
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.network.AiService
import com.gantlab.satori.network.GeminiAiService
import com.gantlab.satori.getAnalytics
import com.gantlab.satori.Analytics
import com.gantlab.satori.DatabaseViewModel
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
import org.koin.core.module.dsl.viewModelOf

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

    single<SettingsManager> { SettingsManager() }
    single<NotificationManager> { DummyNotificationManager() }
    single<SatoriApiService> { SatoriApiService(get(), get(org.koin.core.qualifier.named("baseUrl"))) }
    single<com.gantlab.satori.network.SyncManager> { com.gantlab.satori.network.SyncManager(get(), get(), get()) }
    single<AiService> { GeminiAiService(get(), get(org.koin.core.qualifier.named("baseUrl")), get()) }
    
    // Repositories (sub-types for Use Cases)
    single<ReactionRepository> { get<SatoriRepository>() }
    single<MoodRepository> { get<SatoriRepository>() }
    single<ChallengeRepository> { get<SatoriRepository>() }
    single<RoutineRepository> { get<SatoriRepository>() }
    single<AssessmentRepository> { get<SatoriRepository>() }
    single<ScenarioRepository> { get<SatoriRepository>() }
    
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
    
    viewModelOf(::ReactionViewModel)
    viewModelOf(::MoodViewModel)
    viewModelOf(::RoutineViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SocialViewModel)
    viewModelOf(::AssessmentViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::DatabaseViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

// iOS and other platforms will add their SatoriRepository implementation to the modules list
fun initKoinPlatform(
    repository: SatoriRepository,
    notifications: NotificationManager,
    baseUrl: String,
): KoinApplication = initKoin {
    modules(
        module {
            single<SatoriRepository> { repository }
            single<NotificationManager> { notifications }
            single<String>(qualifier = org.koin.core.qualifier.named("baseUrl")) { baseUrl }
        },
    )
}
