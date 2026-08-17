package com.mehmetbozkurt.questlog.feature.questlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.feature.questlog.component.FilterSheet
import com.mehmetbozkurt.questlog.feature.questlog.component.QuestLogCard
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.remember
import com.mehmetbozkurt.questlog.feature.questlog.component.FilterSheet

@Composable
fun QuestLogListRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: QuestLogListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuestLogListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                QuestLogListEffect.NavigateToCreate -> onNavigateToCreate()
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
                            "Seyir Defteri",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    actions = {
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
                    placeholder = { Text("Ara...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
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
                Icon(Icons.Default.Add, contentDescription = "Yeni kayıt")
            }
        },
        snackbarHost = {SnackbarHost(snackbarHostState)},
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (state.isEmptyBecauseOfFilters) "Eşleşen kayıt yok"
                        else "Defter henüz boş",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        if (state.isEmptyBecauseOfFilters)
                            "Arama veya filtreleri değiştirmeyi dene."
                        else "İlk kaydını oluşturmak için + düğmesine dokun.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = padding.calculateTopPadding() + Spacing.sm,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(state.logs, key = { it.id }) { log ->
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