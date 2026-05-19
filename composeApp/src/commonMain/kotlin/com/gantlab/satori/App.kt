package com.gantlab.satori

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gantlab.satori.ui.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(initialRoute: String? = null) {
    val viewModel: AppViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(initialRoute) {
        if (initialRoute != null && uiState.isOnboardingCompleted) {
            navController.navigate(initialRoute)
        }
    }

    SatoriTheme(
        highContrast = uiState.highContrast,
        largeFont = uiState.largeFont
    ) {
        val startDestination = if (uiState.isOnboardingCompleted) Routes.HOME else Routes.ONBOARDING

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { if (uiState.animationsEnabled) fadeIn(tween(300)) else EnterTransition.None },
            exitTransition = { if (uiState.animationsEnabled) fadeOut(tween(300)) else ExitTransition.None }
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onComplete = { nickname ->
                    viewModel.updateNickname(nickname)
                    viewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToTest = { navController.navigate(Routes.REACTION_TEST) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToReports = { navController.navigate(Routes.REPORTS) },
                    onNavigateToRoutines = { navController.navigate(Routes.ROUTINES) },
                    onNavigateToMood = { navController.navigate(Routes.MOOD_LOG) },
                    onNavigateToTips = { navController.navigate(Routes.TIPS) },
                    onNavigateToScenarios = { navController.navigate(Routes.SCENARIOS) },
                    onNavigateToSelfAssessment = { navController.navigate(Routes.SELF_ASSESSMENT) },
                    onNavigateToColorClash = { navController.navigate(Routes.COLOR_CLASH) },
                    onNavigateToMemoryGame = { navController.navigate(Routes.MEMORY_GAME) }
                )
            }
            composable(Routes.REACTION_TEST) {
                ReactionTestScreen(
                    onResult = { result ->
                        viewModel.saveReactionTime(result)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.COLOR_CLASH) {
                ColorClashScreen(
                    onResult = { score ->
                        viewModel.saveChallengeResult("color_clash", score)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MEMORY_GAME) {
                MemoryGameScreen(
                    onResult = { score ->
                        viewModel.saveChallengeResult("memory_game", score)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SELF_ASSESSMENT) {
                SelfAssessmentScreen(
                    onSave = viewModel::saveSelfAssessment,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ROUTINES) {
                RoutineScreen(
                    routines = uiState.routines,
                    tasks = uiState.routineTasks,
                    onAddRoutine = viewModel::addRoutine,
                    onDeleteRoutine = viewModel::deleteRoutine,
                    onAddTask = viewModel::addTaskToRoutine,
                    onUpdateTaskStatus = viewModel::updateTaskCompletion,
                    onUpdateTaskName = viewModel::updateTaskDetails,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MOOD_LOG) {
                MoodLoggingScreen(
                    history = uiState.moodHistory,
                    onSaveMood = viewModel::saveMood,
                    onUpdateNote = viewModel::updateMoodNote,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TIPS) {
                OverstimulationTipsScreen(
                    tips = viewModel.getOverstimulationTips(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SCENARIOS) {
                SocialScenariosScreen(
                    scenarios = uiState.scenarios,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    nickname = uiState.nickname,
                    highContrast = uiState.highContrast,
                    largeFont = uiState.largeFont,
                    animationsEnabled = uiState.animationsEnabled,
                    isLoggedIn = uiState.isLoggedIn,
                    onNicknameChange = viewModel::updateNickname,
                    onHighContrastChange = viewModel::toggleHighContrast,
                    onLargeFontChange = viewModel::toggleLargeFont,
                    onAnimationsChange = viewModel::toggleAnimations,
                    onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                    onNavigateToAuth = { navController.navigate(Routes.AUTH) },
                    onLogout = viewModel::logout,
                    onExportData = {
                        val csvData = viewModel.getExportData()
                        getPlatform().shareText(csvData) // Or use a more specific file sharing if available
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(
                    results = uiState.results,
                    moodHistory = uiState.moodHistory,
                    taskCompletions = uiState.taskCompletions,
                    challengeResults = uiState.challengeResults,
                    selfAssessmentHistory = uiState.selfAssessmentHistory,
                    aiInsight = uiState.aiInsight,
                    onGetAiInsight = viewModel::getAiInsights,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.AUTH) {
                AuthScreen(
                    onLogin = { u, p ->
                        viewModel.login(u, p) { success ->
                            if (success) navController.popBackStack()
                        }
                    },
                    onRegister = { u, p ->
                        viewModel.register(u, p) { success ->
                            // After register, we could auto-login or just switch to login mode
                            // For now let's just stay on the screen or show a success message
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
