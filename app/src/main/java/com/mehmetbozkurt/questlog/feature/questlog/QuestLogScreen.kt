package com.mehmetbozkurt.questlog.feature.questlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
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
import com.mehmetbozkurt.questlog.feature.questlog.component.ActivePathwayCard
import com.mehmetbozkurt.questlog.feature.questlog.component.CharacterSummaryCard
import com.mehmetbozkurt.questlog.feature.questlog.component.FilterSheet
import com.mehmetbozkurt.questlog.feature.questlog.component.QuestLogCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QuestLogListRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToPathways: () -> Unit,
    onNavigateToPathwayDetail: (String) -> Unit,
    onNavigateToCharacter: () -> Unit,
    viewModel: QuestLogListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuestLogListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                QuestLogListEffect.NavigateToCreate -> onNavigateToCreate()
                QuestLogListEffect.NavigateToPathways -> onNavigateToPathways()
                is QuestLogListEffect.NavigateToPathwayDetail ->
                    onNavigateToPathwayDetail(effect.pathwayId)
                QuestLogListEffect.NavigateToCharacter -> onNavigateToCharacter()
                is QuestLogListEffect.ShowXpMessage ->
                    snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    QuestLogListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestLogListScreen(
    state: QuestLogListState,
    onEvent: (QuestLogListEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Text(
                            "Yolculuk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    actions = {
                        IconButton(onClick = { onEvent(QuestLogListEvent.PathwaysClicked) }) {
                            Icon(Icons.Default.Explore, contentDescription = "Yollar")
                        }
                        BadgedBox(
                            badge = {
                                if (state.activeFilterCount > 0) {
                                    Badge { Text("${state.activeFilterCount}") }
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                onEvent(QuestLogListEvent.FilterSheetToggled(true))
                            }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrele")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                )

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(QuestLogListEvent.SearchChanged(it)) },
                    placeholder = { Text("Görevlerde ara...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(0.dp),
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEvent(QuestLogListEvent.SearchChanged(""))
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(QuestLogListEvent.CreateClicked) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni görev")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = padding.calculateTopPadding() + Spacing.sm,
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {


            if (state.showHeaderSections && state.character != null) {
                item(key = "character") {
                    CharacterSummaryCard(
                        character = state.character,
                        progress = state.levelProgress,
                        onClick = { onEvent(QuestLogListEvent.CharacterClicked) },
                    )
                }
            }


            if (state.showHeaderSections && state.activePathways.isNotEmpty()) {
                item(key = "pathway_header") {
                    SectionHeader("Devam Eden Yollar")
                }
                items(state.activePathways, key = { "pw_${it.pathway.id}" }) { summary ->
                    ActivePathwayCard(
                        summary = summary,
                        onClick = {
                            onEvent(QuestLogListEvent.PathwayClicked(summary.pathway.id))
                        },
                    )
                }
            }

            if (state.showHeaderSections && state.activePathways.isEmpty()) {
                item(key = "pathway_cta") {
                    Card(
                        onClick = { onEvent(QuestLogListEvent.PathwaysClicked) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(Spacing.md)) {
                            Text(
                                "Bir yola gir",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                "Yollar sıralı görevlerden oluşur. Tamamlarsan " +
                                        "biriken XP'yi ve bonusu kazanırsın.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (state.isEmpty) {
                item(key = "empty") {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            if (state.isEmptyBecauseOfFilters) "Eşleşen görev yok"
                            else "Henüz görevin yok",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            if (state.isEmptyBecauseOfFilters)
                                "Arama veya filtreleri değiştirmeyi dene."
                            else
                                "Bir yola gir ya da + ile kendi görevini oluştur.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                if (state.activeLogs.isNotEmpty()) {
                    item(key = "active_header") {
                        SectionHeader(
                            if (state.isSearching) "Sonuçlar" else "Görevlerim",
                            "${state.activeLogs.size}",
                        )
                    }
                    items(state.activeLogs, key = { it.id }) { log ->
                        QuestLogCard(
                            log = log,
                            onClick = { onEvent(QuestLogListEvent.LogClicked(log.id)) },
                            onToggleCompleted = { checked ->
                                onEvent(QuestLogListEvent.CompletionToggled(log.id, checked))
                            },
                        )
                    }
                }

                if (state.completedLogs.isNotEmpty()) {
                    item(key = "completed_header") {
                        SectionHeader("Tamamlananlar", "${state.completedLogs.size}")
                    }
                    items(state.completedLogs, key = { it.id }) { log ->
                        QuestLogCard(
                            log = log,
                            onClick = { onEvent(QuestLogListEvent.LogClicked(log.id)) },
                            onToggleCompleted = { checked ->
                                onEvent(QuestLogListEvent.CompletionToggled(log.id, checked))
                            },
                        )
                    }
                }
            }
        }
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
private fun SectionHeader(title: String, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(
                trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}