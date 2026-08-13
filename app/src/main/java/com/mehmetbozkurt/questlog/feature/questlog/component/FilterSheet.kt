package com.mehmetbozkurt.questlog.feature.questlog.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.LogType
import com.mehmetbozkurt.questlog.domain.model.Priority
import com.mehmetbozkurt.questlog.feature.questlog.CompletionFilter
import com.mehmetbozkurt.questlog.feature.questlog.SortOption
import com.mehmetbozkurt.questlog.feature.questlog.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    completionFilter: CompletionFilter,
    typeFilter: LogType?,
    priorityFilter: Priority?,
    sortOption: SortOption,
    onCompletionChange: (CompletionFilter) -> Unit,
    onTypeChange: (LogType?) -> Unit,
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
                    "Filtrele & Sırala",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = onClear) { Text("Temizle") }
            }

            Spacer(Modifier.height(Spacing.md))

            SectionLabel("Durum")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CompletionFilter.entries.forEach { option ->
                    FilterChip(
                        selected = completionFilter == option,
                        onClick = { onCompletionChange(option) },
                        label = { Text(option.label()) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel("Tür")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { onTypeChange(null) },
                    label = { Text("Tümü") },
                )
                LogType.entries.forEach { type ->
                    FilterChip(
                        selected = typeFilter == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(type.label()) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel("Öncelik")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = priorityFilter == null,
                    onClick = { onPriorityChange(null) },
                    label = { Text("Tümü") },
                )
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priorityFilter == p,
                        onClick = { onPriorityChange(p) },
                        label = { Text(p.label()) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            SectionLabel("Sıralama")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { onSortChange(option) },
                        label = { Text(option.label()) },
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