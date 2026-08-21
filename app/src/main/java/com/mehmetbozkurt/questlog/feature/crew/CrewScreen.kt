package com.mehmetbozkurt.questlog.feature.crew

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionRule
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.feature.crew.component.CrewMemberRow
import com.mehmetbozkurt.questlog.feature.crew.component.FeedEntryCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CrewRoute(
    viewModel: CrewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CrewEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                is CrewEffect.CopyToClipboard ->
                    clipboard.setText(AnnotatedString(effect.text))
            }
        }
    }

    CrewScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewScreen(
    state: CrewState,
    onEvent: (CrewEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ekip",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    if (state.inCrew) {
                        IconButton(onClick = { onEvent(CrewEvent.LeaveDialogToggled(true)) }) {
                            Icon(Icons.Default.Logout, contentDescription = "Ekipten ayrıl")
                        }
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

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            !state.inCrew -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmptyState(
                    icon = Icons.Default.Groups,
                    title = "Yalnız yolculuk zordur",
                    body = "Bir ekip kur ya da arkadaşının davet koduyla katıl. " +
                            "Ekip arkadaşlarının görevlerini görür, Mentor yeteneğiyle " +
                            "onaylayıp ikinize de XP kazandırırsın.",
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = { onEvent(CrewEvent.JoinDialogToggled(true)) },
                        enabled = !state.isWorking,
                    ) {
                        Text("Kodla Katıl", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { onEvent(CrewEvent.CreateDialogToggled(true)) },
                        enabled = !state.isWorking,
                    ) {
                        Text("Ekip Kur", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = padding.calculateTopPadding() + Spacing.sm,
                    bottom = padding.calculateBottomPadding() + Spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "invite") {
                    QuestCard(
                        onClick = { onEvent(CrewEvent.InviteCodeCopied) },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        seed = 21,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    state.crew?.name.orEmpty(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "Davet kodu: ${state.crew?.inviteCode.orEmpty()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Kodu kopyala",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                item(key = "members_header") {
                    CrewSectionHeader("Üyeler", "${state.members.size}")
                }

                items(state.members, key = { "m_${it.userId}" }) { member ->
                    CrewMemberRow(
                        member = member,
                        isSelf = member.userId == state.ownUserId,
                        modifier = Modifier.animateItem(),
                    )
                }

                item(key = "feed_header") {
                    CrewSectionHeader(
                        "Son Görevler",
                        if (state.hasMentorFeat) "${state.approvalsLeft} onay hakkı" else null,
                    )
                }

                if (state.feed.isEmpty()) {
                    item(key = "feed_empty") {
                        Text(
                            "Henüz kimse görev tamamlamadı. İlk sen ol.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                        )
                    }
                } else {
                    items(state.feed, key = { "f_${it.id}" }) { item ->
                        FeedEntryCard(
                            item = item,
                            isMine = item.authorId == state.ownUserId,
                            canApprove = state.canApprove(item) && !state.isWorking,
                            approvedByMe = state.ownUserId in item.approvedBy,
                            onApprove = { onEvent(CrewEvent.ApproveClicked(item.id)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.CreateDialogToggled(false)) },
            title = { Text("Ekip Kur", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        "Ekibine bir ad ver. Kurduktan sonra davet kodunu arkadaşlarınla paylaş.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = state.crewNameInput,
                        onValueChange = { onEvent(CrewEvent.CrewNameChanged(it)) },
                        label = { Text("Ekip adı") },
                        singleLine = true,
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(CrewEvent.CreateConfirmed) },
                    enabled = state.canCreate,
                ) {
                    Text("Kur")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.CreateDialogToggled(false)) }) {
                    Text("Vazgeç")
                }
            },
        )
    }

    if (state.showJoinDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.JoinDialogToggled(false)) },
            title = { Text("Kodla Katıl", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        "Arkadaşının paylaştığı ${CrewRules.INVITE_CODE_LENGTH} haneli davet kodunu gir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = state.joinCodeInput,
                        onValueChange = { onEvent(CrewEvent.JoinCodeChanged(it)) },
                        label = { Text("Davet kodu") },
                        singleLine = true,
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(CrewEvent.JoinConfirmed) },
                    enabled = state.canJoin,
                ) {
                    Text("Katıl")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.JoinDialogToggled(false)) }) {
                    Text("Vazgeç")
                }
            },
        )
    }

    if (state.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.LeaveDialogToggled(false)) },
            title = { Text("Ekipten ayrıl", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Ekipten ayrılırsan üyeler seni göremez, akışın silinir. " +
                            "Kazandığın XP ve seviyen sende kalır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(CrewEvent.LeaveConfirmed) }) {
                    Text("Ayrıl", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.LeaveDialogToggled(false)) }) {
                    Text("Vazgeç")
                }
            },
        )
    }
}

@Composable
private fun CrewSectionHeader(title: String, trailing: String?) {
    Row(
        Modifier.fillMaxWidth().padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(Spacing.md))
        SectionRule(Modifier.weight(1f))
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.md))
            Text(
                trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
