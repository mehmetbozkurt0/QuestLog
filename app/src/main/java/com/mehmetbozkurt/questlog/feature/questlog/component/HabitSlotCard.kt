package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.accentWidth
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.pips
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.HabitSlot

@Composable
fun HabitSlotCard(
    slot: HabitSlot,
    onClick: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val quest = slot.quest

    if (quest == null) {
        QuestCard(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            seed = slot.index * 31,
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.questlog_habit_add),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.questlog_habit_slot_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }

    val statColor = quest.statType?.color()

    QuestCard(
        onClick = onClick,
        accent = statColor,
        accentWidth = quest.difficulty.accentWidth(),
        seed = quest.id.hashCode(),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (slot.burnedToday) 0.55f else 1f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (quest.statType != null && statColor != null) {
                Icon(
                    imageVector = quest.statType.icon(),
                    contentDescription = null,
                    tint = statColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(quest.statType.shortLabelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = statColor,
                )
            }
            if (quest.difficulty != null) {
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = quest.difficulty.pips(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (quest.difficulty >= Difficulty.HARD)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.questlog_habit_clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (slot.burnedToday) {
                    Text(
                        text = stringResource(R.string.questlog_habit_done_today),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Checkbox(
                checked = slot.burnedToday,
                onCheckedChange = onToggleCompleted,
            )
        }
    }
}
