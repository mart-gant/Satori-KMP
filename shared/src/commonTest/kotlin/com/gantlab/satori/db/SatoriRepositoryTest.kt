package com.gantlab.satori.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import app.cash.sqldelight.db.SqlDriver

// Note: Requires providing an in-memory driver in platform tests
expect fun createTestDriver(): SqlDriver

class SatoriRepositoryTest {

    @Test
    fun testInsertingAndRetrievingResults() {
        val driver = createTestDriver()
        val database = SatoriDatabase(driver)
        val repository = SatoriRepository(database)

        repository.insertReactionResult(250L)
        repository.insertReactionResult(150L)

        val results = repository.getAllResults()

        assertEquals(2, results.size)
        // Results are sorted by timestamp DESC, so the second one inserted (150L) should be first
        assertEquals(150L, results[0].reactionTimeMs)
        assertTrue(results.any { it.reactionTimeMs == 250L })
        
        driver.close()
    }

    @Test
    fun testMoodHistory() {
        val driver = createTestDriver()
        val database = SatoriDatabase(driver)
        val repository = SatoriRepository(database)

        repository.insertMood(5, 4, "Feeling good")
        repository.insertMood(3, 2, "A bit tired")

        val history = repository.getMoodHistory()
        assertEquals(2, history.size)
        assertEquals(5, history[0].moodScore)
        assertEquals("Feeling good", history[0].note)

        driver.close()
    }

    @Test
    fun testMindChallenges() {
        val driver = createTestDriver()
        val database = SatoriDatabase(driver)
        val repository = SatoriRepository(database)

        repository.insertChallengeResult("memory_game", 80)
        repository.insertChallengeResult("memory_game", 95)
        repository.insertChallengeResult("color_clash", 120)

        val memoryHistory = repository.getChallengeHistory("memory_game")
        val colorHistory = repository.getChallengeHistory("color_clash")

        assertEquals(2, memoryHistory.size)
        assertEquals(95, memoryHistory[0].score)
        assertEquals(1, colorHistory.size)
        assertEquals(120, colorHistory[0].score)

        driver.close()
    }

    @Test
    fun testRoutinesAndTasks() {
        val driver = createTestDriver()
        val database = SatoriDatabase(driver)
        val repository = SatoriRepository(database)

        repository.createRoutine("Morning", "☀️")
        val routines = repository.getAllRoutines()
        assertEquals(1, routines.size)
        val routineId = routines[0].id

        repository.addTaskToRoutine(routineId, "Drink water", "08:00")
        repository.addTaskToRoutine(routineId, "Meditation", "08:15")

        val tasks = repository.getTasksForRoutine(routineId)
        assertEquals(2, tasks.size)
        assertEquals("Drink water", tasks[0].taskName)

        repository.updateTaskCompletion(tasks[0].id, true)
        val updatedTasks = repository.getTasksForRoutine(routineId)
        assertEquals(1L, updatedTasks[0].isCompletedToday)

        driver.close()
    }
}
