package com.mehmetbozkurt.questlog.domain.progression

import kotlin.random.Random

object CrewRules {
    const val MENTOR_APPROVAL_XP = 10

    const val DAILY_APPROVAL_LIMIT = 5

    const val NEW_MEMBER_DAYS = 7L

    const val INVITE_CODE_LENGTH = 6

    const val MAX_MEMBERS = 12

    const val NAME_MIN_LENGTH = 3

    const val NAME_MAX_LENGTH = 40

    const val MESSAGE_MAX_LENGTH = 500

    private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generateInviteCode(random: Random = Random.Default): String =
        buildString {
            repeat(INVITE_CODE_LENGTH) {
                append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)])
            }
        }

    fun isNewMember(joinedAtMillis: Long?, nowMillis: Long): Boolean {
        if (joinedAtMillis == null) return false
        val elapsed = nowMillis - joinedAtMillis
        return elapsed in 0 until NEW_MEMBER_DAYS * 24 * 60 * 60 * 1000
    }
}
