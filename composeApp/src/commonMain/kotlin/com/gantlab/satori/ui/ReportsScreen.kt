package com.gantlab.satori.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.ChallengeResult
import com.gantlab.satori.db.SelfAssessmentResult
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    results: List<ReactionResult>,
    moodHistory: List<MoodEntry> = emptyList(),
    taskCompletions: List<com.gantlab.satori.db.TaskCompletion> = emptyList(),
    challengeResults: Map<String, List<ChallengeResult>> = emptyMap(),
    selfAssessmentHistory: List<SelfAssessmentResult> = emptyList(),
    aiInsight: String? = null,
    onGetAiInsight: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twoje Wzorce") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("Analiza AI (Beta)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (aiInsight == null) {
                            Text("Pobierz inteligentną analizę Twoich danych, aby lepiej zrozumieć swoje wzorce.")
                            Button(onClick = onGetAiInsight, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Generuj analizę Gemini")
                            }
                        } else {
                            Text(aiInsight, style = MaterialTheme.typography.bodyMedium)
                            if (aiInsight == "Generowanie analizy...") {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            } else {
                                Button(onClick = onGetAiInsight, modifier = Modifier.padding(top = 8.dp)) {
                                    Text("Odśwież analizę")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Mapa Nastroju i Rutyn (7 dni)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                MoodHeatmap(moodHistory, taskCompletions)
                Text(
                    "Ciemniejszy zielony = lepszy nastrój. Kropka (•) = wykonana rutyna.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
            }

            item {
                Text("Wydajność w ciągu dnia", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TimeOfDayAnalysis(results)
            }

            // Challenge Results
            item {
                Text("Wyzwania Poznawcze", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChallengeCard("Color Clash", challengeResults["color_clash"] ?: emptyList())
                    ChallengeCard("Memory Game", challengeResults["memory_game"] ?: emptyList())
                }
            }

            item {
                Text("Trendy Samooceny", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (selfAssessmentHistory.size >= 2) {
                    SelfAssessmentTrends(selfAssessmentHistory)
                } else {
                    Text("Wypełnij samoocenę przynajmniej dwa razy, aby zobaczyć trendy.", style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                Text(stringResource(Res.string.recent_attempts), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(results.take(10)) { result ->
                ResultItem(result)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun MoodHeatmap(history: List<MoodEntry>, completions: List<com.gantlab.satori.db.TaskCompletion> = emptyList()) {
    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    val times = listOf("Rano", "Dzień", "Wieczór")
    
    val grid = Array(7) { arrayOfNulls<Long>(3) }
    val routineGrid = Array(7) { BooleanArray(3) { false } }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    history.forEach { entry ->
        val entryDate = Instant.fromEpochMilliseconds(entry.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
        if (entryDate.date > now.date.minus(7, DateTimeUnit.DAY)) {
            val dayIdx = (entryDate.dayOfWeek.isoDayNumber - 1) % 7
            val timeIdx = when (entryDate.hour) {
                in 5..11 -> 0
                in 12..18 -> 1
                else -> 2
            }
            grid[dayIdx][timeIdx] = entry.moodScore
        }
    }

    completions.forEach { completion ->
        val cDate = Instant.fromEpochMilliseconds(completion.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
        if (cDate.date > now.date.minus(7, DateTimeUnit.DAY)) {
            val dayIdx = (cDate.dayOfWeek.isoDayNumber - 1) % 7
            val timeIdx = when (cDate.hour) {
                in 5..11 -> 0
                in 12..18 -> 1
                else -> 2
            }
            routineGrid[dayIdx][timeIdx] = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(start = 50.dp)) {
            days.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        
        times.forEachIndexed { tIdx, timeLabel ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeLabel, modifier = Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall)
                for (dIdx in 0..6) {
                    val score = grid[dIdx][tIdx]
                    val hasRoutine = routineGrid[dIdx][tIdx]
                    val color = when (score) {
                        5L -> Color(0xFF1B5E20)
                        4L -> Color(0xFF4CAF50)
                        3L -> Color(0xFF81C784)
                        2L -> Color(0xFFA5D6A7)
                        1L -> Color(0xFFC8E6C9)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(color, shape = MaterialTheme.shapes.extraSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasRoutine) {
                            Text("•", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeOfDayAnalysis(results: List<ReactionResult>) {
    val hourlyAvg = remember(results) {
        results.groupBy { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).hour 
        }.mapValues { entry -> 
            entry.value.map { it.reactionTimeMs }.average().toLong() 
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (hourlyAvg.isEmpty()) {
                Text("Brak danych do analizy godzinowej.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Średni czas reakcji względem godziny (ms)", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxVal = if (hourlyAvg.values.isNotEmpty()) hourlyAvg.values.maxOrNull()?.toFloat() ?: 1f else 1f
                    
                    (0..23).forEach { hour ->
                        val avg = hourlyAvg[hour]
                        val heightFactor = if (avg != null) (avg.toFloat() / maxVal).coerceIn(0.1f, 1f) else 0.05f
                        val color = if (avg != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFactor)
                                .padding(horizontal = 1.dp)
                                .background(color, MaterialTheme.shapes.extraSmall)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("00:00", style = MaterialTheme.typography.labelSmall)
                    Text("12:00", style = MaterialTheme.typography.labelSmall)
                    Text("23:59", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SelfAssessmentTrends(history: List<SelfAssessmentResult>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TrendItem("Uwaga", history.map { it.attentionScore }.reversed(), Color(0xFF2196F3))
            TrendItem("Pamięć", history.map { it.memoryScore }.reversed(), Color(0xFF9C27B0))
            TrendItem("Funkcje wykonawcze", history.map { it.executiveScore }.reversed(), Color(0xFFFF5722))
        }
    }
}

@Composable
fun TrendItem(label: String, values: List<Long>, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        LineChart(values = values, color = color, maxValue = 5f)
    }
}

@Composable
fun ChallengeCard(title: String, results: List<ChallengeResult>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            if (results.size >= 2) {
                LineChart(
                    values = results.map { it.score }.reversed().toList(),
                    color = MaterialTheme.colorScheme.secondary
                )
            } else if (results.isNotEmpty()) {
                Text("Ostatni wynik: ${results.first().score}", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("Brak danych. Zagraj, aby zobaczyć postępy.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ResultItem(result: ReactionResult) {
    val dateTime = remember(result.timestamp) {
        Instant.fromEpochMilliseconds(result.timestamp)
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
            result.reactionTimeMs < 250 -> Color(0xFF4CAF50)
            result.reactionTimeMs < 400 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }

        Surface(color = qualityColor, shape = MaterialTheme.shapes.small) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = if (result.reactionTimeMs < 250) "Szybko" 
                           else if (result.reactionTimeMs < 400) "Dobrze" 
                           else "Wolno",
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
