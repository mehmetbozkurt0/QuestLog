package com.mehmetbozkurt.questlog.domain.progression

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val activeToday: Boolean,
    val graceUsed: Boolean
) {
    companion object {
        val EMPTY = StreakInfo(0, 0, activeToday = false, graceUsed = false)
    }
}

object StreakEngine{
    val MILESTONES = setOf(3, 7, 14, 30, 50, 100, 365)
    fun calculate(
        activeDays: Set<LocalDate>,
        today: LocalDate,
        hasResolute: Boolean
    ): StreakInfo {
        if (activeDays.isEmpty()) return StreakInfo.EMPTY

        val activeToday = today in activeDays
        var current = 0
        var graceUsed = false
        var graceAvailable = hasResolute
        var day = if (activeToday) today else today.minusDays(1)

        while (true) {
            if (day in activeDays) {
                current++
                day = day.minusDays(1)
            } else if (graceAvailable && day.minusDays(1) in activeDays) {
                graceAvailable = false
                graceUsed = true
                day = day.minusDays(1)
            }else {
                break
            }
        }

        if (current == 0) graceUsed = false

        return StreakInfo(
            currentStreak = current,
            longestStreak = longestStreak(activeDays, hasResolute).coerceAtLeast(current),
            activeToday = activeToday,
            graceUsed = graceUsed
        )
    }

    private fun longestStreak(activeDays: Set<LocalDate>, hasResolute: Boolean): Int {
        val sorted = activeDays.sorted()
        var longest = 1
        var run = 1
        var graceUsedInRun = false

        for (i in 1 until sorted.size) {
            val gap = ChronoUnit.DAYS.between(sorted[i - 1], sorted[i])
            when {
                gap == 1L -> run++
                gap == 2L && hasResolute && !graceUsedInRun -> {
                    run++
                    graceUsedInRun = true
                }
                else -> {
                    run = 1
                    graceUsedInRun = false
                }
            }
            if (run > longest) longest = run
        }
        return longest
    }
}