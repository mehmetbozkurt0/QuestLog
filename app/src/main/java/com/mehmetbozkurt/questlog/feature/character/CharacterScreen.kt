package com.mehmetbozkurt.questlog.feature.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Shield
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
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.FeatCatalog
import com.mehmetbozkurt.questlog.domain.model.StatProgress
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.descriptionRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import com.mehmetbozkurt.questlog.feature.character.component.FeatChoiceDialog
import com.mehmetbozkurt.questlog.feature.character.component.WeeklySummaryCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CharacterRoute(
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CharacterEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))
            }
        }
    }
    CharacterScreen(state = state, onEvent = viewModel::onEvent, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    state: CharacterState,
    onEvent: (CharacterEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.character_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState)},
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val character = state.character

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            character == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.character_empty_title),
                    body = stringResource(R.string.character_empty_body),
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.lg)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Spacing.md))

                QuestCard(
                    seed = 11,
                    contentPadding = PaddingValues(Spacing.lg),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.character_level_caps),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${character.level}",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(Modifier.height(Spacing.md))

                        LinearProgressIndicator(
                            progress = { state.levelProgress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round,
                        )

                        Spacer(Modifier.height(Spacing.sm))

                        Text(
                            if (character.level >= XpCurve.MAX_LEVEL) {
                                stringResource(R.string.profile_max_level)
                            } else {
                                stringResource(
                                    R.string.character_xp_progress,
                                    character.xpIntoLevel,
                                    character.xpToNextLevel,
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            stringResource(R.string.character_total_xp, character.totalXp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                WeeklySummaryCard(streak = state.streak, weekly = state.weekly)

                if (character.pendingFeatChoices > 0) {
                    Spacer(Modifier.height(Spacing.md))
                    QuestCard(
                        onClick = { onEvent(CharacterEvent.FeatDialogToggled(true)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        seed = 13,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(
                                        R.string.character_pending_feats_available,
                                        character.pendingFeatChoices,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    stringResource(R.string.character_tap_to_choose),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                Text(
                    stringResource(R.string.character_abilities),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Spacing.md))

                character.stats.forEach { stat ->
                    StatRow(stat)
                    Spacer(Modifier.height(Spacing.md))
                }

                if (state.feats.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.lg))
                    Text(
                        stringResource(R.string.character_earned_feats),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(Spacing.md))

                    state.feats.forEach { feat ->
                        val def = FeatCatalog.byId(feat.featId)
                        QuestCard(
                            seed = feat.featId.hashCode(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(def.id.nameRes()),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                stringResource(def.id.descriptionRes()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (feat.chosenStat != null) {
                                Text(
                                    stringResource(
                                        R.string.character_feat_focus,
                                        stringResource(feat.chosenStat.nameRes()),
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }

    if (state.showFeatDialog) {
        FeatChoiceDialog(
            selectedFeatId = state.selectedFeatId,
            selectedStat = state.selectedStatForFeat,
            ownedFeatIds = state.ownedFeatIds,
            canConfirm = state.canConfirmFeat,
            isSaving = state.isSavingFeat,
            onFeatSelected = { onEvent(CharacterEvent.FeatSelected(it)) },
            onStatSelected = { onEvent(CharacterEvent.StatForFeatSelected(it)) },
            onConfirm = { onEvent(CharacterEvent.FeatConfirmed) },
            onDismiss = { onEvent(CharacterEvent.FeatDialogToggled(false)) },
        )
    }
}

@Composable
private fun StatRow(stat: StatProgress) {
    val statColor = stat.statType.colorHex().toComposeColor()
    val progress = if (stat.value >= XpCurve.MAX_STAT) 1f
    else (stat.currentXp.toFloat() / stat.xpToNext).coerceIn(0f, 1f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(statColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${stat.value}",
                style = MaterialTheme.typography.titleLarge,
                color = statColor,
            )
        }

        Spacer(Modifier.width(Spacing.md))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = stat.statType.icon(),
                    contentDescription = null,
                    tint = statColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    stringResource(stat.statType.nameRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (stat.value >= XpCurve.MAX_STAT)
                        stringResource(R.string.character_stat_max)
                    else stringResource(
                        R.string.character_stat_progress,
                        stat.currentXp,
                        stat.xpToNext,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.xs))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = statColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}