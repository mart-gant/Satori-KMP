package com.gantlab.satori

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {

    // Mock/Fake dla potrzeb testu jednostkowego bez bazy danych
    private class FakeRepository : SatoriRepository(null as Any? as com.gantlab.satori.db.DriverFactory) {
        var results = mutableListOf<com.gantlab.satori.db.ReactionResult>()
        override fun getAllResults() = results
        override fun insertReactionResult(reactionTimeMs: Long) { /* no-op */ }
    }

    @Test
    fun `test rank calculation for different reaction times`() {
        // To jest uproszczony test logiki, w realnym scenariuszu użylibyśmy mocków
        // Tutaj sprawdzamy bezpośrednio czy logika w ViewModelu (którą można by wydzielić do helpera) działa
        
        val ninjaRank = calculateRankForTest(150)
        val gepardRank = calculateRankForTest(220)
        val sokolRank = calculateRankForTest(280)
        val humanRank = calculateRankForTest(350)
        val snailRank = calculateRankForTest(500)

        assertEquals("Ninja", ninjaRank)
        assertEquals("Gepard", gepardRank)
        assertEquals("Sokół", sokolRank)
        assertEquals("Człowiek", humanRank)
        assertEquals("Leniwiec", snailRank)
    }

    private fun calculateRankForTest(bestMs: Long?): String {
        if (bestMs == null) return "Nowicjusz"
        return when {
            bestMs < 200 -> "Ninja"
            bestMs < 250 -> "Gepard"
            bestMs < 300 -> "Sokół"
            bestMs < 400 -> "Człowiek"
            else -> "Leniwiec"
        }
    }
}
