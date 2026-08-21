package com.mehmetbozkurt.questlog.feature.crew.component

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.pips
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.CrewFeedItem
import com.mehmetbozkurt.questlog.domain.model.colorHex
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
    val statColor = item.statType?.colorHex()?.toComposeColor()

    QuestCard(
        accent = statColor,
        seed = item.id.hashCode(),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.statType != null && statColor != null) {
                Icon(
                    imageVector = item.statType.icon(),
                    contentDescription = null,
                    tint = statColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
            }
            Text(
                if (isMine) "Sen" else item.authorName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.difficulty != null) {
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    item.difficulty.pips(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                item.completedAt.relativeLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Spacing.xs))

        Text(
            item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        item.proofPhotoUrl?.let { url ->
            Spacer(Modifier.height(Spacing.sm))
            AsyncImage(
                model = url,
                contentDescription = "Kanıt fotoğrafı",
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
                    "${item.approvalCount} onay",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.weight(1f))
            when {
                approvedByMe -> Text(
                    "Onayladın",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                canApprove -> TextButton(onClick = onApprove) {
                    Text("Onayla", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun Instant.relativeLabel(): String {
    val minutes = Duration.between(this, Instant.now()).toMinutes()
    return when {
        minutes < 1 -> "az önce"
        minutes < 60 -> "$minutes dk önce"
        minutes < 60 * 24 -> "${minutes / 60} sa önce"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} gün önce"
        else -> "${minutes / (60 * 24 * 7)} hafta önce"
    }
}
