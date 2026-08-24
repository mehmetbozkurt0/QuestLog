package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.pips
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.domain.model.colorHex
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun QuestLogCard(
    log: QuestLog,
    onClick: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statColor = log.statType?.colorHex()?.toComposeColor()
    val priorityColor = log.priority?.color()

    QuestCard(
        onClick = onClick,
        accent = statColor,
        seed = log.id.hashCode(),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (log.isCompleted) 0.55f else 1f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (log.statType != null && statColor != null) {
                Icon(
                    imageVector = log.statType.icon(),
                    contentDescription = null,
                    tint = statColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(log.statType.shortLabelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = statColor,
                )
                if (log.difficulty != null) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = log.difficulty.pips(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (log.proofLevel != ProofLevel.NONE) {
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        imageVector = if (log.proofLevel == ProofLevel.PHOTO)
                            Icons.Default.PhotoCamera else Icons.Default.EditNote,
                        contentDescription = stringResource(R.string.log_proof_badge),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (priorityColor != null) {
                Text(
                    text = stringResource(log.priority.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor,
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (log.isCompleted)
                        TextDecoration.LineThrough else null,
                )
                if (log.descriptionFirstLine.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = log.descriptionFirstLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (log.type == LogType.QUEST) {
                val scale = remember { Animatable(1f) }
                val haptics = LocalHapticFeedback.current
                var wasCompleted by remember { mutableStateOf(log.isCompleted) }
                LaunchedEffect(log.isCompleted) {
                    if (log.isCompleted && !wasCompleted) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        scale.snapTo(1.35f)
                        scale.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        )
                    }
                    wasCompleted = log.isCompleted
                }
                Checkbox(
                    checked = log.isCompleted,
                    onCheckedChange = onToggleCompleted,
                    modifier = Modifier.scale(scale.value),
                )
            }
        }

        if (log.dueAt != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(R.string.log_due_date, log.dueAt.formatted()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LogType.color(): Color = when (this) {
    LogType.QUEST -> MaterialTheme.extendedColors.typeQuest
    LogType.NPC -> MaterialTheme.extendedColors.typeNpc
    LogType.LORE -> MaterialTheme.extendedColors.typeLore
    LogType.SESSION_NOTE -> MaterialTheme.extendedColors.typeSession
}

@StringRes
fun LogType.labelRes(): Int = when (this) {
    LogType.QUEST -> R.string.log_type_quest
    LogType.NPC -> R.string.log_type_npc
    LogType.LORE -> R.string.log_type_lore
    LogType.SESSION_NOTE -> R.string.log_type_session_note
}

@Composable
private fun Priority.color(): Color = when (this) {
    Priority.LOW -> MaterialTheme.extendedColors.priorityLow
    Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
    Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
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
