package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crew_members",
    indices = [Index("crewId")],
)
data class CrewMemberEntity(
    @PrimaryKey val userId: String,
    val crewId: String,
    val displayName: String,
    val level: Int,
    val totalXp: Int,
    val currentStreak: Int,
    val photoUrl: String? = null,
    val updatedAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)
