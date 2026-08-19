package com.mehmetbozkurt.questlog.domain.model

import java.time.LocalDate

data class DayActivity(val date: LocalDate, val xp: Int)
data class WeeklySummary (
    val days: List<DayActivity>,
    val totalXp: Int,
    val entryCount: Int,
    val topStat: StatType?
) {
    val maxDayXp: Int get() = days.maxOfOrNull { it.xp } ?: 0
}