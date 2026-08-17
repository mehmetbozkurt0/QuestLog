package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mehmetbozkurt.questlog.feature.auth.AuthRoute
import com.mehmetbozkurt.questlog.feature.logdetail.LogDetailRoute
import com.mehmetbozkurt.questlog.feature.logedit.LogEditRoute
import com.mehmetbozkurt.questlog.feature.profile.ProfileRoute
import com.mehmetbozkurt.questlog.feature.questlog.QuestLogListRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.mehmetbozkurt.questlog.feature.category.CategoryRoute
import com.mehmetbozkurt.questlog.feature.character.CharacterRoute

@Composable
fun QuestLogNavHost(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = BottomNavItem.entries.any { item ->
        currentDestination?.hierarchy?.any { it.hasRouteOf(item) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.hasRouteOf(item) } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = {
                                Text(item.label, style = MaterialTheme.typography.labelMedium)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startLoggedIn) HomeRouteKey else AuthRouteKey,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
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

            composable<CharacterRouteKey> {
                CharacterRoute()
            }

            composable<CrewRouteKey> {
                ComingSoonScreen(
                    title = "Ekip",
                    description = "Ekip arkadaşlarınla kayıt paylaşımı ve sohbet. Yakında.",
                )
            }

            composable<ProfileRouteKey> {
                ProfileRoute(
                    onNavigateToAuth = {
                        navController.navigate(AuthRouteKey) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToCategories = {navController.navigate(CategoryRouteKey)}
                )
            }

            composable<CategoryRouteKey> {
                CategoryRoute(onNavigateBack = {navController.popBackStack()})
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
}

private fun androidx.navigation.NavDestination.hasRouteOf(item: BottomNavItem): Boolean =
    when (item) {
        BottomNavItem.LOGS -> hasRoute(HomeRouteKey::class)
        BottomNavItem.CHARACTER -> hasRoute(CharacterRouteKey::class)
        BottomNavItem.CREW -> hasRoute(CrewRouteKey::class)
        BottomNavItem.PROFILE -> hasRoute(ProfileRouteKey::class)
    }