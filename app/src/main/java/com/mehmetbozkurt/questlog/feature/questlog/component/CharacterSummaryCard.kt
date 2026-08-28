package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraBar
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import kotlin.math.roundToInt

@Composable
fun CharacterSummaryCard(
    character: CharacterSheet,
    progress: Float,
    streak: StreakInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notes = buildList {
        if (character.pendingFeatChoices > 0) {
            add(
                stringResource(
                    R.string.character_pending_feats,
                    character.pendingFeatChoices
                )
            )
        }
        if (character.epicBoons > 0) {
            add(stringResource(R.string.character_boons, character.epicBoons))
        }
        if (streak != null && streak.graceUsed) {
            add(stringResource(R.string.character_resolute_active))
        }
    }

    GlassPanel(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.character_level_rank,
                        character.level,
                        stringResource(levelRankRes(character.level)),
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Spacing.xs))
                DataValue(
                    text = stringResource(R.string.character_next_rank, character.level + 1),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DataValue(
                text = "${(progress * 100).roundToInt()}%",
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(Spacing.md))

        AuraBar(
            progress = progress,
            color = MaterialTheme.colorScheme.primary,
            height = Spacing.barHeight,
        )

        if (notes.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = notes.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
