package com.mehmetbozkurt.questlog.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import kotlin.random.Random

fun Modifier.parchmentGrain(color: Color, seed: Int): Modifier = drawWithCache {
    val random = Random(seed)
    val specks = List(56) {
        Triple(
            random.nextFloat() * size.width,
            random.nextFloat() * size.height,
            0.5f + random.nextFloat() * 1.6f,
        )
    }
    onDrawBehind {
        specks.forEach { (x, y, r) ->
            drawCircle(color = color, radius = r, center = Offset(x, y))
        }
    }
}

@Composable
fun QuestCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    accent: Color? = null,
    accentWidth: Dp = 4.dp,
    seed: Int = 0,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(Spacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    val grain = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    if (onClick != null) {
        Card(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = colors,
            border = border,
            modifier = modifier,
        ) {
            QuestCardBody(accent, accentWidth, grain, seed, contentPadding, content)
        }
    } else {
        Card(
            shape = shape,
            colors = colors,
            border = border,
            modifier = modifier,
        ) {
            QuestCardBody(accent, accentWidth, grain, seed, contentPadding, content)
        }
    }
}

@Composable
private fun QuestCardBody(
    accent: Color?,
    accentWidth: Dp,
    grain: Color,
    seed: Int,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .parchmentGrain(grain, seed)
    ) {
        if (accent != null) {
            Box(
                Modifier
                    .width(accentWidth)
                    .fillMaxHeight()
                    .background(accent)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun SectionRule(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    )
}
