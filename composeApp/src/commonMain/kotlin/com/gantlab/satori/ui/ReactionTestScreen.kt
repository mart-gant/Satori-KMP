package com.gantlab.satori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random

enum class GameState {
    WAITING,
    READY,
    GO,
    RESULT,
    TOO_SOON
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionTestScreen(
    onResult: (Long) -> Unit,
    onBack: () -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.WAITING) }
    var startTime by remember { mutableStateOf(0L) }
    var reactionTime by remember { mutableStateOf(0L) }

    val backgroundColor = when (gameState) {
        GameState.WAITING -> MaterialTheme.colorScheme.surface
        GameState.READY -> Color.Red
        GameState.GO -> Color.Green
        GameState.TOO_SOON -> Color.Yellow
        GameState.RESULT -> MaterialTheme.colorScheme.surface
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.READY) {
            delay(Random.nextLong(1000, 4000))
            if (gameState == GameState.READY) {
                startTime = Clock.System.now().toEpochMilliseconds()
                gameState = GameState.GO
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test czasu reakcji") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
                .clickable {
                    when (gameState) {
                        GameState.WAITING -> gameState = GameState.READY
                        GameState.READY -> gameState = GameState.TOO_SOON
                        GameState.GO -> {
                            reactionTime = Clock.System.now().toEpochMilliseconds() - startTime
                            onResult(reactionTime)
                            gameState = GameState.RESULT
                        }
                        GameState.TOO_SOON -> gameState = GameState.WAITING
                        GameState.RESULT -> gameState = GameState.WAITING
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (gameState) {
                    GameState.WAITING -> "Dotknij, aby zacząć"
                    GameState.READY -> "Czekaj na zielony..."
                    GameState.GO -> "TERAZ!"
                    GameState.TOO_SOON -> "Za wcześnie! Dotknij, aby spróbować ponownie."
                    GameState.RESULT -> "Twój czas: ${reactionTime}ms\nDotknij, aby ponowić."
                },
                color = if (backgroundColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
