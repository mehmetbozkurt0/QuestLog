package com.mehmetbozkurt.questlog.feature.logdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.common.Celebration
import com.mehmetbozkurt.questlog.core.designsystem.component.CelebrationHost
import androidx.compose.ui.graphics.Color
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.Eyebrow
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.OutlineChip
import com.mehmetbozkurt.questlog.core.designsystem.component.Rule
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellBackBar
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.component.StatChip
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.designsystem.theme.ContentHero
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.feature.logedit.formattedWithTime
import com.mehmetbozkurt.questlog.feature.proof.ProofSheet
import com.mehmetbozkurt.questlog.feature.questlog.component.formatted
import com.mehmetbozkurt.questlog.feature.questlog.component.labelRes
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun LogDetailRoute(
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LogDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var celebration by remember { mutableStateOf<Celebration?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LogDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                LogDetailEffect.NavigateBack -> onNavigateBack()
                is LogDetailEffect.ShowXpMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))
                is LogDetailEffect.ShowCelebration -> celebration = effect.celebration
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LogDetailScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState
        )
        CelebrationHost(
            celebration = celebration,
            onDismiss = { celebration = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    state: LogDetailState,
    onEvent: (LogDetailEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val log = state.log

    Scaffold(
        topBar = {
            ShellBackBar(
                title = stringResource(R.string.logdetail_header),
                onBack = onNavigateBack,
                trailing = if (log != null) {
                    {
                        IconButton(onClick = { onEvent(LogDetailEvent.EditClicked) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.common_edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { onEvent(LogDetailEvent.DeleteDialogToggled(true)) },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.common_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                } else null,
            )
        },
        bottomBar = {
            if (log != null && log.type == LogType.QUEST) {
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    Rule()
                    Button(
                        shape = MaterialTheme.shapes.large,
                        onClick = { onEvent(LogDetailEvent.CompletionToggled) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.screen)
                            .height(52.dp),
                        colors = if (log.isCompleted) {
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                    ) {
                        Text(
                            stringResource(
                                if (log.isCompleted) R.string.logdetail_mark_incomplete
                                else R.string.logdetail_mark_complete
                            ),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
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

            log == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.logdetail_not_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.screen)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Spacing.sm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.statType != null) {
                        StatChip(
                            label = stringResource(log.statType.nameRes()),
                            color = log.statType.color(),
                            icon = log.statType.icon(),
                        )
                        Spacer(Modifier.width(Spacing.sm))
                    }
                    if (log.difficulty != null) {
                        OutlineChip(label = stringResource(log.difficulty.nameRes()))
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = log.title,
                    style = ContentHero,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (log.isCompleted) TextDecoration.LineThrough else null,
                )

                if (log.description.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.lg))
                    Text(
                        text = log.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (log.statType != null && log.difficulty != null) {
                    Spacer(Modifier.height(Spacing.xl))
                    GlassPanel(
                        accent = if (log.isCompleted) null
                        else MaterialTheme.colorScheme.primary,
                        containerColor = wellColor(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Eyebrow(
                            text = stringResource(R.string.logdetail_rewards),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(
                                icon = Icons.Default.AutoAwesome,
                                color = MaterialTheme.colorScheme.primary,
                                size = 40.dp,
                            )
                            Spacer(Modifier.width(Spacing.md))
                            Column {
                                Text(
                                    "+" + log.difficulty.baseXp + " XP",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    stringResource(log.statType.nameRes()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = log.statType.color(),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                val rows = buildList {
                    if (log.difficulty != null) {
                        add(
                            DetailEntry(
                                stringResource(R.string.logdetail_difficulty),
                                stringResource(log.difficulty.nameRes()),
                            )
                        )
                    }
                    if (log.priority != null) {
                        add(
                            DetailEntry(
                                stringResource(R.string.logdetail_priority),
                                stringResource(log.priority.labelRes()),
                                when (log.priority) {
                                    Priority.LOW -> MaterialTheme.extendedColors.priorityLow
                                    Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
                                    Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
                                },
                            )
                        )
                    }
                    log.dueAt?.let {
                        add(DetailEntry(stringResource(R.string.logdetail_due), it.formatted()))
                    }
                    log.remindAt?.let {
                        add(
                            DetailEntry(
                                stringResource(R.string.logdetail_reminder),
                                it.formattedWithTime(),
                            )
                        )
                    }
                    add(
                        DetailEntry(
                            stringResource(R.string.logdetail_created),
                            log.createdAt.formatted(),
                        )
                    )
                }

                Eyebrow(stringResource(R.string.logdetail_section_details))
                Spacer(Modifier.height(Spacing.md))

                GlassPanel(
                    containerColor = wellColor(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rows.forEachIndexed { index, entry ->
                        if (index > 0) {
                            Spacer(Modifier.height(Spacing.md))
                            Rule()
                            Spacer(Modifier.height(Spacing.md))
                        }
                        DetailRow(entry.label, entry.value, entry.color)
                    }
                }

                val proofPhoto = log.proofPhotoLocalPath ?: log.proofPhotoUrl
                val hasProof = log.proofLevel != ProofLevel.NONE ||
                        proofPhoto != null ||
                        !log.proofNote.isNullOrBlank()

                if (hasProof) {
                    Spacer(Modifier.height(Spacing.xl))
                    Eyebrow(stringResource(R.string.logdetail_proof))
                    Spacer(Modifier.height(Spacing.md))

                    GlassPanel(
                        containerColor = wellColor(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (log.proofLevel != ProofLevel.NONE) {
                            Text(
                                "${stringResource(log.proofLevel.nameRes())} · +%${
                                    ((log.proofLevel.multiplier - 1) * 100).roundToInt()
                                }",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        proofPhoto?.let { source ->
                            Spacer(Modifier.height(Spacing.md))
                            AsyncImage(
                                model = source,
                                contentDescription =
                                    stringResource(R.string.logdetail_proof_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(MaterialTheme.shapes.small),
                            )
                            if (log.proofPhotoUrl == null) {
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    stringResource(R.string.logdetail_photo_pending),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (!log.proofNote.isNullOrBlank()) {
                            Spacer(Modifier.height(Spacing.md))
                            Eyebrow(stringResource(R.string.logdetail_proof_note))
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                log.proofNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }

    if (state.showProofSheet && state.log != null) {
        ProofSheet(
            logId = state.log.id,
            questTitle = state.log.title,
            onDismiss = { onEvent(LogDetailEvent.ProofSheetDismissed) },
            onConfirm = { draft ->
                onEvent(LogDetailEvent.ProofConfirmed(draft.note, draft.photoLocalPath))
            },
        )
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(LogDetailEvent.DeleteDialogToggled(false)) },
            title = { Text(stringResource(R.string.logdetail_delete_title)) },
            text = { Text(stringResource(R.string.logdetail_delete_body)) },
            confirmButton = {
                TextButton(onClick = { onEvent(LogDetailEvent.DeleteConfirmed) }) {
                    Text(
                        stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LogDetailEvent.DeleteDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

private data class DetailEntry(
    val label: String,
    val value: String,
    val color: Color? = null,
)
