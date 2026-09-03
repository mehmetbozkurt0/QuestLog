package com.mehmetbozkurt.questlog.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pathways",
    indices = [Index("primaryStat")],
)
data class PathwayEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val titleEn: String? = null,
    val descriptionEn: String? = null,
    val primaryStat: String,
    val secondaryStat: String?,
    val tier: Int,
    val requiredPathwayId: String?,
    val completionBonusXp: Int,
    val sortOrder: Int,
)