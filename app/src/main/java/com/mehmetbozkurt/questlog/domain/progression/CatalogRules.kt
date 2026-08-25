package com.mehmetbozkurt.questlog.domain.progression

object CatalogRules {
    const val MAX_PER_DAY = 2

    fun isDoneToday(lastCompletedAtMillis: Long, startOfTodayMillis: Long): Boolean =
        lastCompletedAtMillis >= startOfTodayMillis
}
