package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pathway_quests",
    indices = [Index("pathwayId")],
)
data class PathwayQuestEntity(
    @PrimaryKey val id: String,
    val pathwayId: String,
    val title: String,
    val description: String,
    val titleEn: String? = null,
    val descriptionEn: String? = null,
    val statType: String,
    val difficulty: String,
    val stage: Int,
    val requiredCompletions: Int,
    val sortOrder: Int,
)