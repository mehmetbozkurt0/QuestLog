package com.mehmetbozkurt.questlog.core.common

import androidx.annotation.StringRes
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.XpBonus

@StringRes
fun StatType.nameRes(): Int = when (this) {
    StatType.STR -> R.string.stat_str_name
    StatType.DEX -> R.string.stat_dex_name
    StatType.CON -> R.string.stat_con_name
    StatType.INT -> R.string.stat_int_name
    StatType.WIS -> R.string.stat_wis_name
    StatType.CHA -> R.string.stat_cha_name
}

@StringRes
fun StatType.shortLabelRes(): Int = when (this) {
    StatType.STR -> R.string.stat_str_short
    StatType.DEX -> R.string.stat_dex_short
    StatType.CON -> R.string.stat_con_short
    StatType.INT -> R.string.stat_int_short
    StatType.WIS -> R.string.stat_wis_short
    StatType.CHA -> R.string.stat_cha_short
}

@StringRes
fun StatType.descriptionRes(): Int = when (this) {
    StatType.STR -> R.string.stat_str_description
    StatType.DEX -> R.string.stat_dex_description
    StatType.CON -> R.string.stat_con_description
    StatType.INT -> R.string.stat_int_description
    StatType.WIS -> R.string.stat_wis_description
    StatType.CHA -> R.string.stat_cha_description
}

@StringRes
fun Difficulty.nameRes(): Int = when (this) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
    Difficulty.EPIC -> R.string.difficulty_epic
}

@StringRes
fun Difficulty.hintRes(): Int = when (this) {
    Difficulty.EASY -> R.string.difficulty_easy_hint
    Difficulty.MEDIUM -> R.string.difficulty_medium_hint
    Difficulty.HARD -> R.string.difficulty_hard_hint
    Difficulty.EPIC -> R.string.difficulty_epic_hint
}

@StringRes
fun ProofLevel.nameRes(): Int = when (this) {
    ProofLevel.NONE -> R.string.proof_level_none
    ProofLevel.NOTE -> R.string.proof_level_note
    ProofLevel.PHOTO -> R.string.proof_level_photo
}

@StringRes
fun XpBonus.Kind.nameRes(): Int = when (this) {
    XpBonus.Kind.PROOF -> R.string.xp_bonus_proof
    XpBonus.Kind.SPECIALIST -> R.string.xp_bonus_specialist
    XpBonus.Kind.EARLY_RISER -> R.string.xp_bonus_early_riser
    XpBonus.Kind.ENDURING -> R.string.xp_bonus_enduring
    XpBonus.Kind.VERSATILE -> R.string.xp_bonus_versatile
    XpBonus.Kind.NEW_ADVENTURER -> R.string.xp_bonus_new_adventurer
}

fun XpBonus.toUiText(): UiText =
    UiText.Res(R.string.xp_bonus_format, listOf(UiText.Res(kind.nameRes()), percent))

@StringRes
fun FeatId.nameRes(): Int = when (this) {
    FeatId.RESOLUTE -> R.string.feat_resolute_name
    FeatId.SPECIALIST -> R.string.feat_specialist_name
    FeatId.VERSATILE -> R.string.feat_versatile_name
    FeatId.EARLY_RISER -> R.string.feat_early_riser_name
    FeatId.ENDURING -> R.string.feat_enduring_name
    FeatId.MENTOR -> R.string.feat_mentor_name
}

@StringRes
fun FeatId.descriptionRes(): Int = when (this) {
    FeatId.RESOLUTE -> R.string.feat_resolute_description
    FeatId.SPECIALIST -> R.string.feat_specialist_description
    FeatId.VERSATILE -> R.string.feat_versatile_description
    FeatId.EARLY_RISER -> R.string.feat_early_riser_description
    FeatId.ENDURING -> R.string.feat_enduring_description
    FeatId.MENTOR -> R.string.feat_mentor_description
}
