package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfAssessmentScreen(
    onSave: (attention: Long, memory: Long, executive: Long) -> Unit,
    onBack: () -> Unit
) {
    var attentionScore by remember { mutableStateOf(3f) }
    var memoryScore by remember { mutableStateOf(3f) }
    var executiveScore by remember { mutableStateOf(3f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Samoocena Poznawcza") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Oceń swoje dzisiejsze funkcjonowanie w skali 1-5.",
                style = MaterialTheme.typography.bodyLarge
            )

            AssessmentSlider(
                label = "Uwaga i Koncentracja",
                value = attentionScore,
                onValueChange = { attentionScore = it }
            )

            AssessmentSlider(
                label = "Pamięć robocza",
                value = memoryScore,
                onValueChange = { memoryScore = it }
            )

            AssessmentSlider(
                label = "Funkcje wykonawcze (planowanie)",
                value = executiveScore,
                onValueChange = { executiveScore = it }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onSave(attentionScore.toLong(), memoryScore.toLong(), executiveScore.toLong())
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zapisz samoocenę")
            }
        }
    }
}

@Composable
fun AssessmentSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value.toInt().toString(), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3
        )
    }
}
