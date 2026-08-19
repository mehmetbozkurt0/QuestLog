package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletionEntity(
    @PrimaryKey val docId: String,
    val collection: String,
    val userId: String,
)
