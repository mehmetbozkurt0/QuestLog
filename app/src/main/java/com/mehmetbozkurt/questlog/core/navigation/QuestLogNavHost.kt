package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mehmetbozkurt.questlog.feature.auth.AuthRoute
import com.mehmetbozkurt.questlog.feature.home.SignOutViewModel
import com.mehmetbozkurt.questlog.feature.logdetail.LogDetailRoute
import com.mehmetbozkurt.questlog.feature.logedit.LogEditRoute
import com.mehmetbozkurt.questlog.feature.questlog.QuestLogListRoute

@Composable
fun QuestLogNavHost(startLoggedIn: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) HomeRouteKey else AuthRouteKey,
    ) {
        composable<AuthRouteKey> {
            AuthRoute(
                onNavigateToHome = {
                    navController.navigate(HomeRouteKey) {
                        popUpTo(AuthRouteKey) { inclusive = true }
                    }
                }
            )
        }

        composable<HomeRouteKey> {
            QuestLogListRoute(
                onNavigateToDetail = { id -> navController.navigate(LogDetailRouteKey(id)) },
                onNavigateToCreate = { navController.navigate(LogEditRouteKey(null)) },
            )
        }

        composable<LogEditRouteKey> {
            LogEditRoute(onNavigateBack = { navController.popBackStack() })
        }

        composable<LogDetailRouteKey> {
            LogDetailRoute(
                onNavigateToEdit = { id -> navController.navigate(LogEditRouteKey(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun HomePlaceholder(
    onSignOut: () -> Unit,
    viewModel: SignOutViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Giriş başarılı", style = MaterialTheme.typography.headlineMedium)
        TextButton(
            onClick = {
                viewModel.signOut()
                onSignOut()
            }
        ) { Text("Çıkış Yap") }
    }
}