package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.repository.QuestCompletionResult

fun QuestCompletionResult.toUserMessage(): String = when (this) {
    is QuestCompletionResult.Rejected -> reason

    is QuestCompletionResult.Success -> buildString {
        append("+$immediateXp XP · $escrowedXp emanette")
        if (statIncreased) {
            append(" · ${statType.displayName()} $newStatValue oldu!")
        }
        if (stageCompleted) append(" · Aşama tamamlandı!")
        if (pathwayCompleted) {
            append(" · YOL TAMAMLANDI! +${releasedXp + bonusXp} XP")
        }
        if (leveledUp) append(" · SEVİYE $newLevel!")
        if (featChoicesGained > 0) append(" · Yeni yetenek hakkı!")
    }
}