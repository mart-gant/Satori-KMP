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
import com.gantlab.satori.presentation.viewmodel.*
import com.gantlab.satori.ui.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(initialRoute: String? = null) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()

    val reactionViewModel: ReactionViewModel = koinViewModel()
    val reactionState by reactionViewModel.uiState.collectAsState()
    
    val moodViewModel: MoodViewModel = koinViewModel()
    val routineViewModel: RoutineViewModel = koinViewModel()
    val socialViewModel: SocialViewModel = koinViewModel()
    val assessmentViewModel: AssessmentViewModel = koinViewModel()
    val dashboardViewModel: DashboardViewModel = koinViewModel()
    val databaseViewModel: DatabaseViewModel = koinViewModel()

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialRoute) {
        if (initialRoute != null && (settingsState.isOnboardingCompleted)) {
            navController.navigate(initialRoute)
        }
    }

    SatoriTheme(
        highContrast = settingsState.highContrast,
        largeFont = settingsState.largeFont,
    ) {
        val startDestination = if (settingsState.isOnboardingCompleted) Routes.HOME else Routes.ONBOARDING

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { if (settingsState.animationsEnabled) fadeIn(tween(300)) else EnterTransition.None },
            exitTransition = { if (settingsState.animationsEnabled) fadeOut(tween(300)) else ExitTransition.None }
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen { nickname, aiConsent ->
                    settingsViewModel.updateNickname(nickname)
                    settingsViewModel.toggleAiConsent(aiConsent)
                    settingsViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
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
                        reactionViewModel.saveReactionTime(result)
                        dashboardViewModel.refreshDashboard()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.COLOR_CLASH) {
                ColorClashScreen(
                    onResult = { score ->
                        reactionViewModel.saveChallengeResult("color_clash", score)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MEMORY_GAME) {
                MemoryGameScreen(
                    onResult = { score ->
                        reactionViewModel.saveChallengeResult("memory_game", score)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SELF_ASSESSMENT) {
                SelfAssessmentScreen(
                    onSave = { att, mem, exec ->
                        assessmentViewModel.saveSelfAssessment(att, mem, exec)
                        dashboardViewModel.refreshDashboard()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ROUTINES) {
                val routineState by routineViewModel.uiState.collectAsState()
                RoutineScreen(
                    routines = routineState.routines,
                    tasks = routineState.routineTasks,
                    onAddRoutine = routineViewModel::addRoutine,
                    onDeleteRoutine = routineViewModel::deleteRoutine,
                    onAddTask = routineViewModel::addTaskToRoutine,
                    onUpdateTaskStatus = { id, done ->
                        routineViewModel.updateTaskCompletion(id, done)
                        dashboardViewModel.refreshDashboard()
                    },
                    onUpdateTaskName = routineViewModel::updateTaskDetails,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MOOD_LOG) {
                val moodState by moodViewModel.uiState.collectAsState()
                MoodLoggingScreen(
                    history = moodState.moodHistory,
                    onSaveMood = { mood, energy, note ->
                        moodViewModel.saveMood(mood, energy, note)
                        dashboardViewModel.refreshDashboard()
                    },
                    onUpdateNote = moodViewModel::updateMoodNote,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TIPS) {
                OverstimulationTipsScreen(
                    tips = socialViewModel.getOverstimulationTips(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SCENARIOS) {
                val socialState by socialViewModel.uiState.collectAsState()
                SocialScenariosScreen(
                    scenarios = socialState.scenarios,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    nickname = settingsState.nickname,
                    highContrast = settingsState.highContrast,
                    largeFont = settingsState.largeFont,
                    animationsEnabled = settingsState.animationsEnabled,
                    aiConsentGranted = settingsState.aiConsentGranted,
                    language = settingsState.language,
                    isLoggedIn = authState.isLoggedIn,
                    onNicknameChange = settingsViewModel::updateNickname,
                    onHighContrastChange = settingsViewModel::toggleHighContrast,
                    onLargeFontChange = settingsViewModel::toggleLargeFont,
                    onAnimationsChange = settingsViewModel::toggleAnimations,
                    onAiConsentChange = settingsViewModel::toggleAiConsent,
                    onLanguageChange = { lang ->
                        settingsViewModel.updateLanguage(lang)
                        getPlatform().setLanguage(lang)
                    },
                    onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                    onNavigateToAuth = { navController.navigate(Routes.AUTH) },
                    onNavigateToDebug = { navController.navigate(Routes.DEBUG) },
                    onLogout = authViewModel::logout,
                    onExportData = {
                        scope.launch {
                            val csvData = settingsViewModel.getExportData()
                            getPlatform().shareText(csvData)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REPORTS) {
                val moodState by moodViewModel.uiState.collectAsState()
                val routineState by routineViewModel.uiState.collectAsState()
                val assessState by assessmentViewModel.uiState.collectAsState()
                val dashState by dashboardViewModel.uiState.collectAsState()
                ReportsScreen(
                    results = reactionState.results,
                    averageMs = reactionState.averageMs,
                    challengeResults = reactionState.challengeResults,
                    selfAssessmentHistory = assessState.selfAssessmentHistory,
                    reportsData = reactionState.reportsData,
                    aiInsight = dashState.aiInsight,
                    onGetAiInsight = dashboardViewModel::getAiInsights,
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
                        authViewModel.login(u, p) { success ->
                            if (success) {
                                navController.popBackStack()
                                dashboardViewModel.refreshDashboard()
                            }
                        }
                    },
                    onRegister = { u, p ->
                        authViewModel.register(u, p) { success -> }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.DEBUG) {
                DebugScreen(
                    viewModel = databaseViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
