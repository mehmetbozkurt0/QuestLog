package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraCheckbox
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun QuestLogCard(
    log: QuestLog,
    onClick: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statColor = log.statType?.color()
    val accent = statColor ?: MaterialTheme.colorScheme.primary
    val done = log.isCompleted

    GlassPanel(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        edge = if (done) null else accent,
        containerColor = if (done) wellColor() else MaterialTheme.extendedColors.glass,
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraCheckbox(
                checked = done,
                onCheckedChange = { onToggleCompleted(!done) },
                accent = accent,
            )

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                QuestMetaRow(log = log, statColor = statColor)
            }

            if (log.proofLevel != ProofLevel.NONE) {
                Spacer(Modifier.width(Spacing.sm))
                Icon(
                    imageVector = if (log.proofLevel == ProofLevel.PHOTO)
                        Icons.Default.PhotoCamera else Icons.Default.EditNote,
                    contentDescription = stringResource(R.string.log_proof_badge),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }

            if (done && log.difficulty != null && log.isXpEligible) {
                Spacer(Modifier.width(Spacing.sm))
                DataValue(
                    text = "+${log.difficulty.baseXp} XP",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun QuestMetaRow(log: QuestLog, statColor: Color?) {
    val parts = buildList {
        if (log.statType != null) add(stringResource(log.statType.shortLabelRes()))
        if (log.difficulty != null) add(stringResource(log.difficulty.nameRes()))
        if (log.statType == null) add(stringResource(log.type.labelRes()))
    }
    if (parts.isEmpty()) return

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = parts.first().uppercaseLocalized(),
            style = MaterialTheme.typography.labelSmall,
            color = statColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
        parts.drop(1).forEach { part ->
            Text(
                text = "  •  $part",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (log.priority == Priority.HIGH) {
            Text(
                text = "  •  " + stringResource(log.priority.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.priorityHigh,
            )
        }
    }
}

@StringRes
fun LogType.labelRes(): Int = when (this) {
    LogType.QUEST -> R.string.log_type_quest
    LogType.NPC -> R.string.log_type_npc
    LogType.LORE -> R.string.log_type_lore
    LogType.SESSION_NOTE -> R.string.log_type_session_note
}

@StringRes
fun Priority.labelRes(): Int = when (this) {
    Priority.LOW -> R.string.priority_low
    Priority.MEDIUM -> R.string.priority_medium
    Priority.HIGH -> R.string.priority_high
}

@Composable
fun java.time.Instant.formatted(): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    }
    return formatter.format(this.atZone(ZoneId.systemDefault()))
}
