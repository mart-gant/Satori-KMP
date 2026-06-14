package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.domain.model.DashboardData
import com.gantlab.satori.domain.model.Recommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class GetDashboardDataUseCase(private val repository: SatoriRepository) {
    
    suspend operator fun invoke(): DashboardData = withContext(Dispatchers.Default) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val score = calculateScore(today)
        val recommendations = generateRecommendations()
        
        DashboardData(recommendations, score)
    }

    private suspend fun calculateScore(today: LocalDate): Int {
        // 1. Mood & Energy (max 30 pts)
        val latestMood = repository.getMoodHistory().firstOrNull()
        val moodDate = latestMood?.let { Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date }
        val moodPoints = if (moodDate == today) {
            ((latestMood.moodScore + latestMood.energyScore) * 3).toInt()
        } else 0

        // 2. Routines (max 40 pts)
        val sevenDaysAgo = Clock.System.now().minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val todayCompletions = repository.getTaskCompletions(sevenDaysAgo).filter { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
        }.size
        val routinePoints = (todayCompletions * 10).coerceAtMost(40)

        // 3. Activity (max 30 pts)
        val testToday = repository.getAllResults().any { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
        }
        val activityPoints = if (testToday) 30 else 0

        return moodPoints + routinePoints + activityPoints
    }

    private fun generateRecommendations(): List<Recommendation> {
        val latestMood = repository.getMoodHistory().firstOrNull()
        val latestAssessment = repository.getSelfAssessmentHistory().firstOrNull()
        
        val recs = mutableListOf<Recommendation>()
        
        if ((latestMood != null) && (latestMood.moodScore < 3)) {
            recs.add(Recommendation("Zadbaj o siebie", "Twoja ocena nastroju jest niska. Spróbuj ćwiczeń oddechowych z sekcji Porady.", "Zdrowie Psychiczne"))
        }
        
        if ((latestAssessment != null) && (latestAssessment.attentionScore < 3)) {
            recs.add(Recommendation("Trening uważności", "Skupienie może być dziś wyzwaniem. Spróbuj gry Color Clash, aby poćwiczyć uwagę.", "Poznawcze"))
        }

        if (recs.isEmpty()) {
            recs.add(Recommendation("Dobra forma!", "Kontynuuj swoje codzienne rutyny, aby utrzymać ten stan.", "Ogólne"))
        }
        
        return recs
    }
}
