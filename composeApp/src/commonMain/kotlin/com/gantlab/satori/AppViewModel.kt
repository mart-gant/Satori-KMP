package com.gantlab.satori

import androidx.lifecycle.ViewModel
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.*
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask
import com.gantlab.satori.db.TaskCompletion
import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.SocialScenario
import com.gantlab.satori.db.SelfAssessmentResult
import com.gantlab.satori.db.ChallengeResult
import com.gantlab.satori.notifications.NotificationManager
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.network.AiService
import com.gantlab.satori.network.AuthRequest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class AppViewModel(
    private val repository: SatoriRepository,
    private val settings: SettingsManager,
    private val analytics: Analytics,
    private val notifications: NotificationManager? = null,
    private val api: SatoriApiService? = null,
    private val ai: AiService? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                isOnboardingCompleted = settings.isOnboardingCompleted,
                nickname = settings.nickname,
                highContrast = settings.highContrast,
                largeFont = settings.largeFont,
                animationsEnabled = settings.animationsEnabled,
                isLoggedIn = settings.authToken != null,
                language = settings.language
            )
        }
        loadResults()
        loadRoutines()
        loadTaskCompletions()
        loadMoodHistory()
        loadScenarios()
        loadSelfAssessmentHistory()
        loadChallengeResults()
        updateRecommendations()
        if (settings.authToken != null) {
            syncDataWithServer()
        }
        notifications?.scheduleDailyReminder(
            id = 1001,
            title = "Dzień dobry!",
            message = "Czas na poranny test reakcji i sprawdzenie rutyn.",
            hour = 8,
            minute = 0
        )
        analytics.logEvent(AnalyticsEvents.SCREEN_VIEW, mapOf("screen" to "home"))
    }

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        _uiState.update { it.copy(isOnboardingCompleted = true) }
        analytics.logEvent("onboarding_completed")
    }

    fun saveReactionTime(timeMs: Long) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        repository.insertReactionWithTimestamp(timestamp, timeMs)
        loadResults()
        analytics.logEvent(AnalyticsEvents.TEST_FINISHED, mapOf("result_ms" to timeMs.toString()))
        
        val token = settings.authToken
        if (token != null) {
            viewModelScope.launch {
                api?.postReaction(token, timestamp, timeMs)
            }
        }
    }

    fun loadResults() {
        val results = repository.getAllResults()
        val best = if (results.isNotEmpty()) results.minOf { it.reactionTimeMs } else null
        val avg = if (results.isNotEmpty()) results.asSequence().map { it.reactionTimeMs }.average().toLong() else null
        
        _uiState.update { 
            it.copy(
                results = results,
                bestResult = best,
                averageResult = avg,
                rank = calculateRank(best),
            ) 
        }
        calculateDailySatoriScore()
    }

    private fun calculateRank(bestMs: Long?): String {
        if (bestMs == null) return "Nowicjusz"
        return when {
            bestMs < 200 -> "Ninja"
            bestMs < 250 -> "Gepard"
            bestMs < 300 -> "Sokół"
            bestMs < 400 -> "Człowiek"
            else -> "Leniwiec"
        }
    }

    fun updateNickname(name: String) {
        settings.nickname = name
        _uiState.update { it.copy(nickname = name) }
    }

    fun toggleHighContrast(enabled: Boolean) {
        settings.highContrast = enabled
        _uiState.update { it.copy(highContrast = enabled) }
    }

    fun toggleLargeFont(enabled: Boolean) {
        settings.largeFont = enabled
        _uiState.update { it.copy(largeFont = enabled) }
    }

    fun toggleAnimations(enabled: Boolean) {
        settings.animationsEnabled = enabled
        _uiState.update { it.copy(animationsEnabled = enabled) }
    }

    fun updateLanguage(lang: String) {
        settings.language = lang
        _uiState.update { it.copy(language = lang) }
    }

    // --- Routines Logic ---

    fun loadRoutines() {
        val routines = repository.getAllRoutines()
        val tasksMap = routines.associateBy({ it.id }, { repository.getTasksForRoutine(it.id) })
        _uiState.update { 
            it.copy(
                routines = routines,
                routineTasks = tasksMap
            ) 
        }
    }

    fun addRoutine(title: String, icon: String?) {
        repository.createRoutine(title, icon)
        loadRoutines()
    }

    fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {
        repository.updateRoutine(id, title, icon, isActive)
        loadRoutines()
    }

    fun addTaskToRoutine(routineId: Long, name: String, time: String?) {
        repository.addTaskToRoutine(routineId, name, time)
        loadRoutines()
        
        if (time != null) {
            // Find the ID of the newly created task (best effort)
            val newTask = repository.getTasksForRoutine(routineId).firstOrNull { (it.taskName == name) && (it.scheduledTime == time) }
            newTask?.let { 
                notifications?.scheduleTaskNotification(it.id, it.taskName, time)
            }
        }
    }

    fun updateTaskDetails(taskId: Long, name: String, time: String?) {
        repository.updateTaskDetails(taskId, name, time)
        loadRoutines()
        
        if (time != null) {
            notifications?.scheduleTaskNotification(taskId, name, time)
        } else {
            notifications?.cancelTaskNotification(taskId)
        }
    }

    fun deleteRoutine(id: Long) {
        repository.deleteRoutine(id)
        loadRoutines()
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        repository.updateTaskCompletion(taskId, isCompleted)
        loadRoutines()
        loadTaskCompletions()
        calculateDailySatoriScore()
    }

    fun loadTaskCompletions() {
        val sevenDaysAgo = Clock.System.now().minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val completions = repository.getTaskCompletions(sevenDaysAgo)
        _uiState.update { it.copy(taskCompletions = completions) }
    }

    // --- Mood Logic ---

    fun loadMoodHistory() {
        val history = repository.getMoodHistory()
        val streak = calculateMoodStreak(history)
        _uiState.update { it.copy(moodHistory = history, moodStreak = streak) }
    }

    private fun calculateMoodStreak(history: List<MoodEntry>): Int {
        if (history.isEmpty()) return 0
        
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        
        // Group by unique dates and sort descending
        val uniqueDates = history
            .map { Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(timeZone).date }
            .distinct()
            .sortedDescending()

        if (uniqueDates.isEmpty()) return 0

        var streak = 0
        var expectedDate = uniqueDates.first()
        
        // If the latest entry is older than yesterday, the streak is broken
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

    fun saveMood(mood: Long, energy: Long, note: String) {
        repository.insertMood(mood, energy, note)
        loadMoodHistory()
        updateRecommendations()
        calculateDailySatoriScore()
        analytics.logEvent("mood_logged", mapOf("mood" to mood.toString(), "energy" to energy.toString()))
        
        // Sync with server
        val token = settings.authToken
        if (token != null) {
            viewModelScope.launch {
                api?.postMood(token, mood, energy, note)
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val response = api?.login(AuthRequest(username, password))
            if (response != null) {
                settings.authToken = response.token
                settings.nickname = response.username
                _uiState.update { it.copy(isLoggedIn = true, nickname = response.username) }
                syncDataWithServer()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun register(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = api?.register(AuthRequest(username, password)) ?: false
            onResult(success)
        }
    }

    fun logout() {
        settings.authToken = null
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun syncDataWithServer() {
        val token = settings.authToken ?: return
        viewModelScope.launch {
            try {
                // Sync Mood
                val serverMood = api?.getMoodHistory(token) ?: emptyList()
                serverMood.forEach { m ->
                    if (!repository.moodExists(m.timestamp)) {
                        repository.insertMoodWithTimestamp(m.timestamp, m.moodScore, m.energyScore, m.note)
                    }
                }
                
                // Sync Reactions
                val serverReactions = api?.getReactions(token) ?: emptyList()
                serverReactions.forEach { r ->
                    if (!repository.reactionExists(r.timestamp)) {
                        repository.insertReactionWithTimestamp(r.timestamp, r.reactionTimeMs)
                    }
                }
                
                // Sync Challenges
                val serverChallenges = api?.getChallenges(token) ?: emptyList()
                serverChallenges.forEach { c ->
                    if (!repository.challengeExists(c.timestamp, c.challengeType)) {
                        repository.insertChallengeWithTimestamp(c.timestamp, c.challengeType, c.score)
                    }
                }
                
                // Sync Self Assessment
                val serverSelf = api?.getSelfAssessmentHistory(token) ?: emptyList()
                serverSelf.forEach { s ->
                    if (!repository.selfAssessmentExists(s.timestamp)) {
                        repository.insertSelfAssessmentWithTimestamp(s.timestamp, s.attentionScore, s.memoryScore, s.executiveScore)
                    }
                }
                
                loadMoodHistory()
                loadResults()
                loadChallengeResults()
                loadSelfAssessmentHistory()
                updateRecommendations()
                calculateDailySatoriScore()
                
                println("SYNC: Full synchronization completed.")
            } catch (e: Exception) {
                println("SYNC ERROR: ${e.message}")
            }
        }
    }

    fun updateMoodNote(id: Long, note: String) {
        repository.updateMoodNote(id, note)
        loadMoodHistory()
    }

    // --- Mind Challenges ---

    fun loadChallengeResults() {
        val colorClash = repository.getChallengeHistory("color_clash")
        val memoryGame = repository.getChallengeHistory("memory_game")
        _uiState.update { 
            it.copy(
                challengeResults = mapOf(
                    "color_clash" to colorClash,
                    "memory_game" to memoryGame
                )
            )
        }
    }

    fun saveChallengeResult(type: String, score: Long) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        repository.insertChallengeWithTimestamp(timestamp, type, score)
        loadChallengeResults()
        analytics.logEvent("challenge_finished", mapOf("type" to type, "score" to score.toString()))
        
        val token = settings.authToken
        if (token != null) {
            viewModelScope.launch {
                api?.postChallenge(token, timestamp, type, score)
            }
        }
    }

    // --- Social Scenarios Logic ---

    fun loadScenarios() {
        val scenarios = repository.getAllScenarios()
        if (scenarios.isEmpty()) {
            // Dodajmy przykładowe scenariusze, jeśli baza jest pusta
            repository.insertScenario(
                "Wizyta u lekarza", 
                "Co zrobić po wejściu do przychodni.",
                "1. Podejdź do rejestracji\n2. Podaj swoje imię i nazwisko\n3. Usiądź w poczekalni i czekaj na wywołanie\n4. Wejdź do gabinetu, gdy lekarz Cię poprosi",
                "Zdrowie"
            )
            repository.insertScenario(
                "Zakupy w sklepie", 
                "Jak sprawnie zrobić zakupy.",
                "1. Przygotuj listę produktów\n2. Weź koszyk przy wejściu\n3. Znajdź produkty z listy\n4. Podejdź do kasy i zapłać",
                "Codzienność"
            )
            repository.insertScenario(
                "Rozmowa telefoniczna",
                "Jak przygotować się i przeprowadzić rozmowę.",
                "1. Zapisz na kartce główny cel rozmowy\n2. Wybierz numer i poczekaj na odebranie\n3. Przywitaj się: 'Dzień dobry, mówi [Twoje Imię]'\n4. Powiedz, w jakiej sprawie dzwonisz\n5. Słuchaj odpowiedzi i ewentualnie zapisuj ważne informacje\n6. Na koniec powiedz 'Dziękuję, do widzenia' i rozłącz się",
                "Komunikacja"
            )
            repository.insertScenario(
                "Jazda autobusem",
                "Korzystanie z komunikacji miejskiej krok po kroku.",
                "1. Sprawdź numer autobusu i godzinę na rozkładzie\n2. Stań na przystanku i czekaj na nadjeżdżający pojazd\n3. Upewnij się, że to Twój numer i wejdź do środka\n4. Skasuj bilet lub przyłóż kartę do czytnika\n5. Znajdź wolne miejsce siedzące lub złap się stabilnie uchwytu\n6. Obserwuj tablicę z przystankami lub słuchaj komunikatów\n7. Przed Twoim przystankiem naciśnij przycisk 'Stop' i wysiądź",
                "Podróż"
            )
            _uiState.update { it.copy(scenarios = repository.getAllScenarios()) }
        } else {
            _uiState.update { it.copy(scenarios = scenarios) }
        }
    }

    // --- Overstimulation Tips ---
    
    fun getOverstimulationTips() = listOf(
        Tip("Głębokie oddychanie", "Zamknij oczy i weź 5 głębokich oddechów, skupiając się tylko na powietrzu.", "🧘"),
        Tip("Redukcja światła", "Przyciemnij ekran telefonu lub wyjdź do ciemniejszego pomieszczenia na 5 minut.", "💡"),
        Tip("Biały szum", "Włącz dźwięk deszczu lub biały szum, aby odciąć się od nagłych dźwięków otoczenia.", "🎧"),
        Tip("Zasada 20-20-20", "Co 20 minut spójrz na obiekt oddalony o 20 stóp (6m) przez 20 sekund.", "👀"),
        Tip("Zimna woda", "Przemyj nadgarstki lub twarz zimną wodą, aby pobudzić układ przywspółczulny.", "💧")
    )

    // --- Self-Assessment ---

    fun loadSelfAssessmentHistory() {
        val history = repository.getSelfAssessmentHistory()
        _uiState.update { it.copy(selfAssessmentHistory = history) }
    }

    fun saveSelfAssessment(attention: Long, memory: Long, executive: Long) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        repository.insertSelfAssessmentWithTimestamp(timestamp, attention, memory, executive)
        loadSelfAssessmentHistory()
        updateRecommendations()
        calculateDailySatoriScore()
        
        val token = settings.authToken
        if (token != null) {
            viewModelScope.launch {
                api?.postSelfAssessment(token, timestamp, attention, memory, executive)
            }
        }
    }

    // --- Recommendations ---

    fun updateRecommendations() {
        val latestMood = _uiState.value.moodHistory.firstOrNull()
        val latestAssessment = _uiState.value.selfAssessmentHistory.firstOrNull()
        
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

        _uiState.update { it.copy(recommendations = recs) }
    }

    private fun calculateDailySatoriScore() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // 1. Mood & Energy (max 30 pts)
        val latestMood = _uiState.value.moodHistory.firstOrNull()
        val moodDate = latestMood?.let { Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date }
        val moodPoints = if (moodDate == today) {
            ((latestMood!!.moodScore + latestMood.energyScore) * 3).toInt() // (5+5)*3 = 30
        } else 0

        // 2. Routines (max 40 pts)
        val todayCompletions = _uiState.value.taskCompletions.filter { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
        }.size
        val routinePoints = (todayCompletions * 10).coerceAtMost(40)

        // 3. Activity (max 30 pts)
        val testToday = _uiState.value.results.any { 
            Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
        }
        val activityPoints = if (testToday) 30 else 0

        val total = moodPoints + routinePoints + activityPoints
        _uiState.update { it.copy(dailySatoriScore = total) }
    }

    // --- Export ---

    fun getExportData(): String {
        return repository.exportAllDataToCsv()
    }

    // --- AI Insights ---

    fun getAiInsights() {
        val dataSummary = repository.exportAllDataToCsv()
        _uiState.update { it.copy(aiInsight = "Generowanie analizy...") }
        viewModelScope.launch {
            val insight = ai?.getInsights(dataSummary) ?: "AI Service nie jest dostępny."
            _uiState.update { it.copy(aiInsight = insight) }
        }
    }
}

data class AppState(
    val isOnboardingCompleted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val nickname: String = "",
    val highContrast: Boolean = false,
    val largeFont: Boolean = false,
    val animationsEnabled: Boolean = true,
    val results: List<ReactionResult> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val routineTasks: Map<Long, List<RoutineTask>> = emptyMap(),
    val taskCompletions: List<TaskCompletion> = emptyList(),
    val moodHistory: List<MoodEntry> = emptyList(),
    val scenarios: List<SocialScenario> = emptyList(),
    val selfAssessmentHistory: List<SelfAssessmentResult> = emptyList(),
    val challengeResults: Map<String, List<ChallengeResult>> = emptyMap(),
    val recommendations: List<Recommendation> = emptyList(),
    val bestResult: Long? = null,
    val averageResult: Long? = null,
    val rank: String = "Nowicjusz",
    val aiInsight: String? = null,
    val moodStreak: Int = 0,
    val dailySatoriScore: Int = 0,
    val language: String = "pl"
)

data class Tip(val title: String, val description: String, val icon: String)
data class Recommendation(val title: String, val description: String, val type: String)

