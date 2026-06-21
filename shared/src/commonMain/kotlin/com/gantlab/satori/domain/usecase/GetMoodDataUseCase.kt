package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.MoodRepository
import com.gantlab.satori.domain.model.DomainMoodEntry
import kotlinx.datetime.*

data class MoodData(
    val history: List<DomainMoodEntry>,
    val streak: Int
)

class GetMoodDataUseCase(private val repository: MoodRepository) {
    suspend operator fun invoke(): MoodData {
        val history = repository.getMoodHistory()
        val streak = calculateStreak(history)
        return MoodData(history, streak)
    }

    private fun calculateStreak(history: List<DomainMoodEntry>): Int {
        if (history.isEmpty()) return 0
        
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        
        val dates = history.map { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(timeZone).date 
        }.distinct().sortedDescending()

        var streak = 0
        var currentTarget = today

        for (date in dates) {
            if (date == currentTarget) {
                streak++
                currentTarget = currentTarget.minus(1, DateTimeUnit.DAY)
            } else if (date < currentTarget) {
                // Check if we missed today but have yesterday
                if (streak == 0 && date == today.minus(1, DateTimeUnit.DAY)) {
                    streak++
                    currentTarget = date.minus(1, DateTimeUnit.DAY)
                } else {
                    break
                }
            }
        }
        return streak
    }
}
