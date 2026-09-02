package com.mehmetbozkurt.questlog.domain.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PathwayRulesTest {

    @Test
    fun `xp is split forty sixty between now and escrow`() {
        val split = PathwayRules.splitXp(100)
        assertEquals(40, split.immediate)
        assertEquals(60, split.escrowed)
    }

    @Test
    fun `a split never loses or invents xp`() {
        for (total in 0..2_000) {
            val split = PathwayRules.splitXp(total)
            assertEquals(
                "split of $total did not add up",
                total,
                split.immediate + split.escrowed,
            )
            assertTrue("negative immediate for $total", split.immediate >= 0)
            assertTrue("negative escrow for $total", split.escrowed >= 0)
        }
    }

    @Test
    fun `rounding leftovers always land in escrow`() {
        val split = PathwayRules.splitXp(1)
        assertEquals(0, split.immediate)
        assertEquals(1, split.escrowed)
    }

    @Test
    fun `zero xp splits into nothing`() {
        val split = PathwayRules.splitXp(0)
        assertEquals(0, split.immediate)
        assertEquals(0, split.escrowed)
    }

    @Test
    fun `shares add up to a whole`() {
        assertEquals(1.0, PathwayRules.IMMEDIATE_SHARE + PathwayRules.ESCROW_SHARE, 1e-9)
    }

    @Test
    fun `resolute buys a longer inactivity window`() {
        assertTrue(PathwayRules.INACTIVITY_DAYS_RESOLUTE > PathwayRules.INACTIVITY_DAYS)
        assertTrue(PathwayRules.WARNING_THRESHOLD_DAYS < PathwayRules.INACTIVITY_DAYS)
    }

    @Test
    fun `at most two pathways run at once`() {
        assertEquals(2, PathwayRules.MAX_ACTIVE_PATHWAYS)
    }
}
