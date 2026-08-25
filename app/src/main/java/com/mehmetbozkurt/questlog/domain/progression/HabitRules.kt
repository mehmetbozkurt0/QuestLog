package com.mehmetbozkurt.questlog.domain.progression

object HabitRules {
    const val MAX_SLOTS = 3

    val slotRange: IntRange = 0 until MAX_SLOTS

    fun isValidSlot(index: Int): Boolean = index in slotRange

    fun isBurnedToday(lastCompletedDayMillis: Long, startOfTodayMillis: Long): Boolean =
        lastCompletedDayMillis >= startOfTodayMillis
}
