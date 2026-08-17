package com.mehmetbozkurt.questlog.domain.model

data class CatalogQuest(
    val id: String,
    val title: String,
    val description: String,
    val statType: StatType,
    val difficulty: Difficulty,
    val order: Int,
)