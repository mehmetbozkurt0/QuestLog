package com.mehmetbozkurt.questlog.feature.crew.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.designsystem.component.LevelMedallion
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.domain.model.CrewMember
import com.mehmetbozkurt.questlog.domain.progression.XpCurve

@Composable
fun CrewMemberRow(
    member: CrewMember,
    rank: Int,
    isSelf: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rankColor = when (rank) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> MaterialTheme.colorScheme.onSurface
        3 -> MaterialTheme.extendedColors.statCon
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassPanel(
        onClick = onClick,
        edge = if (isSelf) MaterialTheme.colorScheme.primary else rankColor,
        edgeWidth = if (isSelf) 4.dp else 2.dp,
        containerColor = if (isSelf) MaterialTheme.extendedColors.glass else wellColor(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(
            start = Spacing.md,
            end = Spacing.lg,
            top = Spacing.md,
            bottom = Spacing.md,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        val levelInfo = remember(member.totalXp) {
            XpCurve.levelFromTotalXp(member.totalXp)
        }
        val fraction = if (levelInfo.xpToNextLevel <= 0) 1f
        else (levelInfo.xpIntoLevel.toFloat() / levelInfo.xpToNextLevel).coerceIn(0f, 1f)

        Row(verticalAlignment = Alignment.CenterVertically) {
            DataValue(
                text = "#$rank",
                color = rankColor,
                modifier = Modifier.widthIn(min = 28.dp),
            )

            Spacer(Modifier.width(Spacing.sm))

            LevelMedallion(
                level = member.level,
                progress = fraction,
                diameter = 40.dp,
            )

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isSelf)
                        stringResource(R.string.crew_member_self, member.displayName)
                    else member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.character_level_rank,
                        member.level,
                        stringResource(levelRankRes(member.level)),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(Spacing.sm))

            Column(horizontalAlignment = Alignment.End) {
                DataValue(
                    text = stringResource(R.string.crew_member_xp, member.totalXp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (member.currentStreak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(R.string.character_streak),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${member.currentStreak}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
