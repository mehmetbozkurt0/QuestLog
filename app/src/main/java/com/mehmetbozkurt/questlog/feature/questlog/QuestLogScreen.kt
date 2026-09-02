package com.mehmetbozkurt.questlog.feature.questlog

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import com.mehmetbozkurt.questlog.core.designsystem.rememberAppHaptics
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.Eyebrow
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.rimColor
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.progression.HabitRules
import com.mehmetbozkurt.questlog.feature.proof.ProofSheet
import com.mehmetbozkurt.questlog.core.designsystem.component.gridItems
import com.mehmetbozkurt.questlog.feature.pathway.component.PathwayGridCard
import com.mehmetbozkurt.questlog.feature.questlog.component.CharacterSummaryCard
import com.mehmetbozkurt.questlog.feature.questlog.component.FilterSheet
import com.mehmetbozkurt.questlog.feature.questlog.component.HabitSlotCard
import com.mehmetbozkurt.questlog.feature.questlog.component.QuestLogCard
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun QuestLogListRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: (Int) -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToPathways: () -> Unit,
    onNavigateToPathwayDetail: (String) -> Unit,
    onNavigateToCharacter: () -> Unit,
    viewModel: QuestLogListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    var celebration by remember { mutableStateOf<Celebration?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuestLogListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is QuestLogListEffect.NavigateToCreate -> onNavigateToCreate(effect.slotIndex)
                QuestLogListEffect.NavigateToCatalog -> onNavigateToCatalog()
                QuestLogListEffect.NavigateToPathways -> onNavigateToPathways()
                is QuestLogListEffect.NavigateToPathwayDetail ->
                    onNavigateToPathwayDetail(effect.pathwayId)
                QuestLogListEffect.NavigateToCharacter -> onNavigateToCharacter()
                is QuestLogListEffect.ShowXpMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))
                is QuestLogListEffect.ShowCelebration -> {
                    haptics.confirm()
                    celebration = effect.celebration
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        QuestLogListScreen(
            state = state,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
        )
        CelebrationHost(
            celebration = celebration,
            onDismiss = { celebration = null },
        )
    }
}

@Composable
fun QuestLogListScreen(
    state: QuestLogListState,
    onEvent: (QuestLogListEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->

        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(QuestLogListEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.screen,
                    end = Spacing.screen,
                    top = Spacing.lg,
                    bottom = padding.calculateBottomPadding() + Spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {

                if (state.character != null) {
                    item(key = "character") {
                        CharacterSummaryCard(
                            character = state.character,
                            progress = state.levelProgress,
                            streak = state.streak,
                            onClick = { onEvent(QuestLogListEvent.CharacterClicked) },
                        )
                    }
                }

                item(key = "search") {
                    SearchRow(
                        query = state.searchQuery,
                        filterCount = state.activeFilterCount,
                        onQueryChange = { onEvent(QuestLogListEvent.SearchChanged(it)) },
                        onFilterClick = { onEvent(QuestLogListEvent.FilterSheetToggled(true)) },
                    )
                }

                if (state.showHeaderSections) {
                    item(key = "pathway_header") {
                        Heading(stringResource(R.string.questlog_active_pathways))
                    }
                    if (state.activePathways.isNotEmpty()) {
                        gridItems(state.activePathways, key = { it.pathway.id }) { summary ->
                            PathwayGridCard(
                                title = summary.pathway.title,
                                stat = summary.pathway.primaryStat,
                                caption = "${(summary.fraction * 100).roundToInt()}%",
                                progress = summary.fraction,
                                accented = true,
                                onClick = {
                                    onEvent(QuestLogListEvent.PathwayClicked(summary.pathway.id))
                                },
                            )
                        }
                    } else {
                        item(key = "pathway_cta") {
                            CtaCard(
                                icon = Icons.Default.Explore,
                                title = stringResource(R.string.questlog_pathway_cta_title),
                                body = stringResource(R.string.questlog_pathway_cta_body),
                                onClick = { onEvent(QuestLogListEvent.PathwaysClicked) },
                            )
                        }
                    }

                    item(key = "daily_header") {
                        Heading(stringResource(R.string.questlog_daily_header))
                    }

                    item(key = "habits_header") {
                        LabelRow(
                            label = stringResource(R.string.questlog_habits_header),
                            value = "${state.habitSlots.count { !it.isEmpty }}/${HabitRules.MAX_SLOTS}",
                        )
                    }
                    items(state.habitSlots, key = { "habit_${it.index}" }) { slot ->
                        HabitSlotCard(
                            slot = slot,
                            onClick = { onEvent(QuestLogListEvent.HabitSlotClicked(slot.index)) },
                            onToggleCompleted = { checked ->
                                slot.quest?.let {
                                    onEvent(QuestLogListEvent.CompletionToggled(it.id, checked))
                                }
                            },
                            onClear = { onEvent(QuestLogListEvent.HabitClearRequested(slot.index)) },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    item(key = "catalog_cta") {
                        CtaCard(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = stringResource(R.string.catalog_entry_title),
                            body = stringResource(R.string.catalog_entry_body),
                            onClick = { onEvent(QuestLogListEvent.CatalogClicked) },
                        )
                    }
                }

                if (state.isEmptyBecauseOfFilters) {
                    item(key = "empty") {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = stringResource(R.string.questlog_empty_filtered_title),
                            body = stringResource(R.string.questlog_empty_filtered_body),
                            actionLabel = stringResource(R.string.questlog_empty_filtered_action),
                            onAction = { onEvent(QuestLogListEvent.FiltersCleared) },
                        )
                    }
                } else if (!state.isEmpty) {
                    if (state.activeLogs.isNotEmpty()) {
                        item(key = "active_header") {
                            LabelRow(
                                label = stringResource(
                                    if (state.isSearching) R.string.questlog_results_header
                                    else R.string.questlog_active_header
                                ),
                                value = "${state.activeLogs.size}",
                            )
                        }
                        items(state.activeLogs, key = { it.id }) { log ->
                            QuestLogCard(
                                log = log,
                                onClick = { onEvent(QuestLogListEvent.LogClicked(log.id)) },
                                onToggleCompleted = { checked ->
                                    onEvent(QuestLogListEvent.CompletionToggled(log.id, checked))
                                },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    if (state.completedLogs.isNotEmpty()) {
                        item(key = "completed_header") {
                            LabelRow(
                                label = stringResource(R.string.questlog_completed_header),
                                value = "${state.completedLogs.size}",
                            )
                        }
                        items(state.completedLogs, key = { it.id }) { log ->
                            QuestLogCard(
                                log = log,
                                onClick = { onEvent(QuestLogListEvent.LogClicked(log.id)) },
                                onToggleCompleted = { checked ->
                                    onEvent(QuestLogListEvent.CompletionToggled(log.id, checked))
                                },
                                modifier = Modifier
                                    .animateItem()
                                    .alpha(0.6f),
                            )
                        }
                    }
                }
            }
        }
    }

    state.proofSheetLogId?.let { logId ->
        ProofSheet(
            logId = logId,
            questTitle = state.proofSheetTitle,
            onDismiss = { onEvent(QuestLogListEvent.ProofSheetDismissed) },
            onConfirm = { draft ->
                onEvent(
                    QuestLogListEvent.ProofConfirmed(
                        id = logId,
                        note = draft.note,
                        photoLocalPath = draft.photoLocalPath,
                    )
                )
            },
        )
    }

    if (state.slotPendingClear != null) {
        AlertDialog(
            onDismissRequest = { onEvent(QuestLogListEvent.HabitClearRequested(null)) },
            title = { Text(stringResource(R.string.questlog_habit_clear_title)) },
            text = { Text(stringResource(R.string.questlog_habit_clear_body)) },
            confirmButton = {
                TextButton(onClick = { onEvent(QuestLogListEvent.HabitClearConfirmed) }) {
                    Text(stringResource(R.string.questlog_habit_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(QuestLogListEvent.HabitClearRequested(null)) }) {
                    Text(stringResource(R.string.questlog_habit_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (state.showFilterSheet) {
        FilterSheet(
            completionFilter = state.completionFilter,
            statFilter = state.statFilter,
            priorityFilter = state.priorityFilter,
            sortOption = state.sortOption,
            onCompletionChange = { onEvent(QuestLogListEvent.CompletionFilterChanged(it)) },
            onStatChange = { onEvent(QuestLogListEvent.StatFilterChanged(it)) },
            onPriorityChange = { onEvent(QuestLogListEvent.PriorityFilterChanged(it)) },
            onSortChange = { onEvent(QuestLogListEvent.SortChanged(it)) },
            onClear = { onEvent(QuestLogListEvent.FiltersCleared) },
            onDismiss = { onEvent(QuestLogListEvent.FilterSheetToggled(false)) },
        )
    }
}

@Composable
private fun Heading(text: String) {
    SectionTitle(text = text, modifier = Modifier.padding(top = Spacing.md))
}

@Composable
private fun LabelRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Eyebrow(label)
        Spacer(Modifier.weight(1f))
        DataValue(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchRow(
    query: String,
    filterCount: Int,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    stringResource(R.string.questlog_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.common_clear),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = wellColor(),
                focusedContainerColor = wellColor(),
                unfocusedBorderColor = rimColor(),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(Spacing.sm))

        BadgedBox(
            badge = {
                if (filterCount > 0) {
                    Badge { Text("$filterCount") }
                }
            }
        ) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.questlog_filter),
                    tint = if (filterCount > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CtaCard(
    icon: ImageVector,
    title: String,
    body: String,
    onClick: () -> Unit,
) {
    GlassPanel(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        containerColor = wellColor(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = icon,
                color = MaterialTheme.colorScheme.primary,
                size = 36.dp,
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
