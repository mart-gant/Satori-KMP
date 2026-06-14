package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.db.TaskCompletion
import com.gantlab.satori.domain.model.HourlyAnalysisPoint
import com.gantlab.satori.domain.model.MoodHeatmapCell
import com.gantlab.satori.domain.model.ReportsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class GetReportsDataUseCase(private val repository: SatoriRepository) {
    
    suspend operator fun invoke(): ReportsData = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val sevenDaysAgo = now.date.minus(7, DateTimeUnit.DAY)
        
        val moodHistory = repository.getMoodHistory()
        val completions = repository.getTaskCompletions(
            sevenDaysAgo.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        )
        val reactionResults = repository.getAllResults()
        
        ReportsData(
            heatmapCells = calculateHeatmap(moodHistory, completions, now),
            hourlyAnalysis = calculateHourlyAnalysis(reactionResults)
        )
    }

    private fun calculateHeatmap(
        history: List<MoodEntry>, 
        completions: List<TaskCompletion>,
        now: LocalDateTime
    ): List<MoodHeatmapCell> {
        val cells = mutableListOf<MoodHeatmapCell>()
        val timeZone = TimeZone.currentSystemDefault()
        
        for (dIdx in 0..6) {
            for (tIdx in 0..2) {
                // Find mood for this day and time slot
                val score = history.find { entry ->
                    val date = Instant.fromEpochMilliseconds(entry.timestamp).toLocalDateTime(timeZone)
                    date.date > now.date.minus(7, DateTimeUnit.DAY) &&
                    (date.dayOfWeek.isoDayNumber - 1) % 7 == dIdx &&
                    getTimeSlot(date.hour) == tIdx
                }?.moodScore

                val hasRoutine = completions.any { c ->
                    val date = Instant.fromEpochMilliseconds(c.timestamp).toLocalDateTime(timeZone)
                    date.date > now.date.minus(7, DateTimeUnit.DAY) &&
                    (date.dayOfWeek.isoDayNumber - 1) % 7 == dIdx &&
                    getTimeSlot(date.hour) == tIdx
                }

                cells.add(MoodHeatmapCell(dIdx, tIdx, score, hasRoutine))
            }
        }
        return cells
    }

    private fun calculateHourlyAnalysis(results: List<ReactionResult>): List<HourlyAnalysisPoint> {
        val timeZone = TimeZone.currentSystemDefault()
        val hourlyAvg = results.groupBy { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(timeZone).hour 
        }.mapValues { entry -> 
            entry.value.map { it.reactionTimeMs }.average().toLong() 
        }

        val maxVal = if (hourlyAvg.values.isNotEmpty()) hourlyAvg.values.maxOrNull()?.toFloat() ?: 1f else 1f
        
        return (0..23).map { hour ->
            val avg = hourlyAvg[hour]
            HourlyAnalysisPoint(
                hour = hour,
                averageMs = avg,
                heightFactor = if (avg != null) (avg.toFloat() / maxVal).coerceIn(0.1f, 1f) else 0.05f
            )
        }
    }

    private fun getTimeSlot(hour: Int): Int = when (hour) {
        in 5..11 -> 0
        in 12..18 -> 1
        else -> 2
    }
}
