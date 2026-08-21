package com.mehmetbozkurt.questlog.feature.crew.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.CrewMember

@Composable
fun CrewMemberRow(
    member: CrewMember,
    isSelf: Boolean,
    modifier: Modifier = Modifier,
) {
    QuestCard(
        seed = member.userId.hashCode(),
        containerColor = if (isSelf) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${member.level}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {
                Text(
                    if (isSelf) "${member.displayName} (sen)" else member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${member.totalXp} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (member.currentStreak > 0) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = "Seri",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "${member.currentStreak}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
