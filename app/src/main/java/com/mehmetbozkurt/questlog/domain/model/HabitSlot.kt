package com.mehmetbozkurt.questlog.domain.model

data class HabitSlot(
    val index: Int,
    val quest: QuestLog?,
    val burnedToday: Boolean,
) {
    val isEmpty: Boolean get() = quest == null
}
