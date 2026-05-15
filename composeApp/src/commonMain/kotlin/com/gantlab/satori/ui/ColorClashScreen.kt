package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorClashScreen(
    onResult: (score: Long) -> Unit,
    onBack: () -> Unit
) {
    val colors = listOf(
        "Czerwony" to Color.Red,
        "Zielony" to Color.Green,
        "Niebieski" to Color.Blue,
        "Żółty" to Color.Yellow
    )

    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(30) }
    var currentPair by remember { mutableStateOf(colors.random() to colors.random().second) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isPlaying = false
            onResult(score.toLong())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Clash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isPlaying && timeLeft == 30) {
                Text("Wybierz kolor CZCIONKI, a nie treść słowa!", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { isPlaying = true }) {
                    Text("Start")
                }
            } else if (isPlaying) {
                Text("Czas: $timeLeft s", style = MaterialTheme.typography.titleLarge)
                Text("Wynik: $score", style = MaterialTheme.typography.titleMedium)
                
                Spacer(Modifier.height(64.dp))
                
                Text(
                    text = currentPair.first.first,
                    color = currentPair.second,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(64.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (name, color) ->
                        Button(
                            onClick = {
                                if (color == currentPair.second) {
                                    score++
                                } else {
                                    score = (score - 1).coerceAtLeast(0)
                                }
                                currentPair = colors.random() to colors.random().second
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            // Empty content or a dot
                        }
                    }
                }
            } else {
                Text("Koniec czasu!", style = MaterialTheme.typography.headlineMedium)
                Text("Twój wynik: $score", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(32.dp))
                Button(onClick = {
                    timeLeft = 30
                    score = 0
                    isPlaying = true
                }) {
                    Text("Zagraj ponownie")
                }
            }
        }
    }
}
