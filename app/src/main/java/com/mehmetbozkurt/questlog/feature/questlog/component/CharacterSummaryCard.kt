package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
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
    QuestCard(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        seed = 5,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Seviye ${character.level}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (character.epicBoons > 0) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "· ${character.epicBoons} Kutsama",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (streak != null && streak.currentStreak > 0) {
                    Spacer(Modifier.width(Spacing.sm))
                    val flameColor =
                        if (streak.activeToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Seri",
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
                            contentDescription = "Kararlı devrede",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (character.level >= XpCurve.MAX_LEVEL)
                        "${character.xpIntoLevel} / ${XpCurve.XP_PER_EPIC_BOON}"
                    else
                        "${character.xpIntoLevel} / ${character.xpToNextLevel} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(900, easing = FastOutSlowInEasing),
                label = "levelProgress",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary,
                                )
                            )
                        )
                )
            }

            if (character.pendingFeatChoices > 0) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "${character.pendingFeatChoices} yetenek hakkın bekliyor",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}