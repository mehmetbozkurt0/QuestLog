package com.mehmetbozkurt.questlog.feature.crew.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.component.StatChip
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.accentWidth
import com.mehmetbozkurt.questlog.domain.model.CrewFeedItem
import java.time.Duration
import java.time.Instant

@Composable
fun FeedEntryCard(
    item: CrewFeedItem,
    isMine: Boolean,
    canApprove: Boolean,
    approvedByMe: Boolean,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statColor = item.statType?.color()

    GlassPanel(
        edge = statColor,
        edgeWidth = item.difficulty.accentWidth(),
        containerColor = wellColor(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isMine) stringResource(R.string.feed_author_self) else item.authorName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Text(
                item.completedAt.relativeLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Text(
            item.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (item.statType != null && statColor != null) {
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip(
                    label = stringResource(item.statType.shortLabelRes()),
                    color = statColor,
                    icon = item.statType.icon(),
                )
                if (item.difficulty != null) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        stringResource(item.difficulty.nameRes()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item.proofPhotoUrl?.let { url ->
            Spacer(Modifier.height(Spacing.sm))
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.proof_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }

        Spacer(Modifier.height(Spacing.xs))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.approvalCount > 0) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    stringResource(R.string.feed_approvals, item.approvalCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.weight(1f))
            when {
                approvedByMe -> Text(
                    stringResource(R.string.feed_approved),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                canApprove -> TextButton(onClick = onApprove) {
                    Text(
                        stringResource(R.string.feed_approve),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun Instant.relativeLabel(): String {
    val minutes = Duration.between(this, Instant.now()).toMinutes()
    return when {
        minutes < 1 -> stringResource(R.string.feed_time_now)
        minutes < 60 -> stringResource(R.string.feed_time_minutes, minutes)
        minutes < 60 * 24 -> stringResource(R.string.feed_time_hours, minutes / 60)
        minutes < 60 * 24 * 7 ->
            stringResource(R.string.feed_time_days, minutes / (60 * 24))
        else -> stringResource(R.string.feed_time_weeks, minutes / (60 * 24 * 7))
    }
}
