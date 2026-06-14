package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.SatoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

data class MoodData(
    val history: List<MoodEntry>,
    val streak: Int
)

class GetMoodDataUseCase(private val repository: SatoriRepository) {
    
    suspend operator fun invoke(): MoodData = withContext(Dispatchers.Default) {
        val history = repository.getMoodHistory()
        val streak = calculateMoodStreak(history)
        MoodData(history, streak)
    }

    private fun calculateMoodStreak(history: List<MoodEntry>): Int {
        if (history.isEmpty()) return 0
        
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        
        val uniqueDates = history
            .asSequence()
            .map { Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(timeZone).date }
            .distinct()
            .sortedDescending()
            .toList()

        if (uniqueDates.isEmpty()) return 0

        var streak = 0
        var expectedDate = uniqueDates.first()
        
        if (expectedDate < today.minus(1, DateTimeUnit.DAY)) return 0

        for (date in uniqueDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        return streak
    }
}
