package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.repository.XpAward

fun XpAward.toUserMessage(): String? = when (this) {
    is XpAward.Granted -> buildString {
        append("+${result.finalXp} XP (${statType.displayName()})")
        if (statIncreased) append(" · ${statType.displayName()} $newStatValue oldu!")
        if (leveledUp) append(" · SEVİYE $newLevel!")
        if (featChoicesGained > 0) append(" · Yeni yetenek hakkı!")
    }
    is XpAward.Rejected -> when (reason) {
        XpAward.RejectReason.ALREADY_AWARDED_TODAY ->
            "Bu görevden bugün zaten XP kazandın."
        XpAward.RejectReason.DAILY_DIFFICULTY_LIMIT ->
            "Bu zorlukta günlük sınıra ulaştın."
        XpAward.RejectReason.DAILY_STAT_CAP ->
            "Bu yetenekte günlük XP tavanına ulaştın."
        XpAward.RejectReason.NOT_ELIGIBLE -> null
    }
}