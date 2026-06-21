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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gantlab.satori.domain.model.*
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    results: List<DomainReactionResult>,
    averageMs: Long? = null,
    challengeResults: Map<String, List<DomainChallengeResult>> = emptyMap(),
    selfAssessmentHistory: List<DomainSelfAssessmentResult> = emptyList(),
    reportsData: ReportsData? = null,
    aiInsight: String? = null,
    onGetAiInsight: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.your_patterns)) },
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
                Text(stringResource(Res.string.ai_analysis_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (aiInsight == null) {
                            Text(stringResource(Res.string.ai_analysis_desc))
                            Button(onClick = onGetAiInsight, modifier = Modifier.padding(top = 8.dp)) {
                                Text(stringResource(Res.string.ai_generate_btn))
                            }
                        } else {
                            val generatingText = stringResource(Res.string.ai_generating)
                            Text(aiInsight, style = MaterialTheme.typography.bodyMedium)
                            if (aiInsight == generatingText) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            } else {
                                Button(onClick = onGetAiInsight, modifier = Modifier.padding(top = 8.dp)) {
                                    Text(stringResource(Res.string.ai_refresh_btn))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(stringResource(Res.string.mood_map_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                MoodHeatmap(reportsData?.heatmapCells ?: emptyList())
                Text(
                    stringResource(Res.string.mood_map_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(stringResource(Res.string.reaction_history_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (results.size >= 2) {
                    val avgDisplay = averageMs ?: results.asSequence().map { it.reactionTimeMs }.average().toLong()
                    LineChart(
                        values = results.map { it.reactionTimeMs }.reversed().toList(),
                        contentDescription = stringResource(Res.string.history_title) + " " + stringResource(Res.string.ms).replace("%d", avgDisplay.toString())
                    )
                } else {
                    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(stringResource(Res.string.collect_more_data))
                        }
                    }
                }
            }

            item {
                Text(stringResource(Res.string.daily_performance_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TimeOfDayAnalysis(reportsData?.hourlyAnalysis ?: emptyList())
            }

            // Challenge Results
            item {
                Text(stringResource(Res.string.cognitive_challenges_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChallengeCard(stringResource(Res.string.color_clash), challengeResults["color_clash"] ?: emptyList())
                    ChallengeCard(stringResource(Res.string.memory_game), challengeResults["memory_game"] ?: emptyList())
                }
            }

            item {
                Text(stringResource(Res.string.self_assessment_trends_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (selfAssessmentHistory.size >= 2) {
                    SelfAssessmentTrends(selfAssessmentHistory)
                } else {
                    Text(stringResource(Res.string.self_assessment_min_data), style = MaterialTheme.typography.bodySmall)
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
fun MoodHeatmap(cells: List<MoodHeatmapCell>) {
    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    val times = listOf("Rano", "Dzień", "Wieczór")
    
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
                    val cell = cells.find { it.dayIndex == dIdx && it.timeIndex == tIdx }
                    val color = when (cell?.moodScore) {
                        5 -> Color(0xFF1B5E20)
                        4 -> Color(0xFF4CAF50)
                        3 -> Color(0xFF81C784)
                        2 -> Color(0xFFA5D6A7)
                        1 -> Color(0xFFC8E6C9)
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
                        if (cell?.hasRoutineCompletion == true) {
                            Text("•", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeOfDayAnalysis(analysis: List<HourlyAnalysisPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (analysis.isEmpty()) {
                Text(stringResource(Res.string.no_hourly_data), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(stringResource(Res.string.avg_reaction_by_hour), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    analysis.forEach { point ->
                        val color = if (point.averageMs != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(point.heightFactor)
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
fun SelfAssessmentTrends(history: List<DomainSelfAssessmentResult>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TrendItem(stringResource(Res.string.attention), history.map { it.attentionScore }.reversed(), Color(0xFF2196F3))
            TrendItem(stringResource(Res.string.memory), history.map { it.memoryScore }.reversed(), Color(0xFF9C27B0))
            TrendItem(stringResource(Res.string.executive_functions), history.map { it.executiveScore }.reversed(), Color(0xFFFF5722))
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
fun ChallengeCard(title: String, results: List<DomainChallengeResult>) {
    val progressDesc = stringResource(Res.string.game_progress_desc)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            if (results.size >= 2) {
                LineChart(
                    values = results.map { it.score }.reversed().toList(),
                    color = MaterialTheme.colorScheme.secondary,
                    contentDescription = progressDesc.replace("%s", title).replace("%d", results.first().score.toString())
                )
            } else if (results.isNotEmpty()) {
                Text(stringResource(Res.string.result_label).replace("%d", results.first().score.toString()), style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(stringResource(Res.string.no_game_data), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ResultItem(result: DomainReactionResult) {
    val dateTime = remember(result.timestamp) {
        Instant.fromEpochMilliseconds(result.timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val dateString = "${dateTime.dayOfMonth}.${dateTime.monthNumber} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    val rank = remember(result.reactionTimeMs) { ReactionRank.fromTime(result.reactionTimeMs) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(Res.string.result_ms_label).replace("%d", result.reactionTimeMs.toString()), style = MaterialTheme.typography.bodyLarge)
            Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        val qualityColor = when (rank) {
            ReactionRank.NINJA, ReactionRank.GEPARD -> Color(0xFF4CAF50)
            ReactionRank.SOKOL, ReactionRank.HUMAN -> Color(0xFFFFC107)
            ReactionRank.LENIWIEC -> Color(0xFFF44336)
        }

        Surface(color = qualityColor, shape = MaterialTheme.shapes.small) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = rank.label,
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
    maxValue: Float? = null,
    contentDescription: String = "Wykres liniowy"
) {
    val minVal = 0f
    val maxVal = maxValue ?: values.maxOf { it }.toFloat()
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .semantics { this.contentDescription = contentDescription }
    ) {
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
