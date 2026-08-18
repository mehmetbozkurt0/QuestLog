package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.QuestLog
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.model.shortLabel
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

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (log.isCompleted) 0.55f else 1f),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statColor ?: Color.Transparent)
            )
            Column(Modifier.padding(Spacing.md)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.statType != null && statColor != null) {
                        Box(Modifier.size(8.dp).background(statColor, CircleShape))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = log.statType.shortLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            color = statColor,
                        )
                        if (log.difficulty != null) {
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = "· ${log.difficulty.displayName()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (priorityColor != null) {
                        Text(
                            text = log.priority.label(),
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
                        var wasCompleted by remember { mutableStateOf(log.isCompleted) }
                        LaunchedEffect(log.isCompleted) {
                            if (log.isCompleted && !wasCompleted) {
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
                        text = "Son tarih: ${log.dueAt.formatted()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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

fun LogType.label(): String = when (this) {
    LogType.QUEST -> "GÖREV"
    LogType.NPC -> "NPC"
    LogType.LORE -> "LORE"
    LogType.SESSION_NOTE -> "OTURUM"
}

@Composable
private fun Priority.color(): Color = when (this) {
    Priority.LOW -> MaterialTheme.extendedColors.priorityLow
    Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
    Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
}

fun Priority.label(): String = when (this) {
    Priority.LOW -> "Düşük"
    Priority.MEDIUM -> "Orta"
    Priority.HIGH -> "Yüksek"
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))

fun java.time.Instant.formatted(): String =
    dateFormatter.format(this.atZone(ZoneId.systemDefault()))
