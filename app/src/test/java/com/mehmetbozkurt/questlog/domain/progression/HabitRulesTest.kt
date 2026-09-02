package com.mehmetbozkurt.questlog.domain.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitRulesTest {

    private val startOfToday = 1_756_764_000_000L
    private val oneHour = 60L * 60L * 1000L

    @Test
    fun `there are exactly three slots and they are zero based`() {
        assertEquals(3, HabitRules.MAX_SLOTS)
        assertEquals(0 until 3, HabitRules.slotRange)
    }

    @Test
    fun `slot indexes outside the range are rejected`() {
        assertTrue(HabitRules.isValidSlot(0))
        assertTrue(HabitRules.isValidSlot(2))
        assertFalse(HabitRules.isValidSlot(-1))
        assertFalse(HabitRules.isValidSlot(HabitRules.MAX_SLOTS))
    }

    @Test
    fun `a slot never completed is not burned`() {
        assertFalse(HabitRules.isBurnedToday(0L, startOfToday))
    }

    @Test
    fun `a slot completed yesterday is available again`() {
        assertFalse(HabitRules.isBurnedToday(startOfToday - 1, startOfToday))
        assertFalse(HabitRules.isBurnedToday(startOfToday - 20 * oneHour, startOfToday))
    }

    @Test
    fun `a slot completed at any point today is burned`() {
        assertTrue(HabitRules.isBurnedToday(startOfToday, startOfToday))
        assertTrue(HabitRules.isBurnedToday(startOfToday + 15 * oneHour, startOfToday))
    }
}
