package com.mehmetbozkurt.questlog.feature.character

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.CharacterCrest
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionEyebrow
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.descriptionRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.nameRes
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

                CharacterCrest(
                    character = character,
                    levelProgress = state.levelProgress,
                )

                Spacer(Modifier.height(Spacing.sm))

                Row(Modifier.fillMaxWidth()) {
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
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.character_total_xp, character.totalXp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (character.pendingFeatChoices > 0) {
                    Spacer(Modifier.height(Spacing.lg))
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

                Spacer(Modifier.height(Spacing.lg))

                WeeklySummaryCard(streak = state.streak, weekly = state.weekly)

                if (state.feats.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.xl))
                    SectionEyebrow(stringResource(R.string.character_earned_feats))
                    Spacer(Modifier.height(Spacing.md))

                    state.feats.forEach { feat ->
                        QuestCard(
                            seed = feat.featId.hashCode(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(feat.featId.nameRes()),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                stringResource(feat.featId.descriptionRes()),
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
