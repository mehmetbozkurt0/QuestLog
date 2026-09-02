package com.mehmetbozkurt.questlog.domain.progression

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakEngineTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 2)

    private fun days(vararg agoInDays: Long): Set<LocalDate> =
        agoInDays.map { today.minusDays(it) }.toSet()

    @Test
    fun `no activity at all yields an empty streak`() {
        val info = StreakEngine.calculate(emptySet(), today, hasResolute = false)
        assertEquals(StreakInfo.EMPTY, info)
    }

    @Test
    fun `a single day today is a streak of one`() {
        val info = StreakEngine.calculate(days(0), today, hasResolute = false)
        assertEquals(1, info.currentStreak)
        assertEquals(1, info.longestStreak)
        assertTrue(info.activeToday)
        assertFalse(info.graceUsed)
    }

    @Test
    fun `consecutive days ending today count up`() {
        val info = StreakEngine.calculate(days(0, 1, 2, 3), today, hasResolute = false)
        assertEquals(4, info.currentStreak)
        assertEquals(4, info.longestStreak)
        assertTrue(info.activeToday)
    }

    @Test
    fun `an untouched today does not break yesterday's streak`() {
        val info = StreakEngine.calculate(days(1, 2, 3), today, hasResolute = false)
        assertEquals(3, info.currentStreak)
        assertFalse(info.activeToday)
    }

    @Test
    fun `a missed day breaks the streak without resolute`() {
        val info = StreakEngine.calculate(days(0, 1, 3, 4), today, hasResolute = false)
        assertEquals(2, info.currentStreak)
        assertFalse(info.graceUsed)
    }

    @Test
    fun `resolute forgives one missed day`() {
        val info = StreakEngine.calculate(days(0, 1, 3, 4), today, hasResolute = true)
        assertEquals(4, info.currentStreak)
        assertTrue(info.graceUsed)
    }

    @Test
    fun `resolute forgives only one missed day per streak`() {
        val info = StreakEngine.calculate(days(0, 1, 3, 5, 6), today, hasResolute = true)
        assertEquals(3, info.currentStreak)
        assertTrue(info.graceUsed)
    }

    @Test
    fun `resolute cannot bridge two missed days in a row`() {
        val info = StreakEngine.calculate(days(0, 1, 4, 5), today, hasResolute = true)
        assertEquals(2, info.currentStreak)
        assertFalse(info.graceUsed)
    }

    @Test
    fun `a streak that ended days ago reports zero current`() {
        val info = StreakEngine.calculate(days(2, 3, 4, 5), today, hasResolute = false)
        assertEquals(0, info.currentStreak)
        assertEquals(4, info.longestStreak)
        assertFalse(info.activeToday)
        assertFalse(info.graceUsed)
    }

    @Test
    fun `grace is never reported when there is no live streak`() {
        val info = StreakEngine.calculate(days(5), today, hasResolute = true)
        assertEquals(0, info.currentStreak)
        assertFalse(info.graceUsed)
    }

    @Test
    fun `longest streak survives a later shorter run`() {
        val info = StreakEngine.calculate(
            days(0, 1, 10, 11, 12, 13, 14),
            today,
            hasResolute = false,
        )
        assertEquals(2, info.currentStreak)
        assertEquals(5, info.longestStreak)
    }

    @Test
    fun `longest streak is never smaller than the current one`() {
        val info = StreakEngine.calculate(days(0, 1, 2), today, hasResolute = false)
        assertTrue(info.longestStreak >= info.currentStreak)
    }

    @Test
    fun `milestones are the advertised set`() {
        assertEquals(setOf(3, 7, 14, 30, 50, 100, 365), StreakEngine.MILESTONES)
    }

    @Test
    fun `out of order input is handled the same as sorted input`() {
        val scrambled = setOf(
            today.minusDays(2),
            today,
            today.minusDays(4),
            today.minusDays(1),
            today.minusDays(3),
        )
        val info = StreakEngine.calculate(scrambled, today, hasResolute = false)
        assertEquals(5, info.currentStreak)
        assertEquals(5, info.longestStreak)
    }

    @Test
    fun `future dated activity does not inflate the streak`() {
        val info = StreakEngine.calculate(
            days(0, 1) + today.plusDays(1),
            today,
            hasResolute = false,
        )
        assertEquals(2, info.currentStreak)
    }
}
