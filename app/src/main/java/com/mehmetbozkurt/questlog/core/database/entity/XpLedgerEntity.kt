package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "xp_ledger",
    indices = [
        Index("userId"),
        Index("earnedAtMillis"),
        Index(value = ["userId", "statType", "earnedAtMillis"]),
    ],
)
data class XpLedgerEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val logId: String,
    val statType: String,
    val baseXp: Int,
    val finalXp: Int,
    val earnedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)