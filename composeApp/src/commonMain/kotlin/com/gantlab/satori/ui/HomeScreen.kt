package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gantlab.satori.AppViewModel
import com.gantlab.satori.getPlatform
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
    onNavigateToMemoryGame: () -> Unit
) {
    val viewModel: AppViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Witaj, ${uiState.nickname.ifEmpty { "użytkowniku" }}",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // REKOMENDACJE
        if (uiState.recommendations.isNotEmpty()) {
            Text("Dla Ciebie", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            uiState.recommendations.forEach { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(rec.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // KARTA RANGI
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp).align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(Res.string.your_rank), style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = uiState.rank,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                if (uiState.bestResult != null) {
                    val shareText = stringResource(Res.string.share_text)
                        .replace("%d", uiState.bestResult.toString())
                        .replace("%s", uiState.rank)
                        
                    IconButton(
                        onClick = { getPlatform().shareText(shareText) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(Res.string.share_rank))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // STATYSTYKI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = stringResource(Res.string.best_result),
                value = uiState.bestResult?.let { stringResource(Res.string.ms).replace("%d", it.toString()) } ?: "--",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(Res.string.average_result),
                value = uiState.averageResult?.let { stringResource(Res.string.ms).replace("%d", it.toString()) } ?: "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // CORE FEATURES
        Text("Codzienność", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToRoutines,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Rutyny")
            }
            OutlinedButton(
                onClick = onNavigateToMood,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Nastrój")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToSelfAssessment,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Samoocena")
            }
            OutlinedButton(
                onClick = onNavigateToScenarios,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Scenariusze")
            }
        }

        Spacer(Modifier.height(24.dp))

        // MIND CHALLENGES
        Text("Wyzwania Umysłu", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToTest,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Reakcja")
            }
            Button(
                onClick = onNavigateToColorClash,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Color Clash")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onNavigateToMemoryGame,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Gra Pamięciowa")
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onNavigateToTips,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("🆘 SOS: Przebodźcowanie")
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onNavigateToReports, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.reports))
            }
            TextButton(onClick = onNavigateToProfile, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.profile))
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
