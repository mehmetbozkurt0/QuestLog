package com.mehmetbozkurt.questlog.domain.model

enum class FeatId {
    RESOLUTE,
    SPECIALIST,
    VERSATILE,
    EARLY_RISER,
    ENDURING,
    MENTOR,
}

data class FeatDefinition(
    val id: FeatId,
    val requiresStatChoice: Boolean = false,
)

data class AcquiredFeat(
    val featId: FeatId,
    val chosenStat: StatType?,
    val acquiredAtLevel: Int,
)

object FeatCatalog {
    val all = listOf(
        FeatDefinition(FeatId.RESOLUTE),
        FeatDefinition(FeatId.SPECIALIST, requiresStatChoice = true),
        FeatDefinition(FeatId.VERSATILE),
        FeatDefinition(FeatId.EARLY_RISER),
        FeatDefinition(FeatId.ENDURING),
        FeatDefinition(FeatId.MENTOR),
    )

    fun byId(id: FeatId): FeatDefinition = all.first { it.id == id }
    val selectable: List<FeatDefinition> = all
}
