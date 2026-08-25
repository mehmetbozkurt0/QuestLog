package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_tasks")
data class CatalogTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val titleEn: String? = null,
    val descriptionEn: String? = null,
    val statType: String,
    val difficulty: String,
    val sortOrder: Int,
)
