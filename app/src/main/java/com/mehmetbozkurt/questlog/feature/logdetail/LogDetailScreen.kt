package com.mehmetbozkurt.questlog.feature.logdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.domain.model.colorHex
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
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (log != null) {
                        IconButton(onClick = { onEvent(LogDetailEvent.EditClicked) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.common_edit))
                        }
                        IconButton(onClick = { onEvent(LogDetailEvent.DeleteDialogToggled(true)) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.common_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
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
                    .padding(horizontal = Spacing.lg)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (log.statType != null) {
                    val statColor = log.statType.colorHex().toComposeColor()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(statColor, CircleShape))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            stringResource(log.statType.nameRes()),
                            style = MaterialTheme.typography.labelLarge,
                            color = statColor,
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))
                }

                Text(
                    text = log.title,
                    style = MaterialTheme.typography.headlineLarge,
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

                Spacer(Modifier.height(Spacing.xl))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(Spacing.lg))

                if (log.difficulty != null) {
                    DetailRow(
                        stringResource(R.string.logdetail_difficulty),
                        stringResource(log.difficulty.nameRes()),
                    )
                }

                if (log.priority != null) {
                    val priorityColor = when (log.priority) {
                        Priority.LOW -> MaterialTheme.extendedColors.priorityLow
                        Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
                        Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
                    }
                    DetailRow(
                        stringResource(R.string.logdetail_priority),
                        stringResource(log.priority.labelRes()),
                        priorityColor,
                    )
                }

                log.dueAt?.let {
                    DetailRow(stringResource(R.string.logdetail_due), it.formatted())
                }

                log.remindAt?.let {
                    DetailRow(stringResource(R.string.logdetail_reminder), it.formattedWithTime())
                }

                DetailRow(stringResource(R.string.logdetail_created), log.createdAt.formatted())

                if (log.proofLevel != ProofLevel.NONE) {
                    DetailRow(
                        stringResource(R.string.logdetail_proof),
                        "${stringResource(log.proofLevel.nameRes())} · +%${
                            ((log.proofLevel.multiplier - 1) * 100).roundToInt()
                        }",
                        MaterialTheme.colorScheme.primary,
                    )
                }

                (log.proofPhotoLocalPath ?: log.proofPhotoUrl)?.let { source ->
                    Spacer(Modifier.height(Spacing.md))
                    AsyncImage(
                        model = source,
                        contentDescription = stringResource(R.string.logdetail_proof_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.medium),
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
                    QuestCard(seed = log.id.hashCode() + 1, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.logdetail_proof_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            log.proofNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (log.type == LogType.QUEST) {
                    Spacer(Modifier.height(Spacing.xl))

                    Button(
                        onClick = { onEvent(LogDetailEvent.CompletionToggled) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}