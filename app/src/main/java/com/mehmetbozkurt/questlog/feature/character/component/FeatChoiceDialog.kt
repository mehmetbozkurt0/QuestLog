package com.mehmetbozkurt.questlog.feature.character.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.toComposeColor
import com.mehmetbozkurt.questlog.domain.model.FeatCatalog
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.model.StatType
import com.mehmetbozkurt.questlog.domain.model.colorHex
import com.mehmetbozkurt.questlog.domain.model.displayName

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatChoiceDialog(
    selectedFeatId: FeatId?,
    selectedStat: StatType?,
    ownedFeatIds: Set<FeatId>,
    canConfirm: Boolean,
    isSaving: Boolean,
    onFeatSelected: (FeatId) -> Unit,
    onStatSelected: (StatType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val needsStat = selectedFeatId
        ?.let { FeatCatalog.byId(it).requiresStatChoice }
        ?: false

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                "Yeni Yetenek Seç",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Seviye atladın. Kalıcı bir yetenek kazanıyorsun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.md))

                FeatCatalog.selectable.forEach { def ->
                    val owned = def.id in ownedFeatIds
                    val selected = def.id == selectedFeatId

                    Card(
                        onClick = { if (!owned && !isSaving) onFeatSelected(def.id) },
                        enabled = !owned && !isSaving,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                    ) {
                        Column(Modifier.padding(Spacing.md)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    def.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                )
                                if (owned) {
                                    Spacer(Modifier.width(Spacing.sm))
                                    Text(
                                        "· sahipsin",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                def.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (needsStat) {
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        "Hangi yetenekte uzmanlaşacaksın?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        StatType.entries.forEach { stat ->
                            FilterChip(
                                selected = selectedStat == stat,
                                onClick = { if (!isSaving) onStatSelected(stat) },
                                enabled = !isSaving,
                                label = { Text(stat.displayName()) },
                                leadingIcon = {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .background(
                                                stat.colorHex().toComposeColor(),
                                                CircleShape
                                            )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canConfirm) {
                Text("Seç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Sonra")
            }
        },
    )
}