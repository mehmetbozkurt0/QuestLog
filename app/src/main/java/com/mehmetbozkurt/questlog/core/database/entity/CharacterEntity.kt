package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val userId: String,
    val totalXp: Int = 0,
    val pendingFeatChoices: Int = 0,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)