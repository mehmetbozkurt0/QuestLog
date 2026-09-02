package com.mehmetbozkurt.questlog.domain.progression

import com.mehmetbozkurt.questlog.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XpLimitsTest {

    @Test
    fun `a single log can only pay out once a day`() {
        assertTrue(XpLimits.ONE_AWARD_PER_LOG_PER_DAY)
    }

    @Test
    fun `easy and medium rely on the structural slot limits`() {
        assertNull(XpLimits.dailyLimitFor(Difficulty.EASY))
        assertNull(XpLimits.dailyLimitFor(Difficulty.MEDIUM))
    }

    @Test
    fun `hard and epic carry their own daily ceilings`() {
        assertEquals(3, XpLimits.dailyLimitFor(Difficulty.HARD))
        assertEquals(1, XpLimits.dailyLimitFor(Difficulty.EPIC))
    }

    @Test
    fun `the hardest difficulty is never the most permissive`() {
        val hard = XpLimits.dailyLimitFor(Difficulty.HARD)
        val epic = XpLimits.dailyLimitFor(Difficulty.EPIC)
        assertTrue(hard != null && epic != null && epic <= hard)
    }

    @Test
    fun `no single award can exceed the daily stat cap`() {
        Difficulty.entries.forEach { difficulty ->
            assertTrue(
                "${difficulty.name} base xp outgrows the daily cap",
                difficulty.baseXp <= XpCurve.DAILY_STAT_CAP,
            )
        }
    }
}
