package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.domain.repository.XpAward

fun XpAward.Rejected.toUserMessage(): String? = when (reason) {
    XpAward.RejectReason.ALREADY_AWARDED_TODAY ->
        "Bu görevden bugün zaten XP kazandın."
    XpAward.RejectReason.DAILY_DIFFICULTY_LIMIT ->
        "Bu zorlukta günlük sınıra ulaştın."
    XpAward.RejectReason.DAILY_STAT_CAP ->
        "Bu yetenekte günlük XP tavanına ulaştın."
    XpAward.RejectReason.NOT_ELIGIBLE -> null
}