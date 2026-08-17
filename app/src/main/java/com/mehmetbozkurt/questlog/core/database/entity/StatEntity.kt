package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "stats",
    primaryKeys = ["userId", "statType"],
    indices = [Index("userId")]
)
data class StatEntity(
    val userId: String,
    val statType: String,
    val value: Int = 10,
    val currentXp: Int = 0,
    val updatedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)