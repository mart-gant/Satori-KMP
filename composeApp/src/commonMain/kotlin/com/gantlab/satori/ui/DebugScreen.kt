package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gantlab.satori.DatabaseViewModel
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DatabaseViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug / Test Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Generowanie danych", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.addFakeResult() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj losowy wynik reakcji")
                }
                
                Button(
                    onClick = { viewModel.addFakeMood() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj losowy wpis nastroju")
                }
                
                Button(
                    onClick = { viewModel.addFakeChallengeResult("color_clash") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj wynik Color Clash")
                }

                Button(
                    onClick = { viewModel.addFakeChallengeResult("memory_game") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj wynik Memory Game")
                }
            }
            
            item {
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Zarządzanie bazą", style = MaterialTheme.typography.titleMedium)
                
                Button(
                    onClick = { viewModel.clearAllData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("USUŃ WSZYSTKIE DANE", color = MaterialTheme.colorScheme.onError)
                }
            }

            item {
                Text("Ostatnie wyniki reakcji:", style = MaterialTheme.typography.titleSmall)
            }

            items(viewModel.results.take(5).size) { index ->
                val result = viewModel.results[index]
                Text("${result.timestamp}: ${result.reactionTimeMs}ms")
            }
        }
    }
}
