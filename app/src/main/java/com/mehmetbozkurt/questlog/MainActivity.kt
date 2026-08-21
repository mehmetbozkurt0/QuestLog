package com.mehmetbozkurt.questlog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.QuestLogTheme
import com.mehmetbozkurt.questlog.core.navigation.QuestLogNavHost
import com.mehmetbozkurt.questlog.core.notification.ReminderReceiver
import com.mehmetbozkurt.questlog.core.settings.SettingsRepository
import com.mehmetbozkurt.questlog.core.settings.ThemePreference
import com.mehmetbozkurt.questlog.feature.splash.SplashViewModel
import com.mehmetbozkurt.questlog.feature.splash.StartDestination
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()
    @Inject lateinit var settingsRepository: SettingsRepository

    private val pendingLogId = mutableStateOf<String?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        pendingLogId.value = intent?.getStringExtra(ReminderReceiver.EXTRA_LOG_ID)
        requestNotificationPermissionIfNeeded()

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == StartDestination.Loading
        }

        setContent {
            val destination by splashViewModel.startDestination.collectAsStateWithLifecycle()
            val theme by settingsRepository.observeTheme()
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

            QuestLogTheme(
                darkTheme = when (theme) {
                    ThemePreference.SYSTEM -> isSystemInDarkTheme()
                    ThemePreference.LIGHT -> false
                    ThemePreference.DARK -> true
                }
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
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
