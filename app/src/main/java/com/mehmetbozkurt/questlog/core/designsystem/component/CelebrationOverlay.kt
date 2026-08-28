package com.mehmetbozkurt.questlog.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.CelebrationTier
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.theme.statColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.toUiText
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CelebrationHost(
    celebration: Celebration?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(celebration) {
        if (celebration != null) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    when (celebration?.tier) {
        null -> Unit
        CelebrationTier.EPIC -> EpicCelebration(celebration, onDismiss, modifier)
        else -> CelebrationBanner(celebration, onDismiss, modifier)
    }
}

@Composable
private fun CelebrationBanner(
    celebration: Celebration,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember(celebration) { mutableStateOf(false) }
    LaunchedEffect(celebration) {
        visible = true
        delay(if (celebration.tier == CelebrationTier.MAJOR) 3200L else 2200L)
        visible = false
        delay(300)
        onDismiss()
    }
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { -it } + fadeIn() + scaleIn(initialScale = 0.85f),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            BannerContent(celebration)
        }
    }
}

@Composable
private fun BannerContent(celebration: Celebration) {
    val statColor = celebration.statType.color()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, statColor.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(statColor, CircleShape))
                Spacer(Modifier.width(Spacing.sm))
                XpCounterText(
                    target = celebration.xpGained,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(celebration.statType.nameRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (celebration.escrowedXp > 0) {
                Text(
                    stringResource(R.string.celebration_escrowed, celebration.escrowedXp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (celebration.statIncreased) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(
                        R.string.celebration_stat_up,
                        stringResource(celebration.statType.nameRes()),
                        celebration.newStatValue,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = statColor,
                )
            }
            if (celebration.stageCompleted) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.celebration_stage_done),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (celebration.featChoicesGained > 0) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.celebration_new_feat),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            if (celebration.streakMilestone != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.celebration_streak, celebration.streakMilestone),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (celebration.bonuses.isNotEmpty()) {
                val context = LocalContext.current
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    celebration.bonuses.joinToString(" · ") { it.toUiText().resolve(context) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun XpCounterText(target: Int, style: TextStyle, color: androidx.compose.ui.graphics.Color) {
    var started by remember(target) { mutableStateOf(false) }
    val value by animateIntAsState(
        targetValue = if (started) target else 0,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "xpCounter",
    )
    LaunchedEffect(target) { started = true }
    Text(stringResource(R.string.celebration_xp, value), style = style, color = color)
}

private class Spark(val angle: Float, val speed: Float, val size: Float)

@Composable
private fun EpicCelebration(
    celebration: Celebration,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val night = MaterialTheme.extendedColors.celebration
    val scrim = night.bg
    val accent = night.gold
    val bright = night.text
    val muted = night.textDim
    val statColor = night.statColor(celebration.statType)

    val rayAngle by rememberInfiniteTransition(label = "rays").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "rayAngle",
    )
    val sparks = remember(celebration) {
        List(28) {
            Spark(
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                speed = 250f + Random.nextFloat() * 550f,
                size = 4f + Random.nextFloat() * 9f,
            )
        }
    }
    val burst = remember(celebration) { Animatable(0f) }
    LaunchedEffect(celebration) {
        burst.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
        delay(2500)
        onDismiss()
    }

    Box(
        modifier
            .fillMaxSize()
            .background(scrim.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val rayBrush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, accent.copy(alpha = 0.30f)),
                startY = 0f,
                endY = center.y,
            )
            repeat(16) { i ->
                rotate(rayAngle + i * 22.5f) {
                    drawRect(
                        brush = rayBrush,
                        topLeft = Offset(center.x - 30f, 0f),
                        size = Size(60f, center.y),
                    )
                }
            }
            val p = burst.value
            sparks.forEach { s ->
                val dist = s.speed * p
                drawCircle(
                    color = accent.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                    radius = s.size * (1f - p * 0.4f),
                    center = center + Offset(cos(s.angle) * dist, sin(s.angle) * dist),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (celebration.leveledUp) {
                Text(
                    stringResource(R.string.celebration_level_up),
                    style = MaterialTheme.typography.displayLarge,
                    color = accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.xl),
                )
                Spacer(Modifier.height(Spacing.xl))
                Box(
                    Modifier
                        .size(132.dp)
                        .background(scrim.copy(alpha = 0.9f), CircleShape)
                        .border(2.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.character_level_caps),
                            style = MaterialTheme.typography.labelSmall,
                            color = muted,
                        )
                        Text(
                            "${celebration.newLevel}",
                            style = MaterialTheme.typography.displayLarge,
                            color = bright,
                        )
                    }
                }
            } else {
                Text(
                    stringResource(R.string.celebration_pathway),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                )
                Text(
                    stringResource(R.string.celebration_pathway_done),
                    style = MaterialTheme.typography.displayMedium,
                    color = bright,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                stringResource(
                    R.string.celebration_xp_stat,
                    celebration.xpGained,
                    stringResource(celebration.statType.nameRes()),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = bright,
            )
            if (celebration.completionBonusXp > 0) {
                Text(
                    stringResource(
                        R.string.celebration_escrow_bonus,
                        celebration.completionBonusXp,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
            if (celebration.leveledUp && celebration.pathwayCompleted) {
                Text(
                    stringResource(R.string.celebration_pathway_too),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
            if (celebration.statIncreased) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    Modifier
                        .background(statColor.copy(alpha = 0.12f), MaterialTheme.shapes.large)
                        .border(1.dp, statColor.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        celebration.statType.icon(),
                        contentDescription = null,
                        tint = statColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column {
                        Text(
                            stringResource(celebration.statType.nameRes()).uppercaseLocalized(),
                            style = MaterialTheme.typography.labelSmall,
                            color = bright,
                        )
                        Text(
                            "${celebration.newStatValue}",
                            style = MaterialTheme.typography.labelMedium,
                            color = statColor,
                        )
                    }
                }
            }
            if (celebration.featChoicesGained > 0) {
                Text(
                    stringResource(R.string.celebration_new_feat_long),
                    style = MaterialTheme.typography.titleMedium,
                    color = bright,
                )
            }
            if (celebration.streakMilestone != null) {
                Text(
                    stringResource(R.string.celebration_streak, celebration.streakMilestone),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
            Spacer(Modifier.height(Spacing.xl))
            Text(
                stringResource(R.string.celebration_tap),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
        }
    }
}




























