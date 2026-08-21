package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mehmetbozkurt.questlog.feature.auth.AuthRoute
import com.mehmetbozkurt.questlog.feature.logdetail.LogDetailRoute
import com.mehmetbozkurt.questlog.feature.logedit.LogEditRoute
import com.mehmetbozkurt.questlog.feature.onboarding.OnboardingRoute
import com.mehmetbozkurt.questlog.feature.pathway.PathwayDetailRoute

private const val NAV_DURATION = 260

@Composable
fun QuestLogNavHost(
    startLoggedIn: Boolean,
    pendingLogId: String? = null,
    onPendingLogHandled: () -> Unit = {},
) {
    val navController = rememberNavController()

    LaunchedEffect(pendingLogId) {
        val logId = pendingLogId ?: return@LaunchedEffect
        navController.navigate(LogDetailRouteKey(logId))
        onPendingLogHandled()
    }

    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) HomeRouteKey else AuthRouteKey,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(animationSpec = tween(NAV_DURATION)) { it / 6 } +
                    fadeIn(animationSpec = tween(NAV_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(NAV_DURATION)) { -it / 10 } +
                    fadeOut(animationSpec = tween(NAV_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(NAV_DURATION)) { -it / 10 } +
                    fadeIn(animationSpec = tween(NAV_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(NAV_DURATION)) { it / 6 } +
                    fadeOut(animationSpec = tween(NAV_DURATION))
        },
    ) {
        composable<AuthRouteKey> {
            AuthRoute(
                onNavigateToHome = {
                    navController.navigate(HomeRouteKey) {
                        popUpTo(AuthRouteKey) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRouteKey) {
                        popUpTo(AuthRouteKey) { inclusive = true }
                    }
                },
            )
        }

        composable<OnboardingRouteKey> {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(HomeRouteKey) {
                        popUpTo(OnboardingRouteKey) { inclusive = true }
                    }
                }
            )
        }

        composable<HomeRouteKey> {
            MainTabsScreen(
                onNavigateToDetail = { id -> navController.navigate(LogDetailRouteKey(id)) },
                onNavigateToCreate = { navController.navigate(LogEditRouteKey(null)) },
                onNavigateToPathwayDetail = { id ->
                    navController.navigate(PathwayDetailRouteKey(id))
                },
                onNavigateToAuth = {
                    navController.navigate(AuthRouteKey) {
                        popUpTo(0) { inclusive = true }
                    }
                },
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

        composable<PathwayDetailRouteKey> {
            PathwayDetailRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}
