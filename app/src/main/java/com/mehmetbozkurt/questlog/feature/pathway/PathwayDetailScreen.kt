package com.mehmetbozkurt.questlog.feature.pathway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.localizedDescription
import com.mehmetbozkurt.questlog.core.common.localizedTitle
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.accentWidth
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraBar
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.Rule
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellBackBar
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.component.StatChip
import com.mehmetbozkurt.questlog.core.designsystem.theme.ContentHero
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.domain.model.PathwayQuestProgress
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun PathwayDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: PathwayDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var celebration by remember { mutableStateOf<Celebration?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PathwayDetailEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))
                is PathwayDetailEffect.ShowCelebration ->
                    celebration = effect.celebration
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        PathwayDetailScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
        )
        CelebrationHost(
            celebration = celebration,
            onDismiss = { celebration = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathwayDetailScreen(
    state: PathwayDetailState,
    onEvent: (PathwayDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val detail = state.detail

    Scaffold(
        topBar = {
            ShellBackBar(
                title = stringResource(R.string.pathway_detail_header),
                onBack = onNavigateBack,
            )
        },
        bottomBar = {
            if (detail != null && !state.isCompleted) {
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    Rule()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.screen),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.isActive) {
                            OutlinedButton(
                                shape = MaterialTheme.shapes.large,
                                onClick = {
                                    onEvent(PathwayDetailEvent.AbandonDialogToggled(true))
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                            ) {
                                Text(
                                    stringResource(R.string.pathway_detail_abandon),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        } else {
                            Button(
                                shape = MaterialTheme.shapes.large,
                                onClick = { onEvent(PathwayDetailEvent.StartClicked) },
                                enabled = !state.isWorking && state.canStartMore,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                            ) {
                                if (state.isWorking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                } else {
                                    Text(
                                        stringResource(R.string.pathway_detail_start),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            detail == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.pathway_detail_not_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                val statColor = detail.pathway.primaryStat.color()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = Spacing.screen)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(Spacing.lg))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatChip(
                            label = stringResource(detail.pathway.primaryStat.shortLabelRes()),
                            color = statColor,
                        )
                        detail.pathway.secondaryStat?.let { secondary ->
                            Spacer(Modifier.width(Spacing.sm))
                            StatChip(
                                label = stringResource(secondary.shortLabelRes()),
                                color = secondary.color(),
                            )
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = stringResource(
                                R.string.pathway_detail_stage,
                                detail.pathway.tier,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Text(
                        detail.pathway.localizedTitle(),
                        style = ContentHero,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        detail.pathway.localizedDescription(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (state.isActive || state.isCompleted) {
                        Spacer(Modifier.height(Spacing.lg))
                        GlassPanel(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.pathway_detail_overall),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.weight(1f))
                                DataValue(
                                    text = "${(detail.progressFraction * 100).roundToInt()}%",
                                    color = statColor,
                                )
                            }
                            Spacer(Modifier.height(Spacing.md))
                            AuraBar(
                                progress = detail.progressFraction,
                                color = statColor,
                                height = Spacing.barHeight,
                            )
                            Spacer(Modifier.height(Spacing.sm))
                            DataValue(
                                text = stringResource(
                                    R.string.pathway_detail_progress,
                                    detail.completedQuests,
                                    detail.totalQuests,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    GlassPanel(
                        accent = if (state.isActive) MaterialTheme.colorScheme.primary
                        else null,
                        containerColor = wellColor(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(
                                icon = Icons.Default.WorkspacePremium,
                                color = MaterialTheme.colorScheme.primary,
                                size = 40.dp,
                            )
                            Spacer(Modifier.width(Spacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.pathway_reward_label)
                                        .uppercaseLocalized(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    stringResource(
                                        R.string.pathway_reward_value,
                                        detail.pathway.completionBonusXp,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (state.isActive) {
                                    DataValue(
                                        text = stringResource(
                                            R.string.pathway_escrow_short,
                                            detail.progress?.escrowedXp ?: 0,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (state.isCompleted) {
                        Spacer(Modifier.height(Spacing.md))
                        Text(
                            stringResource(R.string.pathway_detail_done),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    if (!state.isActive && !state.isCompleted && !state.canStartMore) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            stringResource(R.string.pathway_detail_limit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    SectionTitle(
                        text = stringResource(R.string.pathway_detail_stages),
                        icon = Icons.Default.Timeline,
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    val stageEntries = detail.stages.entries.toList()
                    stageEntries.forEachIndexed { index, entry ->
                        val stage = entry.key
                        val quests = entry.value
                        val unlocked = detail.isStageUnlocked(stage)
                        val stageComplete = quests.isNotEmpty() && quests.all { it.isComplete }
                        val nextUnlocked = stageEntries.getOrNull(index + 1)
                            ?.let { detail.isStageUnlocked(it.key) } == true

                        StageNode(
                            stage = stage,
                            unlocked = unlocked,
                            complete = stageComplete,
                            spineLit = stageComplete || nextUnlocked,
                            isLast = index == stageEntries.lastIndex,
                            accent = statColor,
                        ) {
                            quests.forEach { qp ->
                                QuestRow(
                                    questProgress = qp,
                                    statColor = qp.quest.statType.color(),
                                    unlocked = unlocked,
                                    enabled = state.isActive && !state.isWorking,
                                    onClick = {
                                        onEvent(PathwayDetailEvent.QuestClicked(qp.quest.id))
                                    },
                                )
                                Spacer(Modifier.height(Spacing.sm))
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        }
    }

    if (state.showAbandonDialog) {
        val escrow = state.detail?.progress?.escrowedXp ?: 0
        AlertDialog(
            onDismissRequest = { onEvent(PathwayDetailEvent.AbandonDialogToggled(false)) },
            title = { Text(stringResource(R.string.pathway_detail_abandon_title)) },
            text = { Text(stringResource(R.string.pathway_detail_abandon_body, escrow)) },
            confirmButton = {
                TextButton(onClick = { onEvent(PathwayDetailEvent.AbandonConfirmed) }) {
                    Text(
                        stringResource(R.string.pathway_detail_abandon),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PathwayDetailEvent.AbandonDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun QuestRow(
    questProgress: PathwayQuestProgress,
    statColor: Color,
    unlocked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val quest = questProgress.quest
    val alpha = if (unlocked) 1f else 0.4f
    val clickable = unlocked && enabled && !questProgress.isComplete

    GlassPanel(
        onClick = if (clickable) onClick else null,
        edge = statColor.copy(alpha = alpha),
        edgeWidth = quest.difficulty.accentWidth(),
        containerColor = wellColor(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    quest.localizedTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    textDecoration = if (questProgress.isComplete)
                        TextDecoration.LineThrough else null,
                )

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    stringResource(
                        R.string.pathway_quest_meta,
                        stringResource(quest.statType.shortLabelRes()),
                        stringResource(quest.difficulty.nameRes()),
                    ).uppercaseLocalized(),
                    style = MaterialTheme.typography.labelSmall,
                    color = statColor.copy(alpha = alpha),
                )

                val questDescription = quest.localizedDescription()
                if (questDescription.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        questDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                AuraBar(
                    progress = questProgress.fraction,
                    color = statColor.copy(alpha = alpha),
                    height = 6.dp,
                )
            }

            Spacer(Modifier.width(Spacing.md))

            if (questProgress.isComplete) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.common_completed),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                DataValue(
                    text = stringResource(
                        R.string.pathway_quest_completions,
                        questProgress.completions,
                        quest.requiredCompletions,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
    }
}

@Composable
private fun StageNode(
    stage: Int,
    unlocked: Boolean,
    complete: Boolean,
    spineLit: Boolean,
    isLast: Boolean,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    val gutter = 30.dp
    val nodeCenterX = 11.dp
    val nodeCenterY = 10.dp
    val nodeRadius = 7.dp

    val dim = MaterialTheme.colorScheme.outline
    val spineColor = if (spineLit) accent else dim
    val nodeColor = if (unlocked) accent else dim

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) Spacing.sm else Spacing.lg)
            .drawBehind {
                val cx = nodeCenterX.toPx()
                val cy = nodeCenterY.toPx()
                val r = nodeRadius.toPx()

                if (!isLast) {
                    drawLine(
                        color = spineColor,
                        start = Offset(cx, cy + r + 3.dp.toPx()),
                        end = Offset(cx, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                if (complete) {
                    drawCircle(color = nodeColor, radius = r, center = Offset(cx, cy))
                } else {
                    drawCircle(
                        color = nodeColor,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                    if (unlocked) {
                        drawCircle(
                            color = nodeColor,
                            radius = r * 0.38f,
                            center = Offset(cx, cy),
                        )
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier.padding(start = gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.pathway_detail_stage, stage),
                style = MaterialTheme.typography.titleMedium,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Spacing.sm))
            when {
                !unlocked -> Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.common_locked),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )

                !complete -> StatChip(
                    label = stringResource(R.string.pathway_stage_active),
                    color = accent,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        Column(
            modifier = Modifier.padding(start = gutter),
            content = content,
        )
    }
}
