package com.mehmetbozkurt.questlog.data.mapper

import com.mehmetbozkurt.questlog.core.database.entity.CatalogQuestEntity
import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.StatType

fun CatalogQuestEntity.toDomain(): CatalogQuest? {
    val stat = runCatching { StatType.valueOf(statType) }.getOrNull() ?: return null
    val diff = runCatching { Difficulty.valueOf(difficulty) }.getOrNull() ?: return null

    return CatalogQuest(
        id = id,
        title = title,
        description = description,
        statType = stat,
        difficulty = diff,
        order = sortOrder,
    )
}