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
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    results: List<ReactionResult>,
    moodHistory: List<com.gantlab.satori.db.MoodEntry> = emptyList(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text("Historia Reakcji", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (results.size >= 2) {
                    LineChart(results.map { it.reactionTimeMs }.reversed().toList())
                } else {
                    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(stringResource(Res.string.collect_more_data))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("Historia Nastroju", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (moodHistory.size >= 2) {
                    LineChart(moodHistory.map { it.moodScore }.reversed().toList(), color = Color(0xFF9C27B0), maxValue = 5f)
                } else {
                    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Zbyt mało danych nastroju.")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(stringResource(Res.string.recent_attempts), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(results) { result ->
                ResultItem(result)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
            Text(text = "${stringResource(Res.string.result_label).replace("%d", result.reactionTimeMs.toString())}", style = MaterialTheme.typography.bodyLarge)
            Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        val qualityColor = when {
            result.reactionTimeMs < 250 -> Color(0xFF4CAF50)
            result.reactionTimeMs < 400 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }

        Surface(color = qualityColor, shape = MaterialTheme.shapes.small) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = if (result.reactionTimeMs < 250) stringResource(Res.string.fast) 
                           else if (result.reactionTimeMs < 400) stringResource(Res.string.ok) 
                           else stringResource(Res.string.slow),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    values: List<Long>,
    color: Color = MaterialTheme.colorScheme.primary,
    maxValue: Float? = null
) {
    val minVal = 0f
    val maxVal = maxValue ?: values.maxOf { it }.toFloat()
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val (width, height) = size
        val path = Path()

        values.forEachIndexed { i, valLong ->
            val value = valLong.toFloat()
            val x = if (values.size > 1) i * (width / (values.size - 1)) else width / 2
            val y = height - ((value - minVal) / range) * height

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
