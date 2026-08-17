package com.mehmetbozkurt.questlog.feature.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.mehmetbozkurt.questlog.domain.model.FeatCatalog
import com.mehmetbozkurt.questlog.domain.model.StatProgress
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import com.mehmetbozkurt.questlog.feature.character.component.FeatChoiceDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CharacterRoute(
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CharacterEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text)
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
                        "Karakter",
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
                Modifier.fillMaxSize().padding(padding).padding(Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Karakter yükleniyor. İlk görevini tamamladığında burası canlanacak.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "SEVİYE",
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
                                "Maksimum seviye"
                            } else {
                                "${character.xpIntoLevel} / ${character.xpToNextLevel} XP"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            "Toplam ${character.totalXp} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (character.pendingFeatChoices > 0) {
                    Spacer(Modifier.height(Spacing.md))
                    Card(
                        onClick = { onEvent(CharacterEvent.FeatDialogToggled(true)) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${character.pendingFeatChoices} yetenek hakkın var",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    "Seçmek için dokun",
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
                    "Yetenekler",
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
                        "Kazanılan Yetenekler",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(Spacing.md))

                    state.feats.forEach { feat ->
                        val def = FeatCatalog.byId(feat.featId)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(Spacing.md)) {
                                Text(
                                    def.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    def.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (feat.chosenStat != null) {
                                    Text(
                                        "Odak: ${feat.chosenStat.displayName()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
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
            Row {
                Text(
                    stat.statType.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (stat.value >= XpCurve.MAX_STAT) "MAX"
                    else "${stat.currentXp} / ${stat.xpToNext}",
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