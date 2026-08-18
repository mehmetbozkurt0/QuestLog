package com.mehmetbozkurt.questlog.data.mapper

import com.mehmetbozkurt.questlog.core.database.entity.CharacterEntity
import com.mehmetbozkurt.questlog.core.database.entity.FeatEntity
import com.mehmetbozkurt.questlog.core.database.entity.StatEntity
import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.StatProgress
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import java.time.Instant

fun StatEntity.toDomain(): StatProgress = StatProgress(
    statType = StatType.valueOf(statType),
    value = value,
    currentXp = currentXp,
    xpToNext = XpCurve.xpForStatIncrease(value),
)

fun FeatEntity.toDomain(): AcquiredFeat = AcquiredFeat(
    featId = FeatId.valueOf(featId),
    chosenStat = chosenStat?.let { StatType.valueOf(it) },
    acquiredAtLevel = acquiredAtLevel,
)

fun buildCharacterSheet(
    entity: CharacterEntity,
    statEntities: List<StatEntity>,
): CharacterSheet {
    val levelInfo = XpCurve.levelFromTotalXp(entity.totalXp)
    val stats = StatType.entries.map { type ->
        statEntities.firstOrNull { it.statType == type.name }?.toDomain()
            ?: StatProgress(
                statType = type,
                value = XpCurve.MIN_STAT,
                currentXp = 0,
                xpToNext = XpCurve.xpForStatIncrease(XpCurve.MIN_STAT),
            )
    }

    return CharacterSheet(
        userId = entity.userId,
        level = levelInfo.level,
        totalXp = entity.totalXp,
        xpIntoLevel = levelInfo.xpIntoLevel,
        xpToNextLevel = levelInfo.xpToNextLevel,
        epicBoons = levelInfo.epicBoons,
        pendingFeatChoices = entity.pendingFeatChoices,
        stats = stats,
        createdAt = Instant.ofEpochMilli(entity.createdAtMillis),
    )
}