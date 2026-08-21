package com.mehmetbozkurt.questlog.feature.pathway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.displayName
import com.mehmetbozkurt.questlog.domain.progression.PathwayRules
import kotlinx.coroutines.flow.collectLatest

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathwayListScreen(
    state: PathwayListState,
    onEvent: (PathwayListEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yollar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
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
                bottom = padding.calculateBottomPadding() + Spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (state.activeItems.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Devam Eden Yollar",
                        "${state.activeCount} / ${PathwayRules.MAX_ACTIVE_PATHWAYS}",
                    )
                }
                items(state.activeItems, key = { it.pathway.id }) { item ->
                    PathwayCard(item) { onEvent(PathwayListEvent.PathwayClicked(item.pathway.id)) }
                }
            }

            if (state.availableItems.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Açık Yollar",
                        if (!state.canStartMore) "Sınıra ulaştın" else null,
                    )
                }
                items(state.availableItems, key = { it.pathway.id }) { item ->
                    PathwayCard(item) { onEvent(PathwayListEvent.PathwayClicked(item.pathway.id)) }
                }
            }

            if (state.completedItems.isNotEmpty()) {
                item { SectionHeader("Tamamlanan Yollar", null) }
                items(state.completedItems, key = { it.pathway.id }) { item ->
                    PathwayCard(item) { onEvent(PathwayListEvent.PathwayClicked(item.pathway.id)) }
                }
            }

            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CloudOff,
                        title = "Yol haritası boş",
                        body = "Yollar buluttan gelir. İnternet bağlantını kontrol edip tekrar dene.",
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String?) {
    Row(
        Modifier.fillMaxWidth().padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PathwayCard(item: PathwayListItem, onClick: () -> Unit) {
    val pathway = item.pathway
    val statColor = pathway.primaryStat.colorHex().toComposeColor()

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(statColor, CircleShape))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    buildString {
                        append(pathway.primaryStat.displayName())
                        pathway.secondaryStat?.let { append(" + ${it.displayName()}") }
                        append(" · Kademe ${pathway.tier}")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = statColor,
                )
                Spacer(Modifier.weight(1f))

                when {
                    item.isCompleted -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    item.isLocked -> Icon(
                        Icons.Default.Lock,
                        contentDescription = "Kilitli",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            Text(
                pathway.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Spacing.xs))

            Text(
                pathway.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (item.isLocked && item.requiredPathwayTitle != null) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "Önce \"${item.requiredPathwayTitle}\" tamamlanmalı",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (item.isActive) {
                Spacer(Modifier.height(Spacing.md))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = statColor,
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Devam ediyor · ${item.progress?.escrowedXp ?: 0} XP emanette",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}