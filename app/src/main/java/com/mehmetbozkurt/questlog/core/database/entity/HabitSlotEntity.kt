package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity

@Entity(
    tableName = "habit_slots",
    primaryKeys = ["userId", "slotIndex"],
)
data class HabitSlotEntity(
    val userId: String,
    val slotIndex: Int,
    val lastCompletedDayMillis: Long = 0L,
    val updatedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)
