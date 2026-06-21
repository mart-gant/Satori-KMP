package com.gantlab.satori

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialRoute) {
        if (initialRoute != null && settingsState.isOnboardingCompleted) {
            navController.navigate(initialRoute)
        }
    }

    SatoriTheme(
        highContrast = settingsState.highContrast,
        largeFont = settingsState.largeFont,
    ) {
        val startDestination =
            if (settingsState.isOnboardingCompleted) Routes.HOME else Routes.ONBOARDING
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        BoxWithConstraints {
            val isWideScreen = maxWidth > 600.dp
            val showSideBar = isWideScreen && settingsState.isOnboardingCompleted

            Row(Modifier.fillMaxSize()) {
                if (showSideBar) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        NavigationRailItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                            onClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(navController.graph.findStartDestination().route!!) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationRailItem(
                            icon = {
                                Icon(
                                    Icons.Default.Assessment,
                                    contentDescription = "Reports"
                                )
                            },
                            label = { Text("Reports") },
                            selected = currentDestination?.hierarchy?.any { it.route == Routes.REPORTS } == true,
                            onClick = {
                                navController.navigate(Routes.REPORTS) {
                                    popUpTo(navController.graph.findStartDestination().route!!) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationRailItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentDestination?.hierarchy?.any { it.route == Routes.PROFILE } == true,
                            onClick = {
                                navController.navigate(Routes.PROFILE) {
                                    popUpTo(navController.graph.findStartDestination().route!!) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding),
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
                            val attention by assessmentViewModel.attentionScore.collectAsState()
                            val memory by assessmentViewModel.memoryScore.collectAsState()
                            val executive by assessmentViewModel.executiveScore.collectAsState()

                            SelfAssessmentScreen(
                                attention = attention,
                                memory = memory,
                                executive = executive,
                                onAttentionChange = assessmentViewModel::updateAttention,
                                onMemoryChange = assessmentViewModel::updateMemory,
                                onExecutiveChange = assessmentViewModel::updateExecutive,
                                onSave = {
                                    assessmentViewModel.saveSelfAssessment()
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
                            val isSyncing by settingsViewModel.isSyncing.collectAsState()
                            ProfileScreen(
                                nickname = settingsState.nickname,
                                highContrast = settingsState.highContrast,
                                largeFont = settingsState.largeFont,
                                animationsEnabled = settingsState.animationsEnabled,
                                aiConsentGranted = settingsState.aiConsentGranted,
                                language = settingsState.language,
                                isLoggedIn = authState.isLoggedIn,
                                isSyncing = isSyncing,
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
                                onSyncNow = {
                                    settingsViewModel.syncNow()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Synchronizacja zakończona")
                                    }
                                },
                                onDeleteAccount = {
                                    authViewModel.deleteAccount { success ->
                                        if (success) {
                                            navController.popBackStack()
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Konto zostało usunięte")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Błąd podczas usuwania konta")
                                            }
                                        }
                                    }
                                },
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
                            val assessState by assessmentViewModel.uiState.collectAsState()
                            val dashState by dashboardViewModel.uiState.collectAsState()
                            ReportsScreen(
                                results = reactionState.results,
                                averageMs = reactionState.averageMs,
                                challengeResults = reactionState.challengeResults,
                                selfAssessmentHistory = assessState.selfAssessmentHistory,
                                reportsData = reactionState.reportsData,
                                aiInsight = dashState.aiInsight,
                                onGetAiInsight = {
                                    dashboardViewModel.getAiInsights()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ABOUT) {
                            AboutScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.AUTH) {
                            val username by authViewModel.username.collectAsState()

                            AuthScreen(
                                username = username,
                                onUsernameChange = authViewModel::updateUsername,
                                onLogin = { password ->
                                    authViewModel.login(password) { success ->
                                        if (success) {
                                            navController.popBackStack()
                                            dashboardViewModel.refreshDashboard()
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Zalogowano pomyślnie")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Błąd logowania")
                                            }
                                        }
                                    }
                                },
                                onRegister = { password ->
                                    authViewModel.register(password) { success ->
                                        if (success) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Konto utworzone. Możesz się zalogować.")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Błąd rejestracji. Użytkownik może już istnieć.")
                                            }
                                        }
                                    }
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
        }
    }
}
