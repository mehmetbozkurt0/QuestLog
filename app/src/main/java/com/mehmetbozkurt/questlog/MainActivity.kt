package com.mehmetbozkurt.questlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.theme.QuestLogTheme
import com.mehmetbozkurt.questlog.core.navigation.QuestLogNavHost
import com.mehmetbozkurt.questlog.feature.splash.SplashViewModel
import com.mehmetbozkurt.questlog.feature.splash.StartDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel : SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.startDestination.value == StartDestination.Loading
        }

        setContent {
            val destination by splashViewModel.startDestination.collectAsStateWithLifecycle()

            QuestLogTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (destination) {
                        StartDestination.Loading -> Unit
                        StartDestination.Home -> QuestLogNavHost(startLoggedIn = true)
                        StartDestination.Auth -> QuestLogNavHost(startLoggedIn = false)
                    }
                }
            }
        }
    }
}