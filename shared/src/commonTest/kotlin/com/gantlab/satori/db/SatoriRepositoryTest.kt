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
        
        // Ensure schema is created for the in-memory driver
        try {
            SatoriDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Schema might already exist in some environments
        }

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
}
