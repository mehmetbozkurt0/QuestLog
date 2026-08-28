package com.mehmetbozkurt.questlog.feature.crew.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.levelRankRes
import com.mehmetbozkurt.questlog.core.designsystem.component.AuraBar
import com.mehmetbozkurt.questlog.core.designsystem.component.DataValue
import com.mehmetbozkurt.questlog.core.designsystem.component.EmptyState
import com.mehmetbozkurt.questlog.core.designsystem.component.GlassPanel
import com.mehmetbozkurt.questlog.core.designsystem.component.IconTile
import com.mehmetbozkurt.questlog.core.designsystem.component.LevelMedallion
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionTitle
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellBackBar
import com.mehmetbozkurt.questlog.core.designsystem.component.wellColor
import com.mehmetbozkurt.questlog.core.designsystem.theme.ContentHero
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.uppercaseLocalized
import com.mehmetbozkurt.questlog.feature.crew.component.FeedEntryCard

@Composable
fun CrewMemberRoute(
    onNavigateBack: () -> Unit,
    viewModel: CrewMemberViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CrewMemberScreen(state = state, onNavigateBack = onNavigateBack)
}

@Composable
fun CrewMemberScreen(
    state: CrewMemberState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            ShellBackBar(
                title = stringResource(R.string.crew_member_profile_title),
                onBack = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val member = state.member

        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            member == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.PersonOff,
                    title = stringResource(R.string.crew_member_gone_title),
                    body = stringResource(R.string.crew_member_gone_body),
                )
            }

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.screen)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Spacing.lg))

                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LevelMedallion(
                            level = member.level,
                            progress = state.levelProgress,
                            diameter = 96.dp,
                        )

                        Spacer(Modifier.height(Spacing.md))

                        Text(
                            text = member.displayName,
                            style = ContentHero,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(Spacing.xs))

                        Text(
                            text = stringResource(
                                R.string.character_level_rank,
                                member.level,
                                stringResource(levelRankRes(member.level)),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (state.rank > 0) {
                            Spacer(Modifier.height(Spacing.sm))
                            DataValue(
                                text = stringResource(
                                    R.string.crew_member_rank,
                                    state.rank,
                                    state.crewSize,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    DataValue(
                        text = if (state.isMaxLevel) {
                            stringResource(R.string.profile_max_level)
                        } else {
                            stringResource(
                                R.string.character_xp_progress,
                                state.xpIntoLevel,
                                state.xpToNextLevel,
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    AuraBar(
                        progress = state.levelProgress,
                        color = MaterialTheme.colorScheme.primary,
                        height = Spacing.barHeight,
                    )
                }

                Spacer(Modifier.height(Spacing.section))

                SectionTitle(
                    text = stringResource(R.string.profile_section_journey),
                    icon = Icons.Default.MilitaryTech,
                )

                Spacer(Modifier.height(Spacing.md))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        icon = Icons.Default.EmojiEvents,
                        value = "${member.totalXp}",
                        label = stringResource(R.string.profile_stat_total_xp),
                    )
                    StatTile(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${member.currentStreak}",
                        label = stringResource(R.string.profile_stat_streak),
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        icon = Icons.Default.TaskAlt,
                        value = "${state.questsShared}",
                        label = stringResource(R.string.crew_member_quests_shared),
                    )
                    StatTile(
                        icon = Icons.Default.ThumbUp,
                        value = "${state.approvalsReceived}",
                        label = stringResource(R.string.crew_member_approvals),
                    )
                }

                Spacer(Modifier.height(Spacing.section))

                SectionTitle(text = stringResource(R.string.crew_member_activity))

                Spacer(Modifier.height(Spacing.md))

                if (state.feed.isEmpty()) {
                    Text(
                        text = stringResource(R.string.crew_member_no_activity),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.lg),
                    )
                } else {
                    state.feed.forEach { item ->
                        FeedEntryCard(
                            item = item,
                            isMine = state.isSelf,
                            canApprove = false,
                            approvedByMe = false,
                            onApprove = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(Spacing.md))
                    }
                }

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}

@Composable
private fun RowScope.StatTile(
    icon: ImageVector,
    value: String,
    label: String,
) {
    GlassPanel(
        containerColor = wellColor(),
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier.weight(1f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = icon,
                color = MaterialTheme.colorScheme.primary,
                size = 32.dp,
                iconSize = 16.dp,
            )
            Spacer(Modifier.width(Spacing.sm))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                Text(
                    text = label.uppercaseLocalized(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}
