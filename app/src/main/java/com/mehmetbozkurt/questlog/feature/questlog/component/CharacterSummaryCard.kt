package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.designsystem.component.LevelMedallion
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import com.mehmetbozkurt.questlog.domain.progression.XpCurve

@Composable
fun CharacterSummaryCard(
    character: CharacterSheet,
    progress: Float,
    streak: StreakInfo?,
    onClick: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "levelProgress",
    )

    QuestCard(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        seed = 5,
        contentPadding = PaddingValues(Spacing.md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            LevelMedallion(
                level = character.level,
                progress = animatedProgress,
                diameter = 68.dp,
            )

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(levelRankRes(character.level)),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (character.epicBoons > 0) {
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            stringResource(R.string.character_boons, character.epicBoons),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (streak != null && streak.currentStreak > 0) {
                        Spacer(Modifier.weight(1f))
                        val flameColor =
                            if (streak.activeToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(R.string.character_streak),
                            tint = flameColor,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            "${streak.currentStreak}",
                            style = MaterialTheme.typography.labelLarge,
                            color = flameColor,
                        )
                        if (streak.graceUsed) {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Shield,
                                contentDescription =
                                    stringResource(R.string.character_resolute_active),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    if (character.level >= XpCurve.MAX_LEVEL)
                        stringResource(
                            R.string.character_stat_progress,
                            character.xpIntoLevel,
                            XpCurve.XP_PER_EPIC_BOON,
                        )
                    else
                        stringResource(
                            R.string.character_xp_progress,
                            character.xpIntoLevel,
                            character.xpToNextLevel,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (character.pendingFeatChoices > 0) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        stringResource(
                            R.string.character_pending_feats,
                            character.pendingFeatChoices,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
