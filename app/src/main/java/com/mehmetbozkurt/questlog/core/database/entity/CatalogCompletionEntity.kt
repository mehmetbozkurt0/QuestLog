package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "catalog_completions",
    primaryKeys = ["userId", "taskId"],
    indices = [Index("userId"), Index("taskId")],
)
data class CatalogCompletionEntity(
    val userId: String,
    val taskId: String,
    val completions: Int,
    val lastCompletedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)
