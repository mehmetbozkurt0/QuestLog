package com.mehmetbozkurt.questlog.feature.logedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.pips
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.ProofLevel
import com.mehmetbozkurt.questlog.domain.model.StatType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.mehmetbozkurt.questlog.core.common.resolve
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionEyebrow
import com.mehmetbozkurt.questlog.core.common.descriptionRes
import com.mehmetbozkurt.questlog.core.common.hintRes
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.feature.questlog.component.formatted
import com.mehmetbozkurt.questlog.feature.questlog.component.labelRes
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LogEditRoute(
    onNavigateBack: () -> Unit,
    viewModel: LogEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LogEditEffect.NavigateBack -> onNavigateBack()
                is LogEditEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                        stringResource(
                            if (state.isEditMode) R.string.logedit_title_edit
                            else R.string.logedit_title_new
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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

//            Text("Kayıt Türü", style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant)
//            Spacer(Modifier.height(Spacing.sm))
//
//            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
//                LogType.entries.forEachIndexed { index, type ->
//                    SegmentedButton(
//                        selected = state.type == type,
//                        onClick = { onEvent(LogEditEvent.TypeChanged(type)) },
//                        shape = SegmentedButtonDefaults.itemShape(index, LogType.entries.size),
//                        enabled = !state.isSaving,
//                    ) {
//                        Text(type.label(), style = MaterialTheme.typography.labelMedium)
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(LogEditEvent.TitleChanged(it)) },
                label = { Text(stringResource(R.string.logedit_field_title)) },
                singleLine = true,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.lg))

            Text(
                stringResource(R.string.logedit_stat_prompt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatType.entries.forEach { stat ->
                    FilterChip(
                        selected = state.statType == stat,
                        onClick = { onEvent(LogEditEvent.StatTypeChanged(stat)) },
                        enabled = !state.isSaving,
                        label = { Text(stringResource(stat.nameRes())) },
                        leadingIcon = {
                            Icon(
                                imageVector = stat.icon(),
                                contentDescription = null,
                                tint = stat.colorHex().toComposeColor(),
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
            }

            if (state.statType != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(state.statType.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Text(
                stringResource(R.string.logedit_difficulty),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Difficulty.entries.forEachIndexed { index, diff ->
                    SegmentedButton(
                        selected = state.difficulty == diff,
                        onClick = { onEvent(LogEditEvent.DifficultyChanged(diff)) },
                        shape = SegmentedButtonDefaults.itemShape(index, Difficulty.entries.size),
                        enabled = !state.isSaving,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(diff.nameRes()),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                diff.pips(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xs))
            Text(
                stringResource(
                    R.string.logedit_difficulty_hint,
                    stringResource(state.difficulty.hintRes()),
                    state.difficulty.baseXp,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(LogEditEvent.DescriptionChanged(it)) },
                label = { Text(stringResource(R.string.logedit_field_description)) },
                enabled = !state.isSaving,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.showQuestFields) {
                Spacer(Modifier.height(Spacing.lg))

                Text(
                    stringResource(R.string.logedit_priority),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Priority.entries.forEachIndexed { index, p ->
                        SegmentedButton(
                            selected = state.priority == p,
                            onClick = { onEvent(LogEditEvent.PriorityChanged(p)) },
                            shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                            enabled = !state.isSaving,
                        ) {
                            Text(
                                stringResource(p.labelRes()),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                SectionEyebrow(stringResource(R.string.logedit_section_timing))

                Spacer(Modifier.height(Spacing.md))

                DateField(
                    label = stringResource(R.string.logedit_due_date),
                    value = state.dueAt,
                    enabled = !state.isSaving,
                    onPick = { onEvent(LogEditEvent.DuePickerToggled(true)) },
                    onClear = { onEvent(LogEditEvent.DueAtChanged(null)) },
                )

                Spacer(Modifier.height(Spacing.md))

                DateField(
                    label = stringResource(R.string.logedit_reminder),
                    value = state.remindAt,
                    enabled = !state.isSaving,
                    withTime = true,
                    onPick = { onEvent(LogEditEvent.RemindPickerToggled(true)) },
                    onClear = { onEvent(LogEditEvent.RemindAtChanged(null)) },
                )

                Spacer(Modifier.height(Spacing.md))

                QuestCard(
                    seed = 17,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.logedit_proof_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                    Text(
                        stringResource(R.string.common_save),
                        style = MaterialTheme.typography.labelLarge,
                    )
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
        LogDateTimePicker(
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
    withTime: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = onPick,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = value?.let {
                    val shown = if (withTime) it.formattedWithTime() else it.formatted()
                    stringResource(R.string.logedit_picker_value, label, shown)
                } ?: stringResource(R.string.logedit_picker_empty, label),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (value != null) {
            IconButton(onClick = onClear, enabled = enabled) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = stringResource(R.string.common_clear),
                )
            }
        }
    }
}

@Composable
fun Instant.formattedWithTime(): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale)
    }
    return formatter.format(this.atZone(ZoneId.systemDefault()))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDateTimePicker(
    initial: Instant?,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val initialLocal = (initial ?: Instant.now().plusSeconds(3600)).atZone(zone)

    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    if (pickedDateMillis == null) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialLocal.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    pickedDateMillis = pickerState.selectedDateMillis
                }) { Text(stringResource(R.string.common_next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_dismiss)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    } else {
        val timeState = rememberTimePickerState(
            initialHour = initialLocal.hour,
            initialMinute = initialLocal.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    stringResource(R.string.logedit_time_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val date = Instant.ofEpochMilli(pickedDateMillis!!)
                        .atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(
                        date.atTime(timeState.hour, timeState.minute)
                            .atZone(zone).toInstant()
                    )
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pickedDateMillis = null }) {
                    Text(stringResource(R.string.common_back))
                }
            },
        )
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
            }) { Text(stringResource(R.string.common_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_dismiss)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
