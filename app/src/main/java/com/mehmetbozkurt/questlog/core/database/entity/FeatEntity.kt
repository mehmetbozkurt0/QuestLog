package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feats",
    indices = [Index("userId")],
)
data class FeatEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val featId: String,
    val chosenStat: String?,
    val acquiredAtLevel: Int,
    val acquiredAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)