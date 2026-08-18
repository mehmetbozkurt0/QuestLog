package com.mehmetbozkurt.questlog.domain.progression

object PathwayRules {
    const val IMMEDIATE_SHARE = 0.40

    const val ESCROW_SHARE = 0.60

    const val MAX_ACTIVE_PATHWAYS = 2

    const val INACTIVITY_DAYS = 14

    const val INACTIVITY_DAYS_RESOLUTE = 21

    const val WARNING_THRESHOLD_DAYS = 4

    fun splitXp(totalXp: Int): XpSplit {
        val immediate = (totalXp * IMMEDIATE_SHARE).toInt()
        return XpSplit(immediate = immediate, escrowed = totalXp - immediate)
    }

    data class XpSplit(val immediate: Int, val escrowed: Int)
}