package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.feature.questlog.ActivePathwaySummary

@Composable
fun ActivePathwayCard(
    summary: ActivePathwaySummary,
    onClick: () -> Unit,
) {
    val statColor = summary.pathway.primaryStat.colorHex().toComposeColor()

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(statColor, CircleShape))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    summary.pathway.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${summary.completedQuests}/${summary.totalQuests}",
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
                    "${summary.progress.escrowedXp} XP emanette",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}