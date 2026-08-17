package com.mehmetbozkurt.questlog.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.CatalogQuest
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.description
import com.mehmetbozkurt.questlog.domain.model.displayName
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CatalogRoute(
    onNavigateBack: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CatalogEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    CatalogScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogState,
    onEvent: (CatalogEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Text(
                            "Yollar",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                )

                ScrollableTabRow(
                    selectedTabIndex = StatType.entries.indexOf(state.selectedStat),
                    containerColor = MaterialTheme.colorScheme.background,
                    edgePadding = Spacing.lg,
                ) {
                    StatType.entries.forEach { stat ->
                        val selected = state.selectedStat == stat
                        Tab(
                            selected = selected,
                            onClick = { onEvent(CatalogEvent.StatSelected(stat)) },
                            text = {
                                Text(
                                    stat.displayName(),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            selectedContentColor = stat.colorHex().toComposeColor(),
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val quests = state.questsForSelectedStat

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.isEmpty -> Box(
                Modifier.fillMaxSize().padding(padding).padding(Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Katalog yüklenemedi. İnternet bağlantını kontrol et.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = padding.calculateTopPadding() + Spacing.md,
                    bottom = padding.calculateBottomPadding() + Spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item {
                    Text(
                        state.selectedStat.description(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                items(quests, key = { it.id }) { quest ->
                    CatalogQuestCard(
                        quest = quest,
                        isAdded = state.isAlreadyAdded(quest),
                        isAdding = state.addingId == quest.id,
                        onClick = { onEvent(CatalogEvent.QuestClicked(quest)) },
                    )
                }

                if (quests.isEmpty()) {
                    item {
                        Text(
                            "Bu yolda henüz görev yok.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogQuestCard(
    quest: CatalogQuest,
    isAdded: Boolean,
    isAdding: Boolean,
    onClick: () -> Unit,
) {
    val statColor = quest.statType.colorHex().toComposeColor()

    Card(
        onClick = onClick,
        enabled = !isAdded && !isAdding,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(statColor, CircleShape))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        quest.difficulty.displayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = statColor,
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (quest.description.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        quest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(Spacing.md))

            when {
                isAdding -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                isAdded -> Icon(
                    Icons.Default.Check,
                    contentDescription = "Eklendi",
                    tint = MaterialTheme.colorScheme.primary,
                )
                else -> Icon(
                    Icons.Default.Add,
                    contentDescription = "Ekle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}