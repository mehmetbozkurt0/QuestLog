package com.mehmetbozkurt.questlog.domain.progression

import com.mehmetbozkurt.questlog.domain.model.AcquiredFeat
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.StatType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpEngineTest {

    private fun atHour(hour: Int): Instant =
        LocalDateTime.of(2026, 9, 2, hour, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()

    private fun context(
        difficulty: Difficulty = Difficulty.EASY,
        statType: StatType = StatType.STR,
        proofLevel: ProofLevel = ProofLevel.NONE,
        hour: Int = 12,
        feats: List<AcquiredFeat> = emptyList(),
        distinctStatsToday: Set<StatType> = emptySet(),
        earnedToday: Int = 0,
        isNewMember: Boolean = false,
    ) = XpContext(
        difficulty = difficulty,
        statType = statType,
        proofLevel = proofLevel,
        completedAt = atHour(hour),
        feats = feats,
        distinctStatsToday = distinctStatsToday,
        xpAlreadyEarnedTodayForStat = earnedToday,
        isNewMember = isNewMember,
    )

    private fun feat(id: FeatId, stat: StatType? = null) =
        AcquiredFeat(featId = id, chosenStat = stat, acquiredAtLevel = 4)

    @Test
    fun `plain quest awards its base xp`() {
        val result = XpEngine.calculate(context())
        assertEquals(10, result.baseXp)
        assertEquals(10, result.finalXp)
        assertEquals(0, result.cappedAmount)
        assertTrue(result.appliedBonuses.isEmpty())
    }

    @Test
    fun `each difficulty carries its own base`() {
        assertEquals(10, XpEngine.calculate(context(difficulty = Difficulty.EASY)).finalXp)
        assertEquals(25, XpEngine.calculate(context(difficulty = Difficulty.MEDIUM)).finalXp)
        assertEquals(60, XpEngine.calculate(context(difficulty = Difficulty.HARD)).finalXp)
        assertEquals(150, XpEngine.calculate(context(difficulty = Difficulty.EPIC)).finalXp)
    }

    @Test
    fun `note proof adds fifteen percent`() {
        val result = XpEngine.calculate(context(proofLevel = ProofLevel.NOTE))
        assertEquals(12, result.finalXp)
        assertEquals(listOf(XpBonus.Kind.PROOF), result.appliedBonuses.map { it.kind })
        assertEquals(15, result.appliedBonuses.single().percent)
    }

    @Test
    fun `photo proof adds thirty percent`() {
        val result = XpEngine.calculate(
            context(difficulty = Difficulty.MEDIUM, proofLevel = ProofLevel.PHOTO),
        )
        assertEquals(33, result.finalXp)
        assertEquals(30, result.appliedBonuses.single().percent)
    }

    @Test
    fun `specialist only fires on its chosen stat`() {
        val matching = XpEngine.calculate(
            context(
                statType = StatType.INT,
                feats = listOf(feat(FeatId.SPECIALIST, StatType.INT)),
            ),
        )
        assertEquals(13, matching.finalXp)
        assertEquals(listOf(XpBonus.Kind.SPECIALIST), matching.appliedBonuses.map { it.kind })

        val other = XpEngine.calculate(
            context(
                statType = StatType.CHA,
                feats = listOf(feat(FeatId.SPECIALIST, StatType.INT)),
            ),
        )
        assertEquals(10, other.finalXp)
        assertTrue(other.appliedBonuses.isEmpty())
    }

    @Test
    fun `early riser fires before nine and not at nine`() {
        val early = XpEngine.calculate(
            context(hour = 6, feats = listOf(feat(FeatId.EARLY_RISER))),
        )
        assertEquals(12, early.finalXp)

        val late = XpEngine.calculate(
            context(hour = 9, feats = listOf(feat(FeatId.EARLY_RISER))),
        )
        assertEquals(10, late.finalXp)
        assertTrue(late.appliedBonuses.isEmpty())
    }

    @Test
    fun `enduring only fires on hard and epic`() {
        val hard = XpEngine.calculate(
            context(difficulty = Difficulty.HARD, feats = listOf(feat(FeatId.ENDURING))),
        )
        assertEquals(72, hard.finalXp)

        val easy = XpEngine.calculate(
            context(difficulty = Difficulty.EASY, feats = listOf(feat(FeatId.ENDURING))),
        )
        assertEquals(10, easy.finalXp)
    }

    @Test
    fun `versatile counts the quest being logged as the third stat`() {
        val third = XpEngine.calculate(
            context(
                statType = StatType.WIS,
                feats = listOf(feat(FeatId.VERSATILE)),
                distinctStatsToday = setOf(StatType.STR, StatType.DEX),
            ),
        )
        assertEquals(12, third.finalXp)

        val second = XpEngine.calculate(
            context(
                statType = StatType.DEX,
                feats = listOf(feat(FeatId.VERSATILE)),
                distinctStatsToday = setOf(StatType.STR, StatType.DEX),
            ),
        )
        assertEquals(10, second.finalXp)
    }

    @Test
    fun `new crew member gets half again`() {
        val result = XpEngine.calculate(context(isNewMember = true))
        assertEquals(15, result.finalXp)
        assertEquals(listOf(XpBonus.Kind.NEW_ADVENTURER), result.appliedBonuses.map { it.kind })
    }

    @Test
    fun `bonuses add rather than multiply`() {
        val result = XpEngine.calculate(
            context(
                statType = StatType.CON,
                proofLevel = ProofLevel.PHOTO,
                feats = listOf(feat(FeatId.SPECIALIST, StatType.CON)),
            ),
        )
        assertEquals(16, result.finalXp)
        assertEquals(2, result.appliedBonuses.size)
    }

    @Test
    fun `daily stat cap trims the award and reports the loss`() {
        val result = XpEngine.calculate(
            context(difficulty = Difficulty.EPIC, earnedToday = XpCurve.DAILY_STAT_CAP - 5),
        )
        assertEquals(5, result.finalXp)
        assertEquals(145, result.cappedAmount)
    }

    @Test
    fun `a stat already at its daily cap earns nothing more`() {
        val result = XpEngine.calculate(
            context(difficulty = Difficulty.EPIC, earnedToday = XpCurve.DAILY_STAT_CAP),
        )
        assertEquals(0, result.finalXp)
        assertEquals(150, result.cappedAmount)
    }

    @Test
    fun `overshooting the daily cap never produces negative xp`() {
        val result = XpEngine.calculate(
            context(earnedToday = XpCurve.DAILY_STAT_CAP + 500),
        )
        assertEquals(0, result.finalXp)
        assertTrue(result.cappedAmount >= 0)
    }

    @Test
    fun `stat xp raises the stat when the threshold is met`() {
        val update = XpEngine.applyStatXp(currentValue = 10, currentXp = 0, gainedXp = 100)
        assertEquals(11, update.newValue)
        assertEquals(0, update.remainingXp)
        assertEquals(1, update.increases)
    }

    @Test
    fun `stat xp can cross several thresholds at once`() {
        val update = XpEngine.applyStatXp(currentValue = 10, currentXp = 0, gainedXp = 260)
        assertEquals(12, update.newValue)
        assertEquals(10, update.remainingXp)
        assertEquals(2, update.increases)
    }

    @Test
    fun `stat xp below the threshold only accumulates`() {
        val update = XpEngine.applyStatXp(currentValue = 10, currentXp = 40, gainedXp = 30)
        assertEquals(10, update.newValue)
        assertEquals(70, update.remainingXp)
        assertEquals(0, update.increases)
    }

    @Test
    fun `a maxed stat absorbs no further xp`() {
        val update = XpEngine.applyStatXp(
            currentValue = XpCurve.MAX_STAT,
            currentXp = 500,
            gainedXp = 10_000,
        )
        assertEquals(XpCurve.MAX_STAT, update.newValue)
        assertEquals(500, update.remainingXp)
        assertEquals(0, update.increases)
    }

    @Test
    fun `undoing a quest can pull the stat back down`() {
        val update = XpEngine.removeStatXp(currentValue = 11, currentXp = 0, removedXp = 50)
        assertEquals(10, update.newValue)
        assertEquals(50, update.remainingXp)
    }

    @Test
    fun `undoing never drops a stat below its floor`() {
        val update = XpEngine.removeStatXp(
            currentValue = XpCurve.MIN_STAT,
            currentXp = 20,
            removedXp = 5_000,
        )
        assertEquals(XpCurve.MIN_STAT, update.newValue)
        assertEquals(0, update.remainingXp)
    }

    @Test
    fun `applying then undoing the same xp restores the original stat`() {
        val applied = XpEngine.applyStatXp(currentValue = 12, currentXp = 60, gainedXp = 400)
        val restored = XpEngine.removeStatXp(
            currentValue = applied.newValue,
            currentXp = applied.remainingXp,
            removedXp = 400,
        )
        assertEquals(12, restored.newValue)
        assertEquals(60, restored.remainingXp)
    }
}
