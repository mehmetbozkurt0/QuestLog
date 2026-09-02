package com.mehmetbozkurt.questlog.domain.progression

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrewRulesTest {

    private val day = 24L * 60L * 60L * 1000L
    private val now = 1_756_764_000_000L

    @Test
    fun `invite codes are six characters long`() {
        repeat(200) {
            assertEquals(
                CrewRules.INVITE_CODE_LENGTH,
                CrewRules.generateInviteCode(Random(it)).length,
            )
        }
    }

    @Test
    fun `invite codes avoid characters that are easy to misread`() {
        val forbidden = setOf('I', 'O', '0', '1')
        repeat(500) { seed ->
            val code = CrewRules.generateInviteCode(Random(seed))
            code.forEach { char ->
                assertTrue("$code contains $char", char !in forbidden)
                assertTrue("$code is not uppercase alphanumeric", char.isLetterOrDigit())
                assertTrue("$code has a lowercase letter", !char.isLowerCase())
            }
        }
    }

    @Test
    fun `invite codes are reproducible for a given seed`() {
        assertEquals(
            CrewRules.generateInviteCode(Random(42)),
            CrewRules.generateInviteCode(Random(42)),
        )
    }

    @Test
    fun `invite codes are not all the same`() {
        val codes = (1..100).map { CrewRules.generateInviteCode(Random(it)) }.toSet()
        assertTrue("codes collapsed to ${codes.size} values", codes.size > 90)
    }

    @Test
    fun `a member with no join date is not new`() {
        assertFalse(CrewRules.isNewMember(null, now))
    }

    @Test
    fun `a member who just joined is new`() {
        assertTrue(CrewRules.isNewMember(now, now))
        assertTrue(CrewRules.isNewMember(now - day, now))
    }

    @Test
    fun `the new member bonus expires after seven days`() {
        assertTrue(CrewRules.isNewMember(now - 7 * day + 1, now))
        assertFalse(CrewRules.isNewMember(now - 7 * day, now))
        assertFalse(CrewRules.isNewMember(now - 30 * day, now))
    }

    @Test
    fun `a join date in the future does not count as new`() {
        assertFalse(CrewRules.isNewMember(now + day, now))
    }

    @Test
    fun `crew limits match what the firestore rules enforce`() {
        assertEquals(12, CrewRules.MAX_MEMBERS)
        assertEquals(3, CrewRules.NAME_MIN_LENGTH)
        assertEquals(40, CrewRules.NAME_MAX_LENGTH)
        assertEquals(6, CrewRules.INVITE_CODE_LENGTH)
    }
}
