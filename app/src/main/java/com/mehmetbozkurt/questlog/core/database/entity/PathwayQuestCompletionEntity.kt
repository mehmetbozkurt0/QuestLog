package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "pathway_quest_completions",
    primaryKeys = ["userId", "questId"],
    indices = [Index("userId"), Index("questId")],
)
data class PathwayQuestCompletionEntity(
    val userId: String,
    val questId: String,
    val completions: Int,
    val lastCompletedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)