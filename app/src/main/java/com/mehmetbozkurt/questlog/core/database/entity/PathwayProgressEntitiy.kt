package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "pathway_progress",
    primaryKeys = ["userId", "pathwayId"],
    indices = [Index("userId")],
)
data class PathwayProgressEntity(
    val userId: String,
    val pathwayId: String,
    val startedAtMillis: Long,
    val lastActivityAtMillis: Long,
    val escrowedXp: Int,
    val completedAtMillis: Long?,
    val abandonedAtMillis: Long?,
    val syncState: String = SyncState.PENDING.name,
)