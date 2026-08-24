package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.feature.questlog.ActivePathwaySummary

@Composable
fun ActivePathwayCard(
    summary: ActivePathwaySummary,
    onClick: () -> Unit,
) {
    val statColor = summary.pathway.primaryStat.colorHex().toComposeColor()

    QuestCard(
        onClick = onClick,
        accent = statColor,
        seed = summary.pathway.id.hashCode(),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = summary.pathway.primaryStat.icon(),
                    contentDescription = null,
                    tint = statColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    summary.pathway.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(
                        R.string.pathway_quest_count,
                        summary.completedQuests,
                        summary.totalQuests,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = statColor,
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            LinearProgressIndicator(
                progress = { summary.fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = statColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            if (summary.progress.escrowedXp > 0) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.pathway_escrow_short, summary.progress.escrowedXp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}