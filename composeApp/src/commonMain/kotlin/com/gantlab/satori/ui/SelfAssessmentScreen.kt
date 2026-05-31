package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

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
                title = { Text(stringResource(Res.string.cognitive_self_assessment_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
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
                stringResource(Res.string.assess_functioning_desc),
                style = MaterialTheme.typography.bodyLarge
            )

            AssessmentSlider(
                label = stringResource(Res.string.attention_concentration),
                value = attentionScore,
                onValueChange = { attentionScore = it }
            )

            AssessmentSlider(
                label = stringResource(Res.string.working_memory),
                value = memoryScore,
                onValueChange = { memoryScore = it }
            )

            AssessmentSlider(
                label = stringResource(Res.string.executive_functions_planning),
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
                Text(stringResource(Res.string.save_self_assessment))
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
