package com.mehmetbozkurt.questlog.feature.character

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextOverflow
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.descriptionRes
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraBar
import com.mehmetbozkurt.questlog.core.designsystem.component.CharacterCrest
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
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
    CharacterScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun CharacterScreen(
    state: CharacterState,
    onEvent: (CharacterEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        val character = state.character

        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            character == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                    .padding(horizontal = Spacing.screen)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Spacing.lg))

                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(Spacing.card),
                ) {
                    CharacterCrest(
                        character = character,
                        levelProgress = state.levelProgress,
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    Text(
                        text = stringResource(levelRankRes(character.level))
                            .uppercaseLocalized(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.xs))

                    Text(
                        text = stringResource(
                            R.string.character_hero_subtitle,
                            character.level,
                            character.totalXp,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    DataValue(
                        text = if (character.level >= XpCurve.MAX_LEVEL) {
                            stringResource(R.string.profile_max_level)
                        } else {
                            stringResource(
                                R.string.character_xp_progress,
                                character.xpIntoLevel,
                                character.xpToNextLevel,
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.md))

                    AuraBar(
                        progress = state.levelProgress,
                        color = MaterialTheme.colorScheme.primary,
                        height = Spacing.barHeight,
                    )
                }

                if (character.pendingFeatChoices > 0) {
                    Spacer(Modifier.height(Spacing.lg))
                    GlassPanel(
                        onClick = { onEvent(CharacterEvent.FeatDialogToggled(true)) },
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(Spacing.lg),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(
                                icon = Icons.Default.Stars,
                                color = MaterialTheme.colorScheme.primary,
                                size = 36.dp,
                            )
                            Spacer(Modifier.width(Spacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(
                                        R.string.character_pending_feats_available,
                                        character.pendingFeatChoices,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    stringResource(R.string.character_tap_to_choose),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (state.feats.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.section))
                    SectionTitle(
                        text = stringResource(R.string.character_earned_feats),
                        icon = Icons.Default.Stars,
                    )
                    Spacer(Modifier.height(Spacing.md))

                    state.feats.chunked(2).forEach { pair ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            pair.forEach { feat ->
                                GlassPanel(
                                    modifier = Modifier.weight(1f),
                                    containerColor = wellColor(),
                                    contentPadding = PaddingValues(Spacing.lg),
                                ) {
                                    IconTile(
                                        icon = Icons.Default.MilitaryTech,
                                        color = MaterialTheme.colorScheme.primary,
                                        size = 36.dp,
                                    )
                                    Spacer(Modifier.height(Spacing.md))
                                    Text(
                                        stringResource(feat.featId.nameRes()),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        minLines = 2,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text(
                                        stringResource(feat.featId.descriptionRes()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        minLines = 3,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (feat.chosenStat != null) {
                                        Spacer(Modifier.height(Spacing.sm))
                                        DataValue(
                                            text = stringResource(feat.chosenStat.nameRes()),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                            repeat(2 - pair.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(Spacing.md))
                    }
                }

                Spacer(Modifier.height(Spacing.section))

                WeeklySummaryCard(streak = state.streak, weekly = state.weekly)

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
