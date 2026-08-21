package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crews")
data class CrewEntity(
    @PrimaryKey val crewId: String,
    val name: String,
    val inviteCode: String,
    val ownerId: String,
    val memberIdsCsv: String,
    val updatedAtMillis: Long,
) {
    val memberIds: List<String>
        get() = memberIdsCsv.split(",").filter { it.isNotBlank() }
}
