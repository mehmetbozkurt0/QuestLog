package com.mehmetbozkurt.questlog.feature.pathway.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraBar
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.domain.model.StatType

@Composable
fun RowScope.PathwayGridCard(
    title: String,
    stat: StatType,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    accented: Boolean = false,
    dimmed: Boolean = false,
    struck: Boolean = false,
    badge: ImageVector? = null,
) {
    val color = stat.color()

    Box(modifier.weight(1f)) {
        GlassPanel(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (dimmed) 0.4f else 1f),
            accent = if (accented) color else null,
            containerColor = if (accented) MaterialTheme.extendedColors.glass else wellColor(),
            contentPadding = PaddingValues(Spacing.lg),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                IconTile(icon = stat.icon(), color = color, size = 36.dp, iconSize = 18.dp)
                Spacer(Modifier.weight(1f))
                if (badge != null) {
                    Icon(
                        badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (struck || dimmed) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (struck) TextDecoration.LineThrough else null,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Spacing.sm))

            DataValue(
                text = caption,
                color = if (accented) color else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.sm))

            AuraBar(
                progress = progress ?: 0f,
                color = color,
                height = 6.dp,
            )
        }
    }
}
