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
    val name: String,
    val description: String,
    val requiresStatChoice: Boolean = false,
)

data class AcquiredFeat(
    val featId: FeatId,
    val chosenStat: StatType?,
    val acquiredAtLevel: Int,
)

object FeatCatalog {
    val all = listOf(
        FeatDefinition(
            FeatId.RESOLUTE,
            "Kararlı",
            "Serin bozulduğunda bir günlük af hakkı kazanırsın.",
        ),
        FeatDefinition(
            FeatId.SPECIALIST,
            "Uzman",
            "Seçtiğin yetenekte %25 fazla XP kazanırsın.",
            requiresStatChoice = true,
        ),
        FeatDefinition(
            FeatId.VERSATILE,
            "Çok Yönlü",
            "Aynı gün 3 farklı yetenekte görev tamamlarsan %20 bonus.",
        ),
        FeatDefinition(
            FeatId.EARLY_RISER,
            "Erkenci",
            "Sabah 09:00'dan önce tamamlanan görevlerde %20 bonus.",
        ),
        FeatDefinition(
            FeatId.ENDURING,
            "Dayanıklı",
            "Zor ve destansı görevlerde %20 bonus.",
        ),
        FeatDefinition(
            FeatId.MENTOR,
            "Mentor",
            "Grup arkadaşının görevini onayladığında ikiniz de XP alırsınız.",
        ),
    )

    fun byId(id: FeatId): FeatDefinition = all.first { it.id == id }
    val selectable: List<FeatDefinition> = all
}