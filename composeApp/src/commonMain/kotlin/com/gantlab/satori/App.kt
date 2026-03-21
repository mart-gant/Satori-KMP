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
fun App() {
    val viewModel: AppViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

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
                    onNavigateToReports = { navController.navigate(Routes.REPORTS) }
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
            composable(Routes.PROFILE) {
                ProfileScreen(
                    nickname = uiState.nickname,
                    highContrast = uiState.highContrast,
                    largeFont = uiState.largeFont,
                    animationsEnabled = uiState.animationsEnabled,
                    onNicknameChange = viewModel::updateNickname,
                    onHighContrastChange = viewModel::toggleHighContrast,
                    onLargeFontChange = viewModel::toggleLargeFont,
                    onAnimationsChange = viewModel::toggleAnimations,
                    onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(
                    results = uiState.results,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
