package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.domain.repository.XpAward

fun XpAward.Rejected.toUserMessage(): UiText? = when (reason) {
    XpAward.RejectReason.ALREADY_AWARDED_TODAY ->
        uiText(R.string.xp_reject_already_awarded_today)

    XpAward.RejectReason.DAILY_DIFFICULTY_LIMIT ->
        uiText(R.string.xp_reject_daily_difficulty_limit)

    XpAward.RejectReason.DAILY_STAT_CAP ->
        uiText(R.string.xp_reject_daily_stat_cap)

    XpAward.RejectReason.NOT_ELIGIBLE -> null
}
