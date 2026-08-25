package com.mehmetbozkurt.questlog.feature.pathway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mehmetbozkurt.questlog.core.designsystem.accentWidth
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.PathwayQuestProgress
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import kotlinx.coroutines.flow.collectLatest

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
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (state.isActive) {
                        TextButton(
                            onClick = { onEvent(PathwayDetailEvent.AbandonDialogToggled(true)) }
                        ) {
                            Text(
                                stringResource(R.string.pathway_detail_abandon),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            detail == null -> Box(
                Modifier.fillMaxSize().padding(padding),
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
                        .padding(horizontal = Spacing.lg)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(statColor, CircleShape))
                        Spacer(Modifier.width(Spacing.sm))
                        val primaryStatName =
                            stringResource(detail.pathway.primaryStat.nameRes())
                        val secondaryStatName = detail.pathway.secondaryStat
                            ?.let { stringResource(it.nameRes()) }

                        Text(
                            if (secondaryStatName != null) stringResource(
                                R.string.pathway_stats_tier_secondary,
                                primaryStatName,
                                secondaryStatName,
                                detail.pathway.tier,
                            ) else stringResource(
                                R.string.pathway_stats_tier,
                                primaryStatName,
                                detail.pathway.tier,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = statColor,
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Text(
                        detail.pathway.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        detail.pathway.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    if (state.isActive || state.isCompleted) {
                        LinearProgressIndicator(
                            progress = { detail.progressFraction },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = statColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            stringResource(
                                R.string.pathway_detail_progress,
                                detail.completedQuests,
                                detail.totalQuests,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (state.isActive) {
                            Text(
                                stringResource(
                                    R.string.pathway_detail_escrow,
                                    detail.progress?.escrowedXp ?: 0,
                                    detail.pathway.completionBonusXp,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    if (state.isCompleted) {
                        Spacer(Modifier.height(Spacing.md))
                        QuestCard(
                            seed = 23,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(R.string.pathway_detail_done),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    if (!state.isActive && !state.isCompleted) {
                        Spacer(Modifier.height(Spacing.md))
                        Button(
                            onClick = { onEvent(PathwayDetailEvent.StartClicked) },
                            enabled = !state.isWorking && state.canStartMore,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
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
                        if (!state.canStartMore) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                stringResource(R.string.pathway_detail_limit),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

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
            text = {
                Text(
                    stringResource(R.string.pathway_detail_abandon_body, escrow)
                )
            },
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
        )
    }
}

@Composable
private fun QuestRow(
    questProgress: PathwayQuestProgress,
    statColor: Color,
    unlocked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val quest = questProgress.quest
    val alpha = if (unlocked) 1f else 0.4f
    val clickable = unlocked && enabled && !questProgress.isComplete

    QuestCard(
        onClick = if (clickable) onClick else null,
        accent = statColor.copy(alpha = alpha),
        accentWidth = quest.difficulty.accentWidth(),
        seed = quest.id.hashCode(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(8.dp)
                            .background(statColor.copy(alpha = alpha), CircleShape)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        stringResource(
                            R.string.pathway_quest_meta,
                            stringResource(quest.statType.nameRes()),
                            stringResource(quest.difficulty.nameRes()),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = statColor.copy(alpha = alpha),
                    )
                }

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                )

                if (quest.description.isNotBlank()) {
                    Text(
                        quest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                LinearProgressIndicator(
                    progress = { questProgress.fraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = statColor.copy(alpha = alpha),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
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
                Text(
                    stringResource(
                        R.string.pathway_quest_completions,
                        questProgress.completions,
                        quest.requiredCompletions,
                    ),
                    style = MaterialTheme.typography.labelLarge,
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
    val nodeCenterY = 13.dp
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
                stringResource(R.string.pathway_detail_stage, stage).uppercaseLocalized(),
                style = MaterialTheme.typography.labelLarge,
                color = if (unlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!unlocked) {
                Spacer(Modifier.width(Spacing.sm))
                Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.common_locked),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        Column(
            modifier = Modifier.padding(start = gutter),
            content = content,
        )
    }
}
