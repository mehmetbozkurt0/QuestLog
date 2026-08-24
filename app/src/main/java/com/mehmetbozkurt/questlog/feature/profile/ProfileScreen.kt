package com.mehmetbozkurt.questlog.feature.profile

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.BuildConfig
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.auth.GoogleCredentialProvider
import com.mehmetbozkurt.questlog.core.common.asString
import com.mehmetbozkurt.questlog.core.auth.GoogleIdTokenResult
import com.mehmetbozkurt.questlog.core.designsystem.component.QuestCard
import com.mehmetbozkurt.questlog.core.designsystem.component.SectionRule
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.notification.ReminderScheduler
import com.mehmetbozkurt.questlog.core.settings.AppLanguage
import com.mehmetbozkurt.questlog.core.settings.ThemePreference
import com.mehmetbozkurt.questlog.domain.progression.XpCurve
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileRoute(
    onNavigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                ProfileEffect.NavigateToAuth -> onNavigateToAuth()
                ProfileEffect.OpenNotificationSettings -> context.openNotificationSettings()

                ProfileEffect.LaunchGoogleReauth -> scope.launch {
                    when (val result = GoogleCredentialProvider.requestIdToken(context)) {
                        is GoogleIdTokenResult.Success ->
                            viewModel.onEvent(ProfileEvent.GoogleReauthToken(result.idToken))

                        GoogleIdTokenResult.Cancelled ->
                            viewModel.onEvent(ProfileEvent.GoogleReauthFailed(null))

                        is GoogleIdTokenResult.Failed ->
                            viewModel.onEvent(ProfileEvent.GoogleReauthFailed(result.message))
                    }
                }
            }
        }
    }

    ProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(Spacing.md))

            IdentityCard(state)

            Spacer(Modifier.height(Spacing.lg))

            SectionHeader(stringResource(R.string.profile_section_journey))
            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                MetricCard(
                    icon = Icons.Default.TaskAlt,
                    value = "${state.completedQuests}",
                    label = stringResource(R.string.profile_stat_completed),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${state.streak?.currentStreak ?: 0}",
                    label = stringResource(R.string.profile_stat_streak),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                MetricCard(
                    icon = Icons.Default.EmojiEvents,
                    value = "${state.featCount}",
                    label = stringResource(R.string.profile_stat_feats),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    icon = Icons.Default.Groups,
                    value = if (state.crewName != null) "1" else "0",
                    label = state.crewName ?: stringResource(R.string.profile_no_crew),
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.streak != null && state.streak.longestStreak > 0) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    stringResource(
                        R.string.profile_streak_summary,
                        state.streak.longestStreak,
                        state.activeQuests,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            SectionHeader(stringResource(R.string.profile_section_appearance))
            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ThemePreference.entries.forEach { pref ->
                    FilterChip(
                        selected = state.theme == pref,
                        onClick = { onEvent(ProfileEvent.ThemeChanged(pref)) },
                        label = { Text(pref.label()) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            SectionHeader(stringResource(R.string.profile_section_language))
            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = state.language == language,
                        onClick = { onEvent(ProfileEvent.LanguageChanged(language)) },
                        label = { Text(language.label()) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            SectionHeader(stringResource(R.string.profile_section_notifications))
            Spacer(Modifier.height(Spacing.md))

            QuestCard(
                onClick = { onEvent(ProfileEvent.NotificationSettingsClicked) },
                seed = 31,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.profile_notification_settings),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            stringResource(
                                R.string.profile_notification_settings_desc,
                                ReminderScheduler.STREAK_HOUR,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            OutlinedButton(
                onClick = { onEvent(ProfileEvent.SignOutDialogToggled(true)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(R.string.profile_sign_out),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            TextButton(
                onClick = { onEvent(ProfileEvent.DeleteDialogToggled(true)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.profile_delete_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Text(
                stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.xxl))
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) onEvent(ProfileEvent.DeleteDialogToggled(false))
            },
            title = { Text(stringResource(R.string.profile_delete_dialog_title)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.profile_delete_dialog_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    if (state.isPasswordAccount) {
                        Text(
                            stringResource(R.string.profile_delete_password_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        OutlinedTextField(
                            value = state.deletePassword,
                            onValueChange = { onEvent(ProfileEvent.DeletePasswordChanged(it)) },
                            label = { Text(stringResource(R.string.auth_password)) },
                            singleLine = true,
                            enabled = !state.isDeleting,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = state.deleteError != null,
                            supportingText = {
                                state.deleteError?.let { Text(it.asString()) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            stringResource(R.string.profile_delete_google_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.deleteError?.let {
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                text = it.asString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    if (state.isDeleting) {
                        Spacer(Modifier.height(Spacing.sm))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(ProfileEvent.DeleteConfirmed) },
                    enabled = state.canDelete,
                ) {
                    Text(
                        stringResource(R.string.profile_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(ProfileEvent.DeleteDialogToggled(false)) },
                    enabled = !state.isDeleting,
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileEvent.SignOutDialogToggled(false)) },
            title = { Text(stringResource(R.string.profile_sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfileEvent.SignOutConfirmed) }) {
                    Text(
                        stringResource(R.string.profile_sign_out),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileEvent.SignOutDialogToggled(false)) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun IdentityCard(state: ProfileState) {
    QuestCard(
        seed = 29,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape,
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(Modifier.weight(1f)) {
                Text(
                    text = state.user?.displayName.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.profile_level_title,
                        state.level,
                        stringResource(state.titleRes),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = state.user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        LinearProgressIndicator(
            progress = { state.levelProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        Spacer(Modifier.height(Spacing.xs))

        Row(Modifier.fillMaxWidth()) {
            Text(
                text = if (state.isMaxLevel) {
                    stringResource(R.string.profile_max_level)
                } else {
                    stringResource(
                        R.string.profile_xp_to_next,
                        (state.character?.xpToNextLevel ?: 0) -
                            (state.character?.xpIntoLevel ?: 0),
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.totalXp} XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.character?.createdAt?.let { created ->
            val memberFormatter = rememberMemberFormatter()
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(
                    R.string.profile_member_since,
                    memberFormatter.format(created.atZone(ZoneId.systemDefault())),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    QuestCard(
        seed = label.hashCode(),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(Spacing.md))
        SectionRule(Modifier.weight(1f))
    }
}

@Composable
private fun rememberMemberFormatter(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { DateTimeFormatter.ofPattern("d MMMM yyyy", locale) }
}

@Composable
private fun ThemePreference.label(): String = stringResource(
    when (this) {
        ThemePreference.SYSTEM -> R.string.theme_system
        ThemePreference.LIGHT -> R.string.theme_light
        ThemePreference.DARK -> R.string.theme_dark
    }
)

@Composable
private fun AppLanguage.label(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> "English"
    AppLanguage.TURKISH -> "Türkçe"
}

private fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
