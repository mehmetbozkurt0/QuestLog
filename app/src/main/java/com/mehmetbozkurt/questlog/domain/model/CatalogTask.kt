package com.mehmetbozkurt.questlog.domain.model

data class CatalogTask(
    val id: String,
    val title: String,
    val description: String,
    val titleEn: String?,
    val descriptionEn: String?,
    val statType: StatType,
    val difficulty: Difficulty,
    val sortOrder: Int,
)

data class CatalogEntry(
    val task: CatalogTask,
    val completions: Int,
    val doneToday: Boolean,
)
