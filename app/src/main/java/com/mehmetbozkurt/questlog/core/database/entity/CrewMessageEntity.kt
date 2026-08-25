package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crew_messages",
    indices = [
        Index("crewId"),
        Index("sentAtMillis"),
    ],
)
data class CrewMessageEntity(
    @PrimaryKey val id: String,
    val crewId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val sentAtMillis: Long,
    val syncState: String = SyncState.PENDING.name,
)
