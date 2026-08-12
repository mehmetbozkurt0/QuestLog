package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncState {SYNCED, PENDING, FAILED}

@Entity(
    tableName = "quest_logs",
    indices = [
        Index("ownerId"),
        Index("campaignId"),
        Index("categoryId"),
        Index("isDeleted")
    ]
)
data class QuestLogEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val campaignId: String?,
    val type: String,
    val title: String,
    val description: String,
    val categoryId: String?,
    val priority: String?,
    val dueAtMillis: Long?,
    val remindAtMillis: Long?,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isDeleted: Boolean = false,
    val syncState: String = SyncState.PENDING.name
)