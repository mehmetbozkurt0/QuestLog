package com.mehmetbozkurt.questlog.feature.crew.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.mehmetbozkurt.questlog.core.designsystem.component.LevelMedallion
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
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
        val levelInfo = remember(member.totalXp) {
            XpCurve.levelFromTotalXp(member.totalXp)
        }
        val fraction = if (levelInfo.xpToNextLevel <= 0) 1f
        else (levelInfo.xpIntoLevel.toFloat() / levelInfo.xpToNextLevel).coerceIn(0f, 1f)

        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelMedallion(
                level = member.level,
                progress = fraction,
                diameter = 44.dp,
            )

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {
                Text(
                    if (isSelf) stringResource(R.string.crew_member_self, member.displayName)
                    else member.displayName,
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
                    contentDescription = stringResource(R.string.character_streak),
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
