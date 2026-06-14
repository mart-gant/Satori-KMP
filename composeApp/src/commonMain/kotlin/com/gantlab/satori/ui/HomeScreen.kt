package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gantlab.satori.getPlatform
import com.gantlab.satori.domain.model.Recommendation
import com.gantlab.satori.presentation.viewmodel.ReactionViewModel
import com.gantlab.satori.presentation.viewmodel.MoodViewModel
import com.gantlab.satori.presentation.viewmodel.DashboardViewModel
import com.gantlab.satori.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import satori.composeapp.generated.resources.*

@Composable
fun HomeScreen(
    onNavigateToTest: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToTips: () -> Unit,
    onNavigateToScenarios: () -> Unit,
    onNavigateToSelfAssessment: () -> Unit,
    onNavigateToColorClash: () -> Unit,
    onNavigateToMemoryGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val reactionViewModel: ReactionViewModel = koinViewModel()
    val moodViewModel: MoodViewModel = koinViewModel()
    val dashboardViewModel: DashboardViewModel = koinViewModel()

    val settingsState by settingsViewModel.uiState.collectAsState()
    val moodState by moodViewModel.uiState.collectAsState()
    val dashState by dashboardViewModel.uiState.collectAsState()
    val reactionState by reactionViewModel.uiState.collectAsState()

    val nickname = remember(settingsState.nickname) {
        settingsState.nickname.ifEmpty { "Użytkowniku" }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(16.dp))
        
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            
            Text(
                text = stringResource(Res.string.welcome_user).replace("%s", nickname),
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(Modifier.height(16.dp))
            
            SatoriScoreCircle(dashState.dailySatoriScore)

            if (moodState.moodStreak > 0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🔥 ", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(Res.string.days_streak).replace("%d", moodState.moodStreak.toString()),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // REKOMENDACJE
            if (dashState.recommendations.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.for_you),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(8.dp))
                dashState.recommendations.forEach { rec ->
                    RecommendationCard(recommendation = rec)
                }
            }

            Spacer(Modifier.height(16.dp))

            // KARTA RANGI
            RankCard(
                rank = reactionState.rank.label,
                bestResult = reactionState.bestMs,
            )

            Spacer(Modifier.height(16.dp))

            // STATYSTYKI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard(
                    label = stringResource(Res.string.best_result),
                    value = reactionState.bestMs?.let { stringResource(Res.string.ms).replace("%d", it.toString()) } ?: "--",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(Res.string.average_result),
                    value = reactionState.averageMs?.let { stringResource(Res.string.ms).replace("%d", it.toString()) } ?: "--",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // FEATURES GRID
            HomeFeatureGrid(
                onNavigateToRoutines = onNavigateToRoutines,
                onNavigateToMood = onNavigateToMood,
                onNavigateToSelfAssessment = onNavigateToSelfAssessment,
                onNavigateToScenarios = onNavigateToScenarios,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // MIND CHALLENGES
            HomeChallengesGrid(
                onNavigateToTest = onNavigateToTest,
                onNavigateToColorClash = onNavigateToColorClash,
                onNavigateToMemoryGame = onNavigateToMemoryGame,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onNavigateToTips,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(text = stringResource(Res.string.sos_overstimulation))
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(Res.string.reports))
                }
                TextButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(Res.string.profile))
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun HomeFeatureGrid(
    onNavigateToRoutines: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToSelfAssessment: () -> Unit,
    onNavigateToScenarios: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.daily_life),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onNavigateToRoutines,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.routines))
            }
            OutlinedButton(
                onClick = onNavigateToMood,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.mood))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onNavigateToSelfAssessment,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.self_assessment_label))
            }
            OutlinedButton(
                onClick = onNavigateToScenarios,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.scenarios))
            }
        }
    }
}

@Composable
fun HomeChallengesGrid(
    onNavigateToTest: () -> Unit,
    onNavigateToColorClash: () -> Unit,
    onNavigateToMemoryGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.mind_challenges),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onNavigateToTest,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.reaction))
            }
            Button(
                onClick = onNavigateToColorClash,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.color_clash))
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onNavigateToMemoryGame,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(text = stringResource(Res.string.memory_game))
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun RankCard(
    rank: String,
    bestResult: Long?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp).align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.your_rank),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = rank,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            if (bestResult != null) {
                val shareText = stringResource(Res.string.share_text)
                    .replace("%d", bestResult.toString())
                    .replace("%s", rank)

                IconButton(
                    onClick = { getPlatform().shareText(shareText) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(Res.string.share_rank),
                    )
                }
            }
        }
    }
}

@Composable
fun SatoriScoreCircle(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.satori_score_desc).replace("%d", score.toString())
    Box(
        contentAlignment = Alignment.Center, 
        modifier = modifier
            .size(160.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
    ) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Satori Score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
