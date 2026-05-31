package com.gantlab.satori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gantlab.satori.utils.TimeUtils
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*
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
    var hasRecorded by remember { mutableStateOf(false) }

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
                startTime = TimeUtils.nowMs()
                hasRecorded = false
                gameState = GameState.GO
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.reaction_test_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
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
                            if (!hasRecorded) {
                                hasRecorded = true
                                reactionTime = TimeUtils.nowMs() - startTime
                                onResult(reactionTime)
                                gameState = GameState.RESULT
                            }
                        }
                        GameState.TOO_SOON -> gameState = GameState.WAITING
                        GameState.RESULT -> gameState = GameState.WAITING
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val text = when (gameState) {
                GameState.WAITING -> stringResource(Res.string.tap_to_start)
                GameState.READY -> stringResource(Res.string.wait_for_green)
                GameState.GO -> stringResource(Res.string.now_exclamation)
                GameState.TOO_SOON -> stringResource(Res.string.too_soon_retry)
                GameState.RESULT -> stringResource(Res.string.your_time_retry).replace("%d", reactionTime.toString())
            }
            Text(
                text = text,
                color = if (backgroundColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
