package com.mehmetbozkurt.questlog.feature.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.accentWidth
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.pips
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.domain.model.CatalogEntry
import com.mehmetbozkurt.questlog.domain.model.CatalogTask
import com.mehmetbozkurt.questlog.domain.model.Difficulty
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
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.catalog_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                start = Spacing.lg,
                end = Spacing.lg,
                top = padding.calculateTopPadding() + Spacing.sm,
                bottom = padding.calculateBottomPadding() + Spacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item(key = "quota") {
                Text(
                    text = if (state.remainingToday > 0) {
                        stringResource(
                            R.string.catalog_daily_left,
                            state.remainingToday,
                            CatalogRules.MAX_PER_DAY,
                        )
                    } else {
                        stringResource(R.string.catalog_daily_none)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.remainingToday > 0)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                )
            }

            item(key = "filters") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StatType.entries.forEach { stat ->
                        FilterChip(
                            selected = state.statFilter == stat,
                            onClick = {
                                onEvent(
                                    CatalogEvent.StatFilterChanged(
                                        if (state.statFilter == stat) null else stat
                                    )
                                )
                            },
                            label = { Text(stringResource(stat.shortLabelRes())) },
                            leadingIcon = {
                                Icon(
                                    imageVector = stat.icon(),
                                    contentDescription = null,
                                    tint = stat.color(),
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }

            items(state.visibleEntries, key = { it.task.id }) { entry ->
                CatalogTaskCard(
                    entry = entry,
                    enabled = !entry.doneToday && state.remainingToday > 0,
                    onComplete = { onEvent(CatalogEvent.TaskCompleted(entry.task.id)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun CatalogTaskCard(
    entry: CatalogEntry,
    enabled: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val task = entry.task
    val statColor = task.statType.color()

    QuestCard(
        onClick = if (enabled) onComplete else null,
        enabled = enabled,
        accent = statColor,
        accentWidth = task.difficulty.accentWidth(),
        seed = task.id.hashCode(),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = task.statType.icon(),
                contentDescription = null,
                tint = statColor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(Spacing.xs))
            Text(
                text = stringResource(task.statType.nameRes()),
                style = MaterialTheme.typography.labelMedium,
                color = statColor,
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = task.difficulty.pips(),
                style = MaterialTheme.typography.labelMedium,
                color = if (task.difficulty >= Difficulty.HARD)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${task.difficulty.baseXp} XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = task.localizedTitle(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (task.localizedDescription().isNotBlank()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = task.localizedDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (entry.doneToday || entry.completions > 0) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = if (entry.doneToday) stringResource(R.string.catalog_done_today)
                else stringResource(R.string.catalog_done_count, entry.completions),
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.doneToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CatalogTask.localizedTitle(): String =
    if (isEnglish()) titleEn ?: title else title

@Composable
private fun CatalogTask.localizedDescription(): String =
    if (isEnglish()) descriptionEn ?: description else description

@Composable
private fun isEnglish(): Boolean =
    androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language != "tr"
