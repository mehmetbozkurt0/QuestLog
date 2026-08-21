package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crew_feed",
    indices = [
        Index("crewId"),
        Index("completedAtMillis"),
    ],
)
data class CrewFeedEntity(
    @PrimaryKey val id: String,
    val crewId: String,
    val authorId: String,
    val authorName: String,
    val questLogId: String,
    val title: String,
    val statType: String?,
    val difficulty: String?,
    val completedAtMillis: Long,
    val proofPhotoUrl: String? = null,
    val approvedByCsv: String = "",
    val syncState: String = SyncState.PENDING.name,
) {
    val approvedBy: List<String>
        get() = approvedByCsv.split(",").filter { it.isNotBlank() }
}
