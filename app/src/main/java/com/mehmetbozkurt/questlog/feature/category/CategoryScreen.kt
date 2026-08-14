package com.mehmetbozkurt.questlog.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.model.Category
import kotlinx.coroutines.flow.collectLatest

fun String.toComposeColor(): Color = Color(this.toColorInt())

@Composable
fun CategoryRoute(
    onNavigateBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CategoryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    CategoryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    state: CategoryState,
    onEvent: (CategoryEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kategoriler",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(CategoryEvent.AddDialogToggled(true)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni kategori")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.categories.isEmpty() && !state.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Henüz kategori yok. + ile ekle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = padding.calculateTopPadding() + Spacing.sm,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(state.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onDelete = { onEvent(CategoryEvent.DeleteRequested(category)) },
                    )
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddCategoryDialog(
            name = state.newName,
            colorHex = state.newColorHex,
            canAdd = state.canAdd,
            isDuplicate = state.isDuplicate,
            onNameChange = { onEvent(CategoryEvent.NameChanged(it)) },
            onColorChange = { onEvent(CategoryEvent.ColorChanged(it)) },
            onConfirm = { onEvent(CategoryEvent.AddConfirmed) },
            onDismiss = { onEvent(CategoryEvent.AddDialogToggled(false)) },
        )
    }

    state.pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { onEvent(CategoryEvent.DeleteRequested(null)) },
            title = { Text("Kategoriyi sil") },
            text = {
                Text("\"${target.name}\" silinecek. Bu kategoriye bağlı kayıtlar kategorisiz kalacak.")
            },
            confirmButton = {
                TextButton(onClick = { onEvent(CategoryEvent.DeleteConfirmed) }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CategoryEvent.DeleteRequested(null)) }) {
                    Text("Vazgeç")
                }
            },
        )
    }
}

@Composable
private fun CategoryRow(category: Category, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(16.dp).background(category.colorHex.toComposeColor(), CircleShape)
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    name: String,
    colorHex: String,
    canAdd: Boolean,
    isDuplicate: Boolean,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Kategori") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Kategori adı") },
                    singleLine = true,
                    isError = isDuplicate,
                    supportingText = {
                        if (isDuplicate) Text("Bu isimde bir kategori zaten var")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.md))

                Text(
                    "Renk",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    CATEGORY_COLORS.forEach { hex ->
                        val selected = hex == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(hex.toComposeColor(), CircleShape)
                                .then(
                                    if (selected) Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape,
                                    ) else Modifier
                                )
                                .clickable { onColorChange(hex) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canAdd) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        },
    )
}