package com.gantlab.satori

import androidx.lifecycle.ViewModel
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.gantlab.satori.db.ReactionResult
import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask
import com.gantlab.satori.db.MoodEntry
import com.gantlab.satori.db.SocialScenario
import com.gantlab.satori.db.SelfAssessmentResult
import com.gantlab.satori.notifications.NotificationManager
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.network.AuthRequest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class AppViewModel(
    private val repository: SatoriRepository,
    private val settings: SettingsManager,
    private val analytics: Analytics,
    private val notifications: NotificationManager? = null,
    private val api: SatoriApiService? = null,
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
                isLoggedIn = settings.authToken != null
            )
        }
        loadResults()
        loadRoutines()
        loadMoodHistory()
        loadScenarios()
        loadSelfAssessmentHistory()
        updateRecommendations()
        if (settings.authToken != null) {
            syncDataWithServer()
        }
        analytics.logEvent(AnalyticsEvents.SCREEN_VIEW, mapOf("screen" to "home"))
    }

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        _uiState.update { it.copy(isOnboardingCompleted = true) }
        analytics.logEvent("onboarding_completed")
    }

    fun saveReactionTime(timeMs: Long) {
        repository.insertReactionResult(timeMs)
        loadResults()
        analytics.logEvent(AnalyticsEvents.TEST_FINISHED, mapOf("result_ms" to timeMs.toString()))
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

    fun addRoutine(title: String) {
        repository.createRoutine(title)
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
    }

    // --- Mood Logic ---

    fun loadMoodHistory() {
        val history = repository.getMoodHistory()
        _uiState.update { it.copy(moodHistory = history) }
    }

    fun saveMood(mood: Long, energy: Long, note: String) {
        repository.insertMood(mood, energy, note)
        loadMoodHistory()
        updateRecommendations()
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
                val serverHistory = api?.getMoodHistory(token) ?: emptyList()
                var addedCount = 0
                
                serverHistory.forEach { serverMood ->
                    if (!repository.moodExists(serverMood.timestamp)) {
                        repository.insertMoodWithTimestamp(
                            timestamp = serverMood.timestamp,
                            moodScore = serverMood.moodScore,
                            energyScore = serverMood.energyScore,
                            note = serverMood.note
                        )
                        addedCount++
                    }
                }
                
                if (addedCount > 0) {
                    loadMoodHistory()
                    updateRecommendations()
                }
                println("SYNC: Received ${serverHistory.size} entries, added $addedCount new ones to local DB.")
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

    fun saveChallengeResult(type: String, score: Long) {
        repository.insertChallengeResult(type, score)
        analytics.logEvent("challenge_finished", mapOf("type" to type, "score" to score.toString()))
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
        repository.insertSelfAssessment(attention, memory, executive)
        loadSelfAssessmentHistory()
        updateRecommendations()
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

    // --- Export ---

    fun getExportData(): String {
        return repository.exportMoodToCsv()
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
    val moodHistory: List<MoodEntry> = emptyList(),
    val scenarios: List<SocialScenario> = emptyList(),
    val selfAssessmentHistory: List<SelfAssessmentResult> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val bestResult: Long? = null,
    val averageResult: Long? = null,
    val rank: String = "Nowicjusz"
)

data class Tip(val title: String, val description: String, val icon: String)
data class Recommendation(val title: String, val description: String, val type: String)

