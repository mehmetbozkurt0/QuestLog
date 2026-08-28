package com.mehmetbozkurt.questlog.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized

@Composable
fun glassColor(): Color = MaterialTheme.extendedColors.glass

@Composable
fun rimColor(): Color = MaterialTheme.extendedColors.rim

@Composable
fun wellColor(): Color = MaterialTheme.extendedColors.well

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    accent: Color? = null,
    edge: Color? = null,
    edgeWidth: Dp = 4.dp,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = glassColor(),
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.card),
    content: @Composable ColumnScope.() -> Unit,
) {
    val stroke = borderColor ?: accent?.copy(alpha = 0.55f) ?: rimColor()
    val clickModifier = if (onClick != null) {
        Modifier.clickable(enabled = enabled, onClick = onClick)
    } else {
        Modifier
    }

    val edgeModifier = if (edge != null) {
        Modifier.drawBehind {
            drawRect(color = edge, size = Size(edgeWidth.toPx(), size.height))
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, stroke),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(clickModifier)
                .then(edgeModifier)
                .padding(start = if (edge != null) edgeWidth else 0.dp)
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun AuraBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = wellColor(),
    trackBorder: Color = rimColor(),
    animated: Boolean = true,
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedValue by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "auraBar",
    )
    val value = if (animated) animatedValue else target
    val fill = remember(color) {
        Brush.horizontalGradient(listOf(color.copy(alpha = 0.75f), color))
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
            .border(1.dp, trackBorder, RoundedCornerShape(height / 2))
            .drawBehind {
                val w = size.width * value
                if (w <= 0f) return@drawBehind
                drawRoundRect(
                    brush = fill,
                    size = Size(w, size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(w - size.height, 0f),
                    size = Size(size.height, size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            }
    )
}

@Composable
fun StatChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.30f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = label.uppercaseLocalized(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun OutlineChip(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, rimColor(), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    filled: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = if (filled) 0.20f else 0.10f))
            .border(1.dp, color.copy(alpha = 0.30f), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercaseLocalized(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = text.uppercaseLocalized(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Spacer(Modifier.width(Spacing.sm))
                trailing()
            }
        }
        Rule()
    }
}

@Composable
fun Rule(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Composable
fun AuraCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale",
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(24.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (checked) accent.copy(alpha = 0.20f) else Color.Transparent)
            .border(
                2.dp,
                if (checked) accent else MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.medium,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onCheckedChange,
            )
            .drawBehind {
                if (scale <= 0.01f) return@drawBehind
                val s = size.minDimension
                val inset = s * 0.26f
                val stroke = s * 0.13f
                val p1 = Offset(inset, s * 0.52f)
                val p2 = Offset(s * 0.43f, s - inset)
                val p3 = Offset(s - inset, inset * 1.15f)
                drawLine(accent, p1, lerp(p1, p2, scale), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                if (scale > 0.5f) {
                    val t = (scale - 0.5f) / 0.5f
                    drawLine(accent, p2, lerp(p2, p3, t), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            }
    )
}

private fun lerp(a: Offset, b: Offset, t: Float) =
    Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

@Composable
fun DataValue(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
        maxLines = 1,
    )
}

@Composable
fun LabeledBarRow(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow(label)
            DataValue(value, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(Spacing.sm))
        AuraBar(progress = progress, color = color, height = height)
    }
}

fun <T> LazyListScope.gridItems(
    items: List<T>,
    columns: Int = 2,
    spacing: Dp = Spacing.md,
    key: ((T) -> Any)? = null,
    itemContent: @Composable RowScope.(T) -> Unit,
) {
    val rows = items.chunked(columns)
    rows.forEachIndexed { rowIndex, row ->
        item(key = key?.let { k -> "grid_${k(row.first())}" } ?: "grid_row_$rowIndex") {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                row.forEach { entry -> itemContent(entry) }
                repeat(columns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
