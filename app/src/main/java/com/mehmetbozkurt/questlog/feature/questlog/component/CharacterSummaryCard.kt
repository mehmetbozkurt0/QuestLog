package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.progression.XpCurve

@Composable
fun CharacterSummaryCard(
    character: CharacterSheet,
    progress: Float,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round,
            )

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