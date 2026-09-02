package com.mehmetbozkurt.questlog.feature.crew

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.material.icons.filled.Notifications
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.resolve
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.Rule
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.ScreenTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.domain.progression.CrewRules
import com.mehmetbozkurt.questlog.feature.crew.component.CrewChatPane
import com.mehmetbozkurt.questlog.feature.crew.component.CrewMemberRow
import com.mehmetbozkurt.questlog.feature.crew.component.FeedEntryCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CrewRoute(
    onNavigateToMember: (String) -> Unit,
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

                is CrewEffect.ShareInvite -> context.shareInvite(
                    context.getString(
                        R.string.crew_share_text,
                        effect.crewName,
                        effect.code,
                    ),
                    context.getString(R.string.crew_share_chooser),
                )
            }
        }
    }

    CrewScreen(
        onNavigateToMember = onNavigateToMember,
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewScreen(
    onNavigateToMember: (String) -> Unit,
    state: CrewState,
    onEvent: (CrewEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                        shape = MaterialTheme.shapes.large,
                        onClick = { onEvent(CrewEvent.JoinDialogToggled(true)) },
                        enabled = !state.isWorking,
                    ) {
                        Text(
                            stringResource(R.string.crew_join_with_code),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Button(
                        shape = MaterialTheme.shapes.large,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Spacing.screen,
                            end = Spacing.sm,
                            top = Spacing.lg,
                            bottom = Spacing.sm,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScreenTitle(
                        title = stringResource(R.string.crew_title),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onEvent(CrewEvent.LeaveDialogToggled(true)) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.crew_leave_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = state.tab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { Rule() },
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
                                        stringResource(tab.labelRes()).uppercaseLocalized(),
                                        style = MaterialTheme.typography.labelSmall,
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

                    CrewTab.FEED -> PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(CrewEvent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Spacing.screen,
                                end = Spacing.screen,
                                top = Spacing.md,
                                bottom = padding.calculateBottomPadding() + Spacing.xxl,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            item(key = "invite") {
                                GlassPanel(
                                    onClick = { onEvent(CrewEvent.InviteCodeCopied) },
                                    containerColor = wellColor(),
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
                                        IconButton(onClick = { onEvent(CrewEvent.InviteCodeShared) }) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = stringResource(R.string.crew_share),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.crew_copy_code),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    if (state.isOwner) {
                                        Spacer(Modifier.height(Spacing.md))
                                        Rule()
                                        Spacer(Modifier.height(Spacing.sm))
                                        Row {
                                            TextButton(
                                                onClick = {
                                                    onEvent(CrewEvent.RenameDialogToggled(true))
                                                },
                                            ) {
                                                Text(
                                                    stringResource(R.string.crew_rename),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            Spacer(Modifier.width(Spacing.sm))
                                            TextButton(
                                                onClick = {
                                                    onEvent(CrewEvent.RegenerateDialogToggled(true))
                                                },
                                            ) {
                                                Text(
                                                    stringResource(R.string.crew_regenerate),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "members_header") {
                                SectionTitle(
                                    text = stringResource(R.string.crew_members_header),
                                    modifier = Modifier.padding(top = Spacing.sm),
                                    trailing = {
                                        DataValue(
                                            text = stringResource(
                                                R.string.crew_members_count,
                                                state.members.size,
                                                CrewRules.MAX_MEMBERS,
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                )
                            }

                            itemsIndexed(
                                state.members,
                                key = { _, m -> "m_${m.userId}" },
                            ) { index, member ->
                                CrewMemberRow(
                                    member = member,
                                    rank = index + 1,
                                    isSelf = member.userId == state.ownUserId,
                                    isOwner = member.userId == state.crew?.ownerId,
                                    showMenuButton = state.isOwner &&
                                            member.userId != state.ownUserId,
                                    onClick = { onNavigateToMember(member.userId) },
                                    onMenuClick = {
                                        onEvent(CrewEvent.MemberMenuRequested(member))
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }

                            item(key = "feed_header") {
                                SectionTitle(
                                    text = stringResource(R.string.crew_feed_header),
                                    icon = Icons.Default.Notifications,
                                    modifier = Modifier.padding(top = Spacing.lg),
                                    trailing = if (state.hasMentorFeat) {
                                        {
                                            DataValue(
                                                text = stringResource(
                                                    R.string.crew_approvals_left,
                                                    state.approvalsLeft,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    } else null,
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
    }

    state.memberMenuFor?.let { member ->
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.MemberMenuRequested(null)) },
            title = {
                Text(member.displayName, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    TextButton(
                        onClick = { onEvent(CrewEvent.TransferRequested(member)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.crew_transfer),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    TextButton(
                        onClick = { onEvent(CrewEvent.KickRequested(member)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.crew_kick),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(CrewEvent.MemberMenuRequested(null)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    state.kickTarget?.let { member ->
        ConfirmDialog(
            title = stringResource(R.string.crew_kick_title, member.displayName),
            body = stringResource(R.string.crew_kick_body),
            confirmLabel = stringResource(R.string.crew_kick),
            destructive = true,
            working = state.isWorking,
            onConfirm = { onEvent(CrewEvent.KickConfirmed) },
            onDismiss = { onEvent(CrewEvent.KickRequested(null)) },
        )
    }

    state.transferTarget?.let { member ->
        ConfirmDialog(
            title = stringResource(R.string.crew_transfer_title, member.displayName),
            body = stringResource(R.string.crew_transfer_body),
            confirmLabel = stringResource(R.string.crew_transfer),
            destructive = false,
            working = state.isWorking,
            onConfirm = { onEvent(CrewEvent.TransferConfirmed) },
            onDismiss = { onEvent(CrewEvent.TransferRequested(null)) },
        )
    }

    if (state.showRegenerateDialog) {
        ConfirmDialog(
            title = stringResource(R.string.crew_regenerate_title),
            body = stringResource(R.string.crew_regenerate_body),
            confirmLabel = stringResource(R.string.crew_regenerate),
            destructive = true,
            working = state.isWorking,
            onConfirm = { onEvent(CrewEvent.RegenerateConfirmed) },
            onDismiss = { onEvent(CrewEvent.RegenerateDialogToggled(false)) },
        )
    }

    if (state.showRenameDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(CrewEvent.RenameDialogToggled(false)) },
            title = {
                Text(
                    stringResource(R.string.crew_rename),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                OutlinedTextField(
                    value = state.renameInput,
                    onValueChange = { onEvent(CrewEvent.RenameInputChanged(it)) },
                    label = { Text(stringResource(R.string.crew_name_label)) },
                    singleLine = true,
                    enabled = !state.isWorking,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(CrewEvent.RenameConfirmed) },
                    enabled = state.canRename,
                ) {
                    Text(stringResource(R.string.crew_rename))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CrewEvent.RenameDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
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
private fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    destructive: Boolean,
    working: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !working) {
                Text(
                    confirmLabel,
                    color = if (destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

private fun Context.shareInvite(text: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
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
