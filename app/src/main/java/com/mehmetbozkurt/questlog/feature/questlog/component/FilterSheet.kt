package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.icon
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.domain.model.StatType
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.nameRes
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.feature.questlog.CompletionFilter
import com.mehmetbozkurt.questlog.feature.questlog.SortOption
import com.mehmetbozkurt.questlog.feature.questlog.labelRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    completionFilter: CompletionFilter,
    statFilter: StatType?,
    priorityFilter: Priority?,
    sortOption: SortOption,
    onCompletionChange: (CompletionFilter) -> Unit,
    onStatChange: (StatType?) -> Unit,
    onPriorityChange: (Priority?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.filter_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = onClear) { Text(stringResource(R.string.common_clear)) }
            }

            Spacer(Modifier.height(Spacing.md))

            SectionLabel(stringResource(R.string.filter_section_status))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CompletionFilter.entries.forEach { option ->
                    FilterChip(
                        selected = completionFilter == option,
                        onClick = { onCompletionChange(option) },
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel(stringResource(R.string.filter_section_stat))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = statFilter == null,
                    onClick = { onStatChange(null) },
                    label = { Text(stringResource(R.string.common_all)) },
                )
                StatType.entries.forEach { stat ->
                    FilterChip(
                        selected = statFilter == stat,
                        onClick = { onStatChange(stat) },
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

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel(stringResource(R.string.filter_section_priority))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = priorityFilter == null,
                    onClick = { onPriorityChange(null) },
                    label = { Text(stringResource(R.string.common_all)) },
                )
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priorityFilter == p,
                        onClick = { onPriorityChange(p) },
                        label = { Text(stringResource(p.labelRes())) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel(stringResource(R.string.filter_section_sort))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { onSortChange(option) },
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = Spacing.sm),
    )
}