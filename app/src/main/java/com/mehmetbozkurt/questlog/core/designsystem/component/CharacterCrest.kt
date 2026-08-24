package com.mehmetbozkurt.questlog.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.theme.CinzelFamily
import com.mehmetbozkurt.questlog.core.designsystem.theme.GaramondFamily
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.CharacterSheet
import com.mehmetbozkurt.questlog.domain.model.StatProgress
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.model.abilityModifier
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import kotlin.math.cos
import kotlin.math.sin

private const val BOARD_W = 358f
private const val BOARD_H = 342f

private const val HEX_HALF_W = 52f
private const val HEX_HALF_H = 45f

private const val SEAL_CX = 179f
private const val SEAL_CY = 171f
private const val SEAL_ARC_R = 70f
private const val TICK_INNER_R = 58f
private const val TICK_OUTER_R = 64f
private const val SEAL_INNER_R = 52f
private const val SEAL_BEAD_R = 46f

private val CELL_CENTERS = listOf(
    StatType.STR to Offset(179f, 45f),
    StatType.DEX to Offset(288f, 108f),
    StatType.CON to Offset(288f, 234f),
    StatType.INT to Offset(179f, 297f),
    StatType.WIS to Offset(70f, 234f),
    StatType.CHA to Offset(70f, 108f),
)

private class CellText(
    val center: Offset,
    val color: Color,
    val label: String,
    val value: String,
    val detail: String,
    val progress: Float,
)

@Composable
fun CharacterCrest(
    character: CharacterSheet,
    levelProgress: Float,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val beadColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    val maxLabel = stringResource(R.string.character_stat_max)
    val levelCaps = stringResource(R.string.character_level_caps)

    val byType = character.stats.associateBy { it.statType }
    val cells = CELL_CENTERS.mapNotNull { (type, center) ->
        val stat = byType[type] ?: return@mapNotNull null
        CellText(
            center = center,
            color = type.colorHex().toComposeColor(),
            label = stringResource(type.shortLabelRes()) + "  " + stat.signedModifier(),
            value = stat.value.toString(),
            detail = if (stat.value >= XpCurve.MAX_STAT) maxLabel
            else stringResource(R.string.character_stat_progress, stat.currentXp, stat.xpToNext),
            progress = stat.fillFraction(),
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(BOARD_W / BOARD_H)
    ) {
        val s = size.width / BOARD_W

        cells.forEach { cell ->
            val path = hexPath(cell.center, s)
            val minFill = 3f * s
            val fillHeight = (cell.progress * 2f * HEX_HALF_H * s).coerceAtLeast(minFill)

            clipPath(path) {
                drawRect(
                    color = cell.color.copy(alpha = 0.28f),
                    topLeft = Offset(
                        (cell.center.x - HEX_HALF_W) * s,
                        (cell.center.y + HEX_HALF_H) * s - fillHeight,
                    ),
                    size = Size(2f * HEX_HALF_W * s, fillHeight),
                )
            }
            drawPath(path, cell.color, style = Stroke(width = 1.5f * s))

            drawCentered(
                measurer, cell.label, cell.center.x, cell.center.y - 27f, s,
                CinzelFamily.style(10f * s, FontWeight.SemiBold, cell.color, 1.4f * s),
            )
            drawCentered(
                measurer, cell.value, cell.center.x, cell.center.y - 1f, s,
                CinzelFamily.style(30f * s, FontWeight.Bold, onSurface, 0f),
            )
            drawCentered(
                measurer, cell.detail, cell.center.x, cell.center.y + 27f, s,
                GaramondFamily.style(10f * s, FontWeight.Normal, onSurfaceVariant, 0f),
                maxWidthPx = hexWidthAt(27f) * s,
            )
        }

        val sealCenter = Offset(SEAL_CX * s, SEAL_CY * s)

        drawCircle(
            color = track,
            radius = SEAL_ARC_R * s,
            center = sealCenter,
            style = Stroke(width = 3f * s),
        )
        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = 360f * levelProgress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(
                (SEAL_CX - SEAL_ARC_R) * s,
                (SEAL_CY - SEAL_ARC_R) * s,
            ),
            size = Size(2f * SEAL_ARC_R * s, 2f * SEAL_ARC_R * s),
            style = Stroke(width = 3f * s, cap = StrokeCap.Round),
        )

        val lit = character.level.coerceIn(0, XpCurve.MAX_LEVEL)
        repeat(XpCurve.MAX_LEVEL) { i ->
            val angle = Math.toRadians(-90.0 + 360.0 / XpCurve.MAX_LEVEL * i)
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            drawLine(
                color = if (i < lit) primary else outline,
                start = Offset(
                    (SEAL_CX + TICK_INNER_R * dx) * s,
                    (SEAL_CY + TICK_INNER_R * dy) * s,
                ),
                end = Offset(
                    (SEAL_CX + TICK_OUTER_R * dx) * s,
                    (SEAL_CY + TICK_OUTER_R * dy) * s,
                ),
                strokeWidth = 2f * s,
                cap = StrokeCap.Round,
            )
        }

        drawCircle(
            color = primary.copy(alpha = 0.05f),
            radius = SEAL_INNER_R * s,
            center = sealCenter,
        )
        drawCircle(
            color = outline,
            radius = SEAL_INNER_R * s,
            center = sealCenter,
            style = Stroke(width = 1f * s),
        )
        drawCircle(
            color = beadColor,
            radius = SEAL_BEAD_R * s,
            center = sealCenter,
            style = Stroke(
                width = 1f * s,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f * s, 6f * s)),
            ),
        )

        drawCentered(
            measurer, levelCaps, SEAL_CX, SEAL_CY - 22f, s,
            CinzelFamily.style(9f * s, FontWeight.SemiBold, onSurfaceVariant, 2f * s),
        )
        drawCentered(
            measurer, character.level.toString(), SEAL_CX, SEAL_CY + 12f, s,
            CinzelFamily.style(44f * s, FontWeight.Bold, primary, 0f),
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 72.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val bead = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .size(diameter)
            .drawBehind { drawProgressRing(progress, primary, track, bead) },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun SealFrame(
    modifier: Modifier = Modifier,
    diameter: Dp = 96.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .size(diameter)
            .drawBehind {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(primary.copy(alpha = 0.06f), radius = r, center = center)
                drawCircle(
                    color = outline,
                    radius = r - 1.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )
                drawCircle(
                    color = primary.copy(alpha = 0.7f),
                    radius = r - 7.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(2.dp.toPx(), 6.dp.toPx())
                        ),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun LevelMedallion(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 64.dp,
) {
    val measurer = rememberTextMeasurer()
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val bead = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

    Canvas(modifier = modifier.size(diameter)) {
        drawProgressRing(progress, primary, track, bead)

        val r = size.minDimension / 2f
        val layout = measurer.measure(
            level.toString(),
            TextStyle(
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (r * 0.78f).toSp(),
                color = primary,
                textAlign = TextAlign.Center,
            ),
        )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(r - layout.size.width / 2f, r - layout.size.height / 2f),
        )
    }
}

private fun DrawScope.drawProgressRing(
    progress: Float,
    primary: Color,
    track: Color,
    bead: Color,
) {
    val r = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val stroke = r * 0.09f
    val ringR = r - stroke / 2f

    drawCircle(track, radius = ringR, center = center, style = Stroke(width = stroke))
    drawArc(
        color = primary,
        startAngle = -90f,
        sweepAngle = 360f * progress.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = Offset(center.x - ringR, center.y - ringR),
        size = Size(2f * ringR, 2f * ringR),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
    drawCircle(
        color = bead,
        radius = ringR - stroke * 1.6f,
        center = center,
        style = Stroke(
            width = r * 0.025f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.05f, r * 0.15f)),
        ),
    )
}

private fun StatProgress.signedModifier(): String {
    val mod = abilityModifier()
    return if (mod >= 0) "+$mod" else "$mod"
}

private fun StatProgress.fillFraction(): Float =
    if (value >= XpCurve.MAX_STAT) 1f
    else if (xpToNext <= 0) 0f
    else (currentXp.toFloat() / xpToNext).coerceIn(0f, 1f)

private fun hexWidthAt(dy: Float): Float {
    val half = HEX_HALF_W - (kotlin.math.abs(dy) / HEX_HALF_H) * (HEX_HALF_W / 2f)
    return (2f * half).coerceAtLeast(0f)
}

private fun DrawScope.hexPath(center: Offset, s: Float): Path {
    val cx = center.x * s
    val cy = center.y * s
    val hw = HEX_HALF_W * s
    val hh = HEX_HALF_H * s
    return Path().apply {
        moveTo(cx - hw, cy)
        lineTo(cx - hw / 2f, cy - hh)
        lineTo(cx + hw / 2f, cy - hh)
        lineTo(cx + hw, cy)
        lineTo(cx + hw / 2f, cy + hh)
        lineTo(cx - hw / 2f, cy + hh)
        close()
    }
}

private fun androidx.compose.ui.text.font.FontFamily.style(
    sizePx: Float,
    weight: FontWeight,
    color: Color,
    letterSpacingPx: Float,
): TextStyleSpec = TextStyleSpec(this, sizePx, weight, color, letterSpacingPx)

private class TextStyleSpec(
    val family: androidx.compose.ui.text.font.FontFamily,
    val sizePx: Float,
    val weight: FontWeight,
    val color: Color,
    val letterSpacingPx: Float,
)

private fun DrawScope.drawCentered(
    measurer: TextMeasurer,
    text: String,
    boardX: Float,
    boardY: Float,
    s: Float,
    spec: TextStyleSpec,
    maxWidthPx: Float = Float.MAX_VALUE,
) {
    fun styleAt(sizePx: Float) = TextStyle(
        fontFamily = spec.family,
        fontWeight = spec.weight,
        fontSize = sizePx.toSp(),
        letterSpacing = spec.letterSpacingPx.toSp(),
        color = spec.color,
        textAlign = TextAlign.Center,
    )

    var style = styleAt(spec.sizePx)
    var layout = measurer.measure(text, style)
    if (layout.size.width > maxWidthPx) {
        val shrunk = (spec.sizePx * maxWidthPx / layout.size.width).coerceAtLeast(6f * s)
        style = styleAt(shrunk)
        layout = measurer.measure(text, style)
    }
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            boardX * s - layout.size.width / 2f,
            boardY * s - layout.size.height / 2f,
        ),
    )
}
