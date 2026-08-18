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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.PathwayQuestProgress
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.displayName
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PathwayDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: PathwayDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PathwayDetailEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    PathwayDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (state.isActive) {
                        TextButton(
                            onClick = { onEvent(PathwayDetailEvent.AbandonDialogToggled(true)) }
                        ) {
                            Text("Bırak", color = MaterialTheme.colorScheme.error)
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
                    "Yol bulunamadı",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                val statColor = detail.pathway.primaryStat.colorHex().toComposeColor()

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
                        Text(
                            buildString {
                                append(detail.pathway.primaryStat.displayName())
                                detail.pathway.secondaryStat?.let {
                                    append(" + ${it.displayName()}")
                                }
                                append(" · Kademe ${detail.pathway.tier}")
                            },
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
                            "${detail.completedQuests} / ${detail.totalQuests} görev tamamlandı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (state.isActive) {
                            Text(
                                "${detail.progress?.escrowedXp ?: 0} XP emanette · " +
                                        "Bitirince +${detail.pathway.completionBonusXp} bonus",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    if (state.isCompleted) {
                        Spacer(Modifier.height(Spacing.md))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Bu yolu tamamladın.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(Spacing.md),
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
                                Text("Bu Yola Gir", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        if (!state.canStartMore) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                "Aktif yol sınırına ulaştın. Önce birini bitir veya bırak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    detail.stages.forEach { (stage, quests) ->
                        val unlocked = detail.isStageUnlocked(stage)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Aşama $stage",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (unlocked)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!unlocked) {
                                Spacer(Modifier.width(Spacing.sm))
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Kilitli",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(Spacing.sm))

                        quests.forEach { qp ->
                            QuestRow(
                                questProgress = qp,
                                statColor = qp.quest.statType.colorHex().toComposeColor(),
                                unlocked = unlocked,
                                enabled = state.isActive && !state.isWorking,
                                onClick = { onEvent(PathwayDetailEvent.QuestClicked(qp.quest.id)) },
                            )
                            Spacer(Modifier.height(Spacing.sm))
                        }

                        Spacer(Modifier.height(Spacing.md))
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
            title = { Text("Yolu bırak") },
            text = {
                Text(
                    "Emanetteki $escrow XP kaybolacak. " +
                            "Yeteneklerinde kazandıkların kalacak. Emin misin?"
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(PathwayDetailEvent.AbandonConfirmed) }) {
                    Text("Bırak", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PathwayDetailEvent.AbandonDialogToggled(false)) }) {
                    Text("Vazgeç")
                }
            },
        )
    }
}

@Composable
private fun QuestRow(
    questProgress: PathwayQuestProgress,
    statColor: androidx.compose.ui.graphics.Color,
    unlocked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val quest = questProgress.quest
    val alpha = if (unlocked) 1f else 0.4f
    val clickable = unlocked && enabled && !questProgress.isComplete

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick,
        enabled = clickable,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.md),
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
                        "${quest.statType.displayName()} · ${quest.difficulty.displayName()}",
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
                    contentDescription = "Tamamlandı",
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "${questProgress.completions}/${quest.requiredCompletions}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
    }
}