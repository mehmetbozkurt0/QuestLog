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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.feature.questlog.component.formatted
import com.mehmetbozkurt.questlog.feature.questlog.component.label
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LogDetailRoute(
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LogDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LogDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                LogDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    LogDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    state: LogDetailState,
    onEvent: (LogDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val log = state.log

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (log != null) {
                        IconButton(onClick = { onEvent(LogDetailEvent.EditClicked) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { onEvent(LogDetailEvent.DeleteDialogToggled(true)) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
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
                    "Kayıt bulunamadı",
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
                val typeColor = when (log.type) {
                    LogType.QUEST -> MaterialTheme.extendedColors.typeQuest
                    LogType.NPC -> MaterialTheme.extendedColors.typeNpc
                    LogType.LORE -> MaterialTheme.extendedColors.typeLore
                    LogType.SESSION_NOTE -> MaterialTheme.extendedColors.typeSession
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(typeColor, CircleShape))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        log.type.label(),
                        style = MaterialTheme.typography.labelLarge,
                        color = typeColor,
                    )
                }

                Spacer(Modifier.height(Spacing.md))

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

                if (log.priority != null) {
                    val priorityColor = when (log.priority) {
                        Priority.LOW -> MaterialTheme.extendedColors.priorityLow
                        Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
                        Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
                    }
                    DetailRow("Öncelik", log.priority.label(), priorityColor)
                }

                log.dueAt?.let {
                    DetailRow("Son teslim", it.formatted())
                }

                log.remindAt?.let {
                    DetailRow("Hatırlatma", it.formatted())
                }

                DetailRow("Oluşturulma", log.createdAt.formatted())

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
                            if (log.isCompleted) "Tamamlanmadı olarak işaretle"
                            else "Tamamlandı olarak işaretle",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(LogDetailEvent.DeleteDialogToggled(false)) },
            title = { Text("Kaydı sil") },
            text = { Text("Bu kayıt kalıcı olarak silinecek. Emin misin?") },
            confirmButton = {
                TextButton(onClick = { onEvent(LogDetailEvent.DeleteConfirmed) }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LogDetailEvent.DeleteDialogToggled(false)) }) {
                    Text("Vazgeç")
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