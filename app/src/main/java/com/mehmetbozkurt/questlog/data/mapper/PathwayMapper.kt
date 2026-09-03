package com.mehmetbozkurt.questlog.data.mapper

import com.mehmetbozkurt.questlog.core.database.entity.PathwayEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayProgressEntity
import com.mehmetbozkurt.questlog.core.database.entity.PathwayQuestEntity
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.Pathway
import com.mehmetbozkurt.questlog.domain.model.PathwayProgress
import com.mehmetbozkurt.questlog.domain.model.PathwayQuest
import com.mehmetbozkurt.questlog.domain.model.StatType
import java.time.Instant

fun PathwayEntity.toDomain(): Pathway? {
    val primary = runCatching { StatType.valueOf(primaryStat) }.getOrNull() ?: return null
    return Pathway(
        id = id,
        title = title,
        description = description,
        primaryStat = primary,
        titleEn = titleEn,
        descriptionEn = descriptionEn,
        secondaryStat = secondaryStat?.let {
            runCatching { StatType.valueOf(it) }.getOrNull()
        },
        tier = tier,
        requiredPathwayId = requiredPathwayId,
        completionBonusXp = completionBonusXp,
        sortOrder = sortOrder,
    )
}

fun PathwayQuestEntity.toDomain(): PathwayQuest? {
    val stat = runCatching { StatType.valueOf(statType) }.getOrNull() ?: return null
    val diff = runCatching { Difficulty.valueOf(difficulty) }.getOrNull() ?: return null
    return PathwayQuest(
        id = id,
        pathwayId = pathwayId,
        title = title,
        description = description,
        titleEn = titleEn,
        descriptionEn = descriptionEn,
        statType = stat,
        difficulty = diff,
        stage = stage,
        requiredCompletions = requiredCompletions,
        sortOrder = sortOrder,
    )
}

fun PathwayProgressEntity.toDomain(): PathwayProgress = PathwayProgress(
    pathwayId = pathwayId,
    userId = userId,
    startedAt = Instant.ofEpochMilli(startedAtMillis),
    lastActivityAt = Instant.ofEpochMilli(lastActivityAtMillis),
    escrowedXp = escrowedXp,
    completedAt = completedAtMillis?.let(Instant::ofEpochMilli),
    abandonedAt = abandonedAtMillis?.let(Instant::ofEpochMilli),
)