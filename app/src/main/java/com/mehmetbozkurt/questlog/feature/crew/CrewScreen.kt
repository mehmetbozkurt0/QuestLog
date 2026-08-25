package com.mehmetbozkurt.questlog.feature.crew

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.resolve
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionEyebrow
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.feature.crew.component.CrewChatPane
import com.mehmetbozkurt.questlog.feature.crew.component.CrewMemberRow
import com.mehmetbozkurt.questlog.feature.crew.component.FeedEntryCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CrewRoute(
    startOnChat: Boolean = false,
    onStartOnChatHandled: () -> Unit = {},
    viewModel: CrewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(startOnChat) {
        if (!startOnChat) return@LaunchedEffect
        viewModel.onEvent(CrewEvent.TabSelected(CrewTab.CHAT))
        onStartOnChatHandled()
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CrewEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.text.resolve(context))
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
                        stringResource(R.string.crew_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    if (state.inCrew) {
                        IconButton(onClick = { onEvent(CrewEvent.LeaveDialogToggled(true)) }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.crew_leave_action))
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
                    title = stringResource(R.string.crew_empty_title),
                    body = stringResource(R.string.crew_empty_body),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = { onEvent(CrewEvent.JoinDialogToggled(true)) },
                        enabled = !state.isWorking,
                    ) {
                        Text(
                            stringResource(R.string.crew_join_with_code),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Button(
                        onClick = { onEvent(CrewEvent.CreateDialogToggled(true)) },
                        enabled = !state.isWorking,
                    ) {
                        Text(
                            stringResource(R.string.crew_create),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                TabRow(
                    selectedTabIndex = state.tab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    CrewTab.entries.forEach { tab ->
                        Tab(
                            selected = state.tab == tab,
                            onClick = { onEvent(CrewEvent.TabSelected(tab)) },
                            text = {
                                val unread =
                                    if (tab == CrewTab.CHAT) state.unreadMessages else 0
                                BadgedBox(
                                    badge = {
                                        if (unread > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError,
                                            ) {
                                                Text(unread.badgeLabel())
                                            }
                                        }
                                    },
                                ) {
                                    Text(
                                        stringResource(tab.labelRes()),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            },
                        )
                    }
                }

                when (state.tab) {
                    CrewTab.CHAT -> CrewChatPane(
                        messages = state.messages,
                        ownUserId = state.ownUserId,
                        input = state.messageInput,
                        canSend = state.canSendMessage,
                        onInputChange = { onEvent(CrewEvent.MessageInputChanged(it)) },
                        onSend = { onEvent(CrewEvent.MessageSent) },
                        onVisibilityChanged = {
                            onEvent(CrewEvent.ChatVisibilityChanged(it))
                        },
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize(),
                    )

                    CrewTab.FEED -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.lg,
                            end = Spacing.lg,
                            top = Spacing.md,
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
                                        Spacer(Modifier.height(Spacing.sm))
                                        Text(
                                            stringResource(R.string.crew_invite_code_field).uppercaseLocalized(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(Modifier.height(Spacing.xs))
                                        InviteCodeStamp(state.crew?.inviteCode.orEmpty())
                                    }
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.crew_copy_code),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        item(key = "members_header") {
                            SectionEyebrow(
                                stringResource(R.string.crew_members_header),
                                trailing = "${state.members.size}",
                            )
                        }

                        items(state.members, key = { "m_${it.userId}" }) { member ->
                            CrewMemberRow(
                                member = member,
                                isSelf = member.userId == state.ownUserId,
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item(key = "feed_header") {
                            SectionEyebrow(
                                stringResource(R.string.crew_feed_header),
                                trailing = if (state.hasMentorFeat)
                                    stringResource(R.string.crew_approvals_left, state.approvalsLeft)
                                else null,
                            )
                        }

                        if (state.feed.isEmpty()) {
                            item(key = "feed_empty") {
                                Text(
                                    stringResource(R.string.crew_feed_empty),
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
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.CreateDialogToggled(false)) },
            title = {
                Text(
                    stringResource(R.string.crew_create),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.crew_create_dialog_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = state.crewNameInput,
                        onValueChange = { onEvent(CrewEvent.CrewNameChanged(it)) },
                        label = { Text(stringResource(R.string.crew_name_label)) },
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
                    Text(stringResource(R.string.crew_create_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.CreateDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.showJoinDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.JoinDialogToggled(false)) },
            title = {
                Text(
                    stringResource(R.string.crew_join_with_code),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.crew_join_dialog_body,
                            CrewRules.INVITE_CODE_LENGTH,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = state.joinCodeInput,
                        onValueChange = { onEvent(CrewEvent.JoinCodeChanged(it)) },
                        label = { Text(stringResource(R.string.crew_invite_code_field)) },
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
                    Text(stringResource(R.string.crew_join_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.JoinDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.LeaveDialogToggled(false)) },
            title = {
                Text(
                    stringResource(R.string.crew_leave_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    stringResource(R.string.crew_leave_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(CrewEvent.LeaveConfirmed) }) {
                    Text(
                        stringResource(R.string.crew_leave_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.LeaveDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

internal fun Int.badgeLabel(): String = if (this > 99) "99+" else "$this"

private fun CrewTab.labelRes(): Int = when (this) {
    CrewTab.FEED -> R.string.crew_tab_feed
    CrewTab.CHAT -> R.string.crew_tab_chat
}

@Composable
private fun InviteCodeStamp(code: String) {
    val border = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val radius = MaterialTheme.shapes.small

    Box(
        modifier = Modifier
            .drawBehind {
                drawRoundRect(
                    color = border,
                    cornerRadius = CornerRadius(5.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                        ),
                    ),
                )
            }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 6.sp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
