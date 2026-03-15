package com.gantlab.satori.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gantlab.satori.db.ReactionResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    results: List<ReactionResult>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twoje Wyniki") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        }
    ) { padding ->
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak zapisanych wyników. Wykonaj pierwszy test!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Historia reakcji (ms)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (results.size >= 2) {
                    LineChart(results.reversed()) // Pokazujemy od najstarszego do najnowszego na wykresie
                } else {
                    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Zbierz więcej danych dla wykresu")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Ostatnie próby", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(results) { result ->
                        ResultItem(result)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ResultItem(result: ReactionResult) {
    val dateTime = remember(result.timestamp) {
        kotlinx.datetime.Instant.fromEpochMilliseconds(result.timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val dateString = "${dateTime.dayOfMonth}.${dateTime.monthNumber} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Wynik: ${result.reactionTimeMs} ms", style = MaterialTheme.typography.bodyLarge)
            Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        val qualityColor = when {
            result.reactionTimeMs < 250 -> Color(0xFF4CAF50) // Świetny
            result.reactionTimeMs < 400 -> Color(0xFFFFC107) // Dobry
            else -> Color(0xFFF44336) // Słaby
        }

        Surface(color = qualityColor, shape = MaterialTheme.shapes.small) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = if (result.reactionTimeMs < 250) "SZYBKO" else if (result.reactionTimeMs < 400) "OK" else "POWOLNIE",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun LineChart(results: List<ReactionResult>) {
    val color = MaterialTheme.colorScheme.primary
    val minTime = results.minOf { it.reactionTimeMs }.toFloat()
    val maxTime = results.maxOf { it.reactionTimeMs }.toFloat()
    val timeRange = (maxTime - minTime).coerceAtLeast(1f)

    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val (width, height) = size
        val path = Path()

        results.forEachIndexed { i, result ->
            val x = if (results.size > 1) i * (width / (results.size - 1)) else width / 2
            val y = height - ((result.reactionTimeMs - minTime) / timeRange) * height

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(color, radius = 6f, center = Offset(x, y))
        }

        drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
    }
}
