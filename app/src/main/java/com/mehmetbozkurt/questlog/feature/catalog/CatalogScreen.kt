package com.mehmetbozkurt.questlog.feature.catalog

import com.mehmetbozkurt.questlog.core.common.localizedDescription
import com.mehmetbozkurt.questlog.core.common.localizedTitle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellBackBar
import androidx.compose.foundation.layout.RowScope
import com.mehmetbozkurt.questlog.core.designsystem.component.gridItems
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.ScreenTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.domain.model.CatalogEntry
import com.mehmetbozkurt.questlog.domain.model.CatalogTask
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.progression.CatalogRules
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CatalogRoute(
    onNavigateBack: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var celebration by remember { mutableStateOf<Celebration?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CatalogEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))

                is CatalogEffect.ShowCelebration -> celebration = effect.celebration
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        CatalogScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
        )
        CelebrationHost(
            celebration = celebration,
            onDismiss = { celebration = null },
        )
    }
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
            ShellBackBar(
                title = stringResource(R.string.catalog_title),
                onBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.isEmpty) {
            Box(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.catalog_empty_title),
                    body = stringResource(R.string.catalog_empty_body),
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.screen,
                end = Spacing.screen,
                top = padding.calculateTopPadding() + Spacing.lg,
                bottom = padding.calculateBottomPadding() + Spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item(key = "hero") {
                ScreenTitle(
                    title = stringResource(R.string.catalog_entry_title),
                    subtitle = if (state.remainingToday > 0) {
                        stringResource(
                            R.string.catalog_daily_left,
                            state.remainingToday,
                            CatalogRules.MAX_PER_DAY,
                        )
                    } else {
                        stringResource(R.string.catalog_daily_none)
                    },
                )
            }

            item(key = "filters") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    StatType.entries.forEach { stat ->
                        val selected = state.statFilter == stat
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onEvent(
                                    CatalogEvent.StatFilterChanged(if (selected) null else stat)
                                )
                            },
                            label = {
                                Text(
                                    stringResource(stat.shortLabelRes()).uppercaseLocalized(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = stat.icon(),
                                    contentDescription = null,
                                    tint = stat.color(),
                                    modifier = Modifier.size(14.dp),
                                )
                            },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedContainerColor = stat.color().copy(alpha = 0.16f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedLabelColor = stat.color(),
                            ),
                        )
                    }
                }
            }

            gridItems(state.visibleEntries, key = { it.task.id }) { entry ->
                CatalogTaskCard(
                    entry = entry,
                    enabled = !entry.doneToday && state.remainingToday > 0,
                    onComplete = { onEvent(CatalogEvent.TaskCompleted(entry.task.id)) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.CatalogTaskCard(
    entry: CatalogEntry,
    enabled: Boolean,
    onComplete: () -> Unit,
) {
    val task = entry.task
    val statColor = task.statType.color()
    val done = entry.doneToday

    GlassPanel(
        onClick = if (enabled) onComplete else null,
        enabled = enabled,
        containerColor = if (done) wellColor() else MaterialTheme.extendedColors.glass,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier
            .weight(1f)
            .alpha(if (done) 0.5f else 1f),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            IconTile(
                icon = task.statType.icon(),
                color = statColor,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(Modifier.weight(1f))
            if (done) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.catalog_done_today),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = task.localizedTitle(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(Spacing.xs))

        Text(
            text = task.localizedDescription(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(Spacing.md))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DataValue(
                text = stringResource(task.difficulty.nameRes()).uppercaseLocalized(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            DataValue(
                text = "+${task.difficulty.baseXp}",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
