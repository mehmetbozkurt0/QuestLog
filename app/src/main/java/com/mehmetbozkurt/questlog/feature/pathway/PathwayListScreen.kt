package com.mehmetbozkurt.questlog.feature.pathway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.ScreenTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.gridItems
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.feature.pathway.component.PathwayGridCard
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun PathwayListRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: PathwayListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PathwayListEffect.NavigateToDetail -> onNavigateToDetail(effect.pathwayId)
            }
        }
    }

    PathwayListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun PathwayListScreen(
    state: PathwayListState,
    onEvent: (PathwayListEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    Scaffold(
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
            onRefresh = { onEvent(PathwayListEvent.Refresh) },
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
                item(key = "hero") {
                    Row(verticalAlignment = Alignment.Top) {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.common_back),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.width(Spacing.sm))
                        }
                        ScreenTitle(
                            title = stringResource(R.string.pathway_list_title),
                            subtitle = stringResource(R.string.pathway_list_subtitle),
                        )
                    }
                }

                if (state.items.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            icon = Icons.Default.CloudOff,
                            title = stringResource(R.string.pathway_list_empty_title),
                            body = stringResource(R.string.pathway_list_empty_body),
                        )
                    }
                    return@LazyColumn
                }

                if (state.activeItems.isNotEmpty()) {
                    item(key = "active_header") {
                        SectionTitle(
                            text = stringResource(R.string.pathway_list_active),
                            icon = Icons.Default.Explore,
                            modifier = Modifier.padding(top = Spacing.md),
                        )
                    }
                    gridItems(state.activeItems, key = { it.pathway.id }) { item ->
                        PathwayGridCard(
                            title = item.pathway.title,
                            stat = item.pathway.primaryStat,
                            caption = "${(item.fraction * 100).roundToInt()}%",
                            progress = item.fraction,
                            accented = true,
                            onClick = {
                                onEvent(PathwayListEvent.PathwayClicked(item.pathway.id))
                            },
                        )
                    }
                }

                if (state.availableItems.isNotEmpty()) {
                    item(key = "open_header") {
                        SectionTitle(
                            text = stringResource(R.string.pathway_list_open),
                            icon = Icons.Default.Map,
                            modifier = Modifier.padding(top = Spacing.md),
                            trailing = if (!state.canStartMore) {
                                {
                                    DataValue(
                                        text = stringResource(R.string.pathway_list_limit_reached),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else null,
                        )
                    }
                    gridItems(state.availableItems, key = { it.pathway.id }) { item ->
                        val locked = item.isLocked || !state.canStartMore
                        PathwayGridCard(
                            title = item.pathway.title,
                            stat = item.pathway.primaryStat,
                            caption = stringResource(
                                R.string.pathway_stage_count,
                                item.pathway.tier,
                                item.totalQuests,
                            ),
                            dimmed = locked,
                            badge = if (locked) Icons.Default.Lock else Icons.Default.Add,
                            onClick = {
                                onEvent(PathwayListEvent.PathwayClicked(item.pathway.id))
                            },
                        )
                    }
                }

                if (state.completedItems.isNotEmpty()) {
                    item(key = "done_header") {
                        SectionTitle(
                            text = stringResource(R.string.pathway_list_completed),
                            icon = Icons.Default.WorkspacePremium,
                            modifier = Modifier.padding(top = Spacing.md),
                        )
                    }
                    gridItems(state.completedItems, key = { it.pathway.id }) { item ->
                        PathwayGridCard(
                            title = item.pathway.title,
                            stat = item.pathway.primaryStat,
                            caption = stringResource(
                                R.string.pathway_quest_count,
                                item.completedQuests,
                                item.totalQuests,
                            ),
                            progress = 1f,
                            struck = true,
                            badge = Icons.Default.CheckCircle,
                            onClick = {
                                onEvent(PathwayListEvent.PathwayClicked(item.pathway.id))
                            },
                        )
                    }
                }
            }
        }
    }
}
