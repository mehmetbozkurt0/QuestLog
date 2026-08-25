package com.mehmetbozkurt.questlog.domain.progression

import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.StatType
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

data class XpContext(
    val difficulty: Difficulty,
    val statType: StatType,
    val proofLevel: ProofLevel,
    val completedAt: Instant,
    val feats: List<AcquiredFeat>,
    val distinctStatsToday: Set<StatType>,
    val xpAlreadyEarnedTodayForStat: Int,
    val isNewMember: Boolean = false
)

data class XpResult(
    val baseXp: Int,
    val finalXp: Int,
    val cappedAmount: Int,
    val appliedBonuses: List<XpBonus>
)

data class XpBonus(val kind: Kind, val percent: Int) {
    enum class Kind {
        PROOF,
        SPECIALIST,
        EARLY_RISER,
        ENDURING,
        VERSATILE,
        NEW_ADVENTURER,
    }
}

object XpEngine {
    fun calculate(context: XpContext): XpResult {
        val base = context.difficulty.baseXp
        var multiplier = context.proofLevel.multiplier
        val bonuses = mutableListOf<XpBonus>()

        if (context.proofLevel != ProofLevel.NONE) {
            bonuses += XpBonus(
                XpBonus.Kind.PROOF,
                ((context.proofLevel.multiplier - 1) * 100).roundToInt(),
            )
        }

        context.feats.forEach { feat ->
            when (feat.featId) {
                FeatId.SPECIALIST -> if (feat.chosenStat == context.statType) {
                    multiplier += 0.25
                    bonuses += XpBonus(XpBonus.Kind.SPECIALIST, 25)
                }
                FeatId.EARLY_RISER -> {
                    val hour = context.completedAt.atZone(ZoneId.systemDefault()).hour
                    if (hour < 9) {
                        multiplier += 0.20
                        bonuses += XpBonus(XpBonus.Kind.EARLY_RISER, 20)
                    }
                }
                FeatId.ENDURING -> if (
                    context.difficulty == Difficulty.HARD || context.difficulty == Difficulty.EPIC
                ) {
                    multiplier += 0.20
                    bonuses += XpBonus(XpBonus.Kind.ENDURING, 20)
                }
                FeatId.VERSATILE -> {
                    val distinct = context.distinctStatsToday + context.statType
                    if (distinct.size >= 3) {
                        multiplier += 0.20
                        bonuses += XpBonus(XpBonus.Kind.VERSATILE, 20)
                    }
                }
                else -> Unit
            }
        }

        if (context.isNewMember) {
            multiplier += 0.50
            bonuses += XpBonus(XpBonus.Kind.NEW_ADVENTURER, 50)
        }

        val raw = (base * multiplier).roundToInt()

        val remaining = (XpCurve.DAILY_STAT_CAP - context.xpAlreadyEarnedTodayForStat)
            .coerceAtLeast(0)
        val granted = raw.coerceAtMost(remaining)

        return XpResult(
            baseXp = base,
            finalXp = granted,
            cappedAmount = raw - granted,
            appliedBonuses = bonuses,
        )
    }

    fun applyStatXp(
        currentValue: Int,
        currentXp: Int,
        gainedXp: Int,
    ): StatUpdate {
        if (currentValue >= XpCurve.MAX_STAT) {
            return StatUpdate(currentValue, currentXp, 0)
        }

        var value = currentValue
        var xp = currentXp + gainedXp
        var increases = 0

        while (value < XpCurve.MAX_STAT) {
            val needed = XpCurve.xpForStatIncrease(value)
            if (xp < needed) break
            xp -= needed
            value++
            increases++
        }

        return StatUpdate(value, xp, increases)
    }

    data class StatUpdate(
        val newValue: Int,
        val remainingXp: Int,
        val increases: Int,
    )

    fun removeStatXp(
        currentValue: Int,
        currentXp: Int,
        removedXp: Int,
    ): StatUpdate {
        var value = currentValue
        var xp = currentXp - removedXp

        while (xp < 0 && value > XpCurve.MIN_STAT) {
            value--
            xp += XpCurve.xpForStatIncrease(value)
        }

        return StatUpdate(
            newValue = value,
            remainingXp = xp.coerceAtLeast(0),
            increases = value - currentValue,
        )
    }
}