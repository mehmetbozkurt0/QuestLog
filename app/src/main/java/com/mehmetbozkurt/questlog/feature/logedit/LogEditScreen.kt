package com.mehmetbozkurt.questlog.feature.logedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.feature.questlog.component.formatted
import com.mehmetbozkurt.questlog.feature.questlog.component.label
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant

@Composable
fun LogEditRoute(
    onNavigateBack: () -> Unit,
    viewModel: LogEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LogEditEffect.NavigateBack -> onNavigateBack()
                is LogEditEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LogEditScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEditScreen(
    state: LogEditState,
    onEvent: (LogEditEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Kaydı Düzenle" else "Yeni Kayıt",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            Spacer(Modifier.height(Spacing.sm))

            Text("Kayıt Türü", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                LogType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { onEvent(LogEditEvent.TypeChanged(type)) },
                        shape = SegmentedButtonDefaults.itemShape(index, LogType.entries.size),
                        enabled = !state.isSaving,
                    ) {
                        Text(type.label(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(LogEditEvent.TitleChanged(it)) },
                label = { Text("Başlık") },
                singleLine = true,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(LogEditEvent.DescriptionChanged(it)) },
                label = { Text("Açıklama") },
                enabled = !state.isSaving,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.showQuestFields) {
                Spacer(Modifier.height(Spacing.lg))

                Text("Öncelik", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.sm))

                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Priority.entries.forEachIndexed { index, p ->
                        SegmentedButton(
                            selected = state.priority == p,
                            onClick = { onEvent(LogEditEvent.PriorityChanged(p)) },
                            shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                            enabled = !state.isSaving,
                        ) {
                            Text(p.label(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                DateField(
                    label = "Son teslim tarihi",
                    value = state.dueAt,
                    enabled = !state.isSaving,
                    onPick = { onEvent(LogEditEvent.DuePickerToggled(true)) },
                    onClear = { onEvent(LogEditEvent.DueAtChanged(null)) },
                )

                Spacer(Modifier.height(Spacing.md))

                DateField(
                    label = "Hatırlatma tarihi",
                    value = state.remindAt,
                    enabled = !state.isSaving,
                    onPick = { onEvent(LogEditEvent.RemindPickerToggled(true)) },
                    onClear = { onEvent(LogEditEvent.RemindAtChanged(null)) },
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            Button(
                onClick = { onEvent(LogEditEvent.SaveClicked) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Kaydet", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }

    if (state.showDuePicker) {
        LogDatePicker(
            initial = state.dueAt,
            onDismiss = { onEvent(LogEditEvent.DuePickerToggled(false)) },
            onConfirm = { onEvent(LogEditEvent.DueAtChanged(it)) },
        )
    }

    if (state.showRemindPicker) {
        LogDatePicker(
            initial = state.remindAt,
            onDismiss = { onEvent(LogEditEvent.RemindPickerToggled(false)) },
            onConfirm = { onEvent(LogEditEvent.RemindAtChanged(it)) },
        )
    }
}

@Composable
private fun DateField(
    label: String,
    value: Instant?,
    enabled: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = onPick,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = value?.let { "$label: ${it.formatted()}" } ?: "$label seç",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (value != null) {
            IconButton(onClick = onClear, enabled = enabled) {
                Icon(Icons.Default.Clear, contentDescription = "Temizle")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDatePicker(
    initial: Instant?,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial?.toEpochMilli() ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onConfirm(Instant.ofEpochMilli(it)) }
            }) { Text("Tamam") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}