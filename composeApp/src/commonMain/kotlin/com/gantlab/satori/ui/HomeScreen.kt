package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gantlab.satori.AppViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import satori.composeapp.generated.resources.*

@Composable
fun HomeScreen(
    onNavigateToTest: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val viewModel: AppViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = stringResource(Res.string.welcome_warrior).replace("%s", uiState.nickname.ifEmpty { stringResource(Res.string.warrior_default) }),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(32.dp))

        // KARTA RANGI
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNavigateToTest,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(Res.string.start_test), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToReports,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(Res.string.reports))
            }
            OutlinedButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.weight(1f)
            ) {
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
