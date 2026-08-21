package com.mehmetbozkurt.questlog.feature.character.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.WeeklySummary
import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.progression.StreakInfo
import java.time.format.TextStyle
import java.util.Locale

private val trLocale = Locale("tr")

@Composable
fun WeeklySummaryCard(
    streak: StreakInfo?,
    weekly: WeeklySummary?,
    modifier: Modifier = Modifier,
) {
    QuestCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        seed = 3,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bu Hafta",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                if (streak != null && streak.currentStreak > 0) {
                    val flameColor =
                        if (streak.activeToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Seri",
                        tint = flameColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${streak.currentStreak} gün",
                        style = MaterialTheme.typography.labelLarge,
                        color = flameColor,
                    )
                }
            }

            if (streak != null && streak.longestStreak > 0) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    buildString {
                        append("En uzun seri: ${streak.longestStreak} gün")
                        if (streak.graceUsed) append(" · Kararlı devrede")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (weekly != null) {
                Spacer(Modifier.height(Spacing.md))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    weekly.days.forEach { day ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val frac =
                                if (weekly.maxDayXp > 0) day.xp.toFloat() / weekly.maxDayXp
                                else 0f
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((2 + 46 * frac).dp)
                                    .background(
                                        if (day.xp > 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(2.dp),
                                    )
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                day.date.dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, trLocale)
                                    .take(2),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    buildString {
                        append("${weekly.totalXp} XP · ${weekly.entryCount} görev")
                        weekly.topStat?.let { append(" · En çok: ${it.displayName()}") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
