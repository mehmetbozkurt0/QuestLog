package com.mehmetbozkurt.questlog.core.common

import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.domain.repository.ApproveFailure
import com.mehmetbozkurt.questlog.domain.repository.CrewFailure
import com.mehmetbozkurt.questlog.domain.repository.CatalogRejection
import com.mehmetbozkurt.questlog.domain.repository.DeleteFailure
import com.mehmetbozkurt.questlog.domain.repository.QuestRejection

fun CrewFailure.toUiText(): UiText = uiText(
    when (this) {
        CrewFailure.NO_SESSION -> R.string.crew_error_no_session
        CrewFailure.NO_CHARACTER -> R.string.crew_error_no_character
        CrewFailure.PERMISSION_DENIED -> R.string.crew_error_permission_denied
        CrewFailure.UNKNOWN -> R.string.crew_error_unknown
    }
)

fun ApproveFailure.toUiText(): UiText = uiText(
    when (this) {
        ApproveFailure.NO_SESSION -> R.string.crew_error_no_session
        ApproveFailure.NO_CHARACTER -> R.string.crew_error_no_character
        ApproveFailure.NOT_IN_CREW -> R.string.crew_error_not_in_crew
        ApproveFailure.ENTRY_NOT_FOUND -> R.string.crew_approve_error_entry_not_found
        ApproveFailure.WRITE_FAILED -> R.string.crew_approve_error_write_failed
    }
)

fun QuestRejection.toUiText(): UiText = uiText(
    when (this) {
        QuestRejection.NO_SESSION -> R.string.pathway_reject_no_session
        QuestRejection.QUEST_NOT_FOUND -> R.string.pathway_reject_quest_not_found
        QuestRejection.NOT_ENROLLED -> R.string.pathway_reject_not_enrolled
        QuestRejection.PATHWAY_INACTIVE -> R.string.pathway_reject_pathway_inactive
        QuestRejection.PATHWAY_NOT_FOUND -> R.string.pathway_reject_pathway_not_found
        QuestRejection.STAGE_LOCKED -> R.string.pathway_reject_stage_locked
        QuestRejection.ALREADY_COMPLETED -> R.string.pathway_reject_already_completed
        QuestRejection.ALREADY_DONE_TODAY -> R.string.pathway_reject_already_done_today
        QuestRejection.XP_NOT_AWARDED -> R.string.pathway_reject_xp_not_awarded
    }
)

fun CatalogRejection.toUiText(): UiText = uiText(
    when (this) {
        CatalogRejection.NO_SESSION -> R.string.pathway_reject_no_session
        CatalogRejection.TASK_NOT_FOUND -> R.string.pathway_reject_quest_not_found
        CatalogRejection.ALREADY_DONE_TODAY -> R.string.catalog_reject_already_done_today
        CatalogRejection.DAILY_LIMIT -> R.string.catalog_reject_daily_limit
        CatalogRejection.XP_NOT_AWARDED -> R.string.pathway_reject_xp_not_awarded
    }
)

fun DeleteFailure.toUiText(): UiText = uiText(
    when (this) {
        DeleteFailure.REAUTH_FAILED -> R.string.account_error_reauth_failed
        DeleteFailure.DELETE_FAILED -> R.string.account_error_delete_failed
    }
)
