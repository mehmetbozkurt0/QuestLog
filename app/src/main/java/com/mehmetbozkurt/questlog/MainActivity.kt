package com.mehmetbozkurt.questlog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.QuestLogTheme
import com.mehmetbozkurt.questlog.core.navigation.QuestLogNavHost
import com.mehmetbozkurt.questlog.core.notification.CrewMessageNotifier
import com.mehmetbozkurt.questlog.core.notification.ReminderReceiver
import com.mehmetbozkurt.questlog.core.settings.AppPalette
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.core.settings.ThemePreference
import com.mehmetbozkurt.questlog.feature.splash.SplashViewModel
import com.mehmetbozkurt.questlog.feature.splash.StartDestination
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val splashViewModel: SplashViewModel by viewModels()
    @Inject lateinit var settingsRepository: SettingsRepository

    private val pendingLogId = mutableStateOf<String?>(null)
    private val openCrewChat = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        pendingLogId.value = intent?.getStringExtra(ReminderReceiver.EXTRA_LOG_ID)
        openCrewChat.value =
            intent?.getBooleanExtra(CrewMessageNotifier.EXTRA_OPEN_CREW_CHAT, false) == true

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == StartDestination.Loading
        }

        setContent {
            val destination by splashViewModel.startDestination.collectAsStateWithLifecycle()
            val theme by settingsRepository.observeTheme()
                .collectAsStateWithLifecycle(initialValue = ThemePreference.Default)
            val palette by settingsRepository.observePalette()
                .collectAsStateWithLifecycle(initialValue = AppPalette.Default)

            val darkTheme = when (theme) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        LIGHT_NAV_SCRIM,
                        DARK_NAV_SCRIM,
                    ) { darkTheme },
                )
            }

            QuestLogTheme(
                darkTheme = darkTheme,
                palette = palette,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (destination) {
                        StartDestination.Loading -> Unit
                        StartDestination.Home -> QuestLogNavHost(
                            startLoggedIn = true,
                            pendingLogId = pendingLogId.value,
                            onPendingLogHandled = { pendingLogId.value = null },
                            openCrewChat = openCrewChat.value,
                            onCrewChatHandled = { openCrewChat.value = false },
                        )

                        StartDestination.Auth -> QuestLogNavHost(
                            startLoggedIn = false,
                            pendingLogId = null,
                            onPendingLogHandled = {},
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingLogId.value = intent.getStringExtra(ReminderReceiver.EXTRA_LOG_ID)
        openCrewChat.value =
            intent.getBooleanExtra(CrewMessageNotifier.EXTRA_OPEN_CREW_CHAT, false)
    }

    private companion object {
        const val LIGHT_NAV_SCRIM = 0xE6FFFFFF.toInt()
        const val DARK_NAV_SCRIM = 0x801B1B1B.toInt()
    }
}
