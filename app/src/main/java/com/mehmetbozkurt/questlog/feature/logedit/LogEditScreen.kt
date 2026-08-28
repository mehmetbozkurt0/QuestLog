package com.mehmetbozkurt.questlog.feature.logedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.rimColor
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.descriptionRes
import com.mehmetbozkurt.questlog.core.common.hintRes
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.core.common.resolve
import com.mehmetbozkurt.questlog.core.common.shortLabelRes
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.component.Eyebrow
import com.mehmetbozkurt.questlog.core.designsystem.component.Rule
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.color
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.domain.model.Difficulty
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.feature.questlog.component.formatted
import com.mehmetbozkurt.questlog.feature.questlog.component.labelRes
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                if (state.isEditMode) R.string.logedit_title_edit
                                else R.string.logedit_title_new
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
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
                Rule()
            }
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                Rule()
                Button(
                    shape = MaterialTheme.shapes.large,
                    onClick = { onEvent(LogEditEvent.SaveClicked) },
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.screen)
                        .height(52.dp),
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
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.screen)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            Spacer(Modifier.height(Spacing.lg))

            GlassPanel(containerColor = wellColor(), modifier = Modifier.fillMaxWidth()) {
                Eyebrow(stringResource(R.string.logedit_field_title))
                Spacer(Modifier.height(Spacing.sm))
                AuraTextField(
                    value = state.title,
                    onValueChange = { onEvent(LogEditEvent.TitleChanged(it)) },
                    enabled = !state.isSaving,
                    singleLine = true,
                )

                Spacer(Modifier.height(Spacing.lg))

                Eyebrow(stringResource(R.string.logedit_field_description))
                Spacer(Modifier.height(Spacing.sm))
                AuraTextField(
                    value = state.description,
                    onValueChange = { onEvent(LogEditEvent.DescriptionChanged(it)) },
                    enabled = !state.isSaving,
                    minLines = 3,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Eyebrow(stringResource(R.string.logedit_stat_prompt))
            Spacer(Modifier.height(Spacing.md))

            StatGrid(
                selected = state.statType,
                enabled = !state.isSaving,
                onSelect = { onEvent(LogEditEvent.StatTypeChanged(it)) },
            )

            when {
                state.statType != null -> {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        stringResource(state.statType.descriptionRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                state.isHabit -> {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        stringResource(R.string.logedit_habit_stat_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            GlassPanel(containerColor = wellColor(), modifier = Modifier.fillMaxWidth()) {
                Eyebrow(stringResource(R.string.logedit_difficulty))
                Spacer(Modifier.height(Spacing.md))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Difficulty.entries.forEach { diff ->
                        val selected = state.difficulty == diff
                        FilterChip(
                            selected = selected,
                            onClick = { onEvent(LogEditEvent.DifficultyChanged(diff)) },
                            enabled = !state.isSaving,
                            label = {
                                Text(
                                    stringResource(diff.nameRes()),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                selectedContainerColor =
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    stringResource(
                        R.string.logedit_difficulty_hint,
                        stringResource(state.difficulty.hintRes()),
                        state.difficulty.baseXp,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.showQuestFields) {
                Spacer(Modifier.height(Spacing.lg))

                GlassPanel(containerColor = wellColor(), modifier = Modifier.fillMaxWidth()) {
                    Eyebrow(stringResource(R.string.logedit_priority))
                    Spacer(Modifier.height(Spacing.md))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Priority.entries.forEach { p ->
                            val selected = state.priority == p
                            FilterChip(
                                selected = selected,
                                onClick = { onEvent(LogEditEvent.PriorityChanged(p)) },
                                enabled = !state.isSaving,
                                label = {
                                    Text(
                                        stringResource(p.labelRes()),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    selectedContainerColor =
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                GlassPanel(containerColor = wellColor(), modifier = Modifier.fillMaxWidth()) {
                    Eyebrow(stringResource(R.string.logedit_section_timing))

                    Spacer(Modifier.height(Spacing.md))

                    DateField(
                        label = stringResource(R.string.logedit_due_date),
                        icon = Icons.Default.CalendarMonth,
                        value = state.dueAt,
                        enabled = !state.isSaving,
                        onPick = { onEvent(LogEditEvent.DuePickerToggled(true)) },
                        onClear = { onEvent(LogEditEvent.DueAtChanged(null)) },
                    )

                    Spacer(Modifier.height(Spacing.md))

                    Rule()

                    Spacer(Modifier.height(Spacing.md))

                    DateField(
                        label = stringResource(R.string.logedit_reminder),
                        icon = Icons.Default.NotificationsActive,
                        value = state.remindAt,
                        enabled = !state.isSaving,
                        withTime = true,
                        onPick = { onEvent(LogEditEvent.RemindPickerToggled(true)) },
                        onClear = { onEvent(LogEditEvent.RemindAtChanged(null)) },
                    )

                    Spacer(Modifier.height(Spacing.md))

                    Text(
                        stringResource(R.string.logedit_proof_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = MaterialTheme.typography.titleMedium,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StatGrid(
    selected: StatType?,
    enabled: Boolean,
    onSelect: (StatType) -> Unit,
) {
    val stats = StatType.entries
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        stats.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { stat ->
                    StatTile(
                        stat = stat,
                        selected = selected == stat,
                        enabled = enabled,
                        onClick = { onSelect(stat) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    stat: StatType,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statColor = stat.color()
    val shape = MaterialTheme.shapes.medium
    val background: Color = if (selected) statColor.copy(alpha = 0.14f)
    else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) statColor
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .height(86.dp)
            .background(background, shape)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = stat.icon(),
                contentDescription = null,
                tint = if (selected) statColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(stat.shortLabelRes()).uppercaseLocalized(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) statColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DateField(
    label: String,
    icon: ImageVector,
    value: Instant?,
    enabled: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
    withTime: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(
            icon = icon,
            color = MaterialTheme.colorScheme.primary,
            size = 40.dp,
            iconSize = 20.dp,
        )

        Spacer(Modifier.width(Spacing.md))

        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.logedit_picker_empty, label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(Spacing.sm))

        Row(
            Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, rimColor(), MaterialTheme.shapes.large)
                .clickable(enabled = enabled, onClick = onPick)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DataValue(
                text = value?.let {
                    if (withTime) it.formattedWithTime() else it.formatted()
                } ?: "--/--/----",
                color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
            )
        }

        if (value != null) {
            IconButton(onClick = onClear, enabled = enabled) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = stringResource(R.string.common_clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
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
            containerColor = MaterialTheme.colorScheme.surface,
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
