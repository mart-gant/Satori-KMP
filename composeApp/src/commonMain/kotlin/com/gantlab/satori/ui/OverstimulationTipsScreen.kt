package com.gantlab.satori.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gantlab.satori.domain.model.Tip
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverstimulationTipsScreen(
    tips: List<Tip>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.calm_focus_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(Res.string.breathing_tool_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                BreathingExercise()
            }

            item {
                Text(
                    stringResource(Res.string.tips_techniques_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Text(
                    stringResource(Res.string.overstimulation_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(tips) { tip ->
                TipCard(tip)
            }
        }
    }
}

@Composable
fun BreathingExercise() {
    var isBreathingIn by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (isBreathingIn) 1.5f else 0.8f,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "BreathingScale"
    )

    val inhaleText = stringResource(Res.string.inhale)
    val exhaleText = stringResource(Res.string.exhale)

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            isBreathingIn = !isBreathingIn
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = if (isBreathingIn) inhaleText else exhaleText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Text(
                text = stringResource(Res.string.follow_circle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TipCard(tip: Tip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tip.icon, fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
            Column {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
