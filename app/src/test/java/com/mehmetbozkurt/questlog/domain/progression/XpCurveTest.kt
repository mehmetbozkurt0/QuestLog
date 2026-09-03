package com.mehmetbozkurt.questlog.domain.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpCurveTest {

    @Test
    fun `stat increase cost follows the 1_5x growth curve`() {
        assertEquals(100, XpCurve.xpForStatIncrease(10))
        assertEquals(150, XpCurve.xpForStatIncrease(11))
        assertEquals(225, XpCurve.xpForStatIncrease(12))
        assertEquals(338, XpCurve.xpForStatIncrease(13))
        assertEquals(3844, XpCurve.xpForStatIncrease(19))
    }

    @Test
    fun `stat increase is impossible at the cap`() {
        assertEquals(Int.MAX_VALUE, XpCurve.xpForStatIncrease(XpCurve.MAX_STAT))
        assertEquals(Int.MAX_VALUE, XpCurve.xpForStatIncrease(XpCurve.MAX_STAT + 1))
    }

    @Test
    fun `level cost follows the 1_3x growth curve`() {
        assertEquals(400, XpCurve.xpForLevelUp(1))
        assertEquals(520, XpCurve.xpForLevelUp(2))
        assertEquals(676, XpCurve.xpForLevelUp(3))
        assertEquals(879, XpCurve.xpForLevelUp(4))
        assertEquals(44982, XpCurve.xpForLevelUp(19))
    }

    @Test
    fun `level up is impossible at the cap`() {
        assertEquals(Int.MAX_VALUE, XpCurve.xpForLevelUp(XpCurve.MAX_LEVEL))
    }

    @Test
    fun `total xp needed to reach max level is stable`() {
        assertEquals(193_590, XpCurve.xpToMaxLevel)
    }

    @Test
    fun `fresh character is level one with no progress`() {
        val info = XpCurve.levelFromTotalXp(0)
        assertEquals(1, info.level)
        assertEquals(0, info.xpIntoLevel)
        assertEquals(400, info.xpToNextLevel)
        assertEquals(0, info.epicBoons)
    }

    @Test
    fun `one xp short of a level up stays on the old level`() {
        val info = XpCurve.levelFromTotalXp(399)
        assertEquals(1, info.level)
        assertEquals(399, info.xpIntoLevel)
    }

    @Test
    fun `exact threshold levels up with no leftover`() {
        val info = XpCurve.levelFromTotalXp(400)
        assertEquals(2, info.level)
        assertEquals(0, info.xpIntoLevel)
        assertEquals(520, info.xpToNextLevel)
    }

    @Test
    fun `leftover xp carries into the new level`() {
        val info = XpCurve.levelFromTotalXp(1200)
        assertEquals(3, info.level)
        assertEquals(280, info.xpIntoLevel)
        assertEquals(676, info.xpToNextLevel)
    }

    @Test
    fun `reaching max level reports no boons yet`() {
        val info = XpCurve.levelFromTotalXp(XpCurve.xpToMaxLevel)
        assertEquals(XpCurve.MAX_LEVEL, info.level)
        assertEquals(0, info.epicBoons)
        assertEquals(0, info.xpIntoLevel)
        assertEquals(XpCurve.XP_PER_EPIC_BOON, info.xpToNextLevel)
    }

    @Test
    fun `xp past max level accumulates epic boons`() {
        val info = XpCurve.levelFromTotalXp(
            XpCurve.xpToMaxLevel + 2 * XpCurve.XP_PER_EPIC_BOON + 12_345,
        )
        assertEquals(XpCurve.MAX_LEVEL, info.level)
        assertEquals(2, info.epicBoons)
        assertEquals(12_345, info.xpIntoLevel)
    }

    @Test
    fun `feat levels are awarded once each on the way up`() {
        assertEquals(0, XpCurve.featChoicesBetween(1, 3))
        assertEquals(1, XpCurve.featChoicesBetween(1, 4))
        assertEquals(0, XpCurve.featChoicesBetween(4, 4))
        assertEquals(2, XpCurve.featChoicesBetween(3, 8))
        assertEquals(1, XpCurve.featChoicesBetween(16, 19))
    }

    @Test
    fun `a run from level one to the cap grants every feat exactly once`() {
        assertEquals(
            XpCurve.FEAT_LEVELS.size,
            XpCurve.featChoicesBetween(1, XpCurve.MAX_LEVEL),
        )
    }

    @Test
    fun `level derived from xp is monotonic across the whole curve`() {
        var previous = 1
        var xp = 0
        while (xp <= XpCurve.xpToMaxLevel) {
            val level = XpCurve.levelFromTotalXp(xp).level
            assertTrue("level went backwards at $xp", level >= previous)
            previous = level
            xp += 977
        }
        assertEquals(
            XpCurve.MAX_LEVEL,
            XpCurve.levelFromTotalXp(XpCurve.xpToMaxLevel).level,
        )
        assertEquals(
            XpCurve.MAX_LEVEL - 1,
            XpCurve.levelFromTotalXp(XpCurve.xpToMaxLevel - 1).level,
        )
    }
}
