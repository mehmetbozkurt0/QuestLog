package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "catalog_quests",
    indices = [Index("statType")],
)
data class CatalogQuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val statType: String,
    val difficulty: String,
    val sortOrder: Int,
)