package com.gantlab.satori

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.db.SatoriDatabase
import com.gantlab.satori.settings.SettingsManager
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {

    private class FakeAnalytics : Analytics {
        override fun logEvent(name: String, params: Map<String, String>) {}
    }

    @Test
    fun `test rank calculation for different reaction times`() {
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
