package com.mehmetbozkurt.questlog.domain.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogRulesTest {

    private val startOfToday = 1_756_764_000_000L

    @Test
    fun `the daily catalog ceiling is two`() {
        assertEquals(2, CatalogRules.MAX_PER_DAY)
    }

    @Test
    fun `a task never completed is available`() {
        assertFalse(CatalogRules.isDoneToday(0L, startOfToday))
    }

    @Test
    fun `a task completed before midnight is available again`() {
        assertFalse(CatalogRules.isDoneToday(startOfToday - 1, startOfToday))
    }

    @Test
    fun `a task completed today is locked for the rest of the day`() {
        assertTrue(CatalogRules.isDoneToday(startOfToday, startOfToday))
        assertTrue(CatalogRules.isDoneToday(startOfToday + 1, startOfToday))
    }

    @Test
    fun `the two structural limits together cap the daily faucet`() {
        val maxHabitAwards = HabitRules.MAX_SLOTS
        val maxCatalogAwards = CatalogRules.MAX_PER_DAY
        assertEquals(5, maxHabitAwards + maxCatalogAwards)
    }
}
