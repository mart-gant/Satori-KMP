package com.gantlab.satori

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.gantlab.satori.di.initKoin
import com.gantlab.satori.domain.usecase.*
import com.gantlab.satori.presentation.viewmodel.*
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin {
        modules(module {
            // Dostarczamy atrapy Use Case'ów lokalnie
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
            factoryOf(::GetReportsDataUseCase)

            single<String>(qualifier = org.koin.core.qualifier.named("baseUrl")) { "http://localhost:8080" }
        })
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
