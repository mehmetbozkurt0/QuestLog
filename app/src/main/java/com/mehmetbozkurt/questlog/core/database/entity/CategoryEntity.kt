package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index("ownerId")]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val colorHex: String,
    val createdAtMillis: Long,
    val isDeleted: Boolean = false,
    val syncState: String = SyncState.PENDING.name,
)