package com.mehmetbozkurt.questlog.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mehmetbozkurt.questlog.feature.character.CharacterRoute
import com.mehmetbozkurt.questlog.feature.crew.CrewRoute
import com.mehmetbozkurt.questlog.feature.pathway.PathwayListRoute
import com.mehmetbozkurt.questlog.feature.profile.ProfileRoute
import com.mehmetbozkurt.questlog.feature.questlog.QuestLogListRoute
import kotlinx.coroutines.launch

@Composable
fun MainTabsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToPathwayDetail: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    val tabs = BottomNavItem.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, item ->
                    val label = stringResource(item.labelRes)
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = { Icon(item.icon, contentDescription = label) },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            beyondViewportPageCount = 1,
            snapPosition = SnapPosition.Start,
            key = { tabs[it].name },
        ) { page ->
            when (tabs[page]) {
                BottomNavItem.LOGS -> QuestLogListRoute(
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToCreate = onNavigateToCreate,
                    onNavigateToPathways = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                tabs.indexOf(BottomNavItem.PATHWAYS)
                            )
                        }
                    },
                    onNavigateToPathwayDetail = onNavigateToPathwayDetail,
                    onNavigateToCharacter = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                tabs.indexOf(BottomNavItem.CHARACTER)
                            )
                        }
                    },
                )

                BottomNavItem.PATHWAYS -> PathwayListRoute(
                    onNavigateToDetail = onNavigateToPathwayDetail,
                )

                BottomNavItem.CHARACTER -> CharacterRoute()

                BottomNavItem.CREW -> CrewRoute()

                BottomNavItem.PROFILE -> ProfileRoute(onNavigateToAuth = onNavigateToAuth)
            }
        }
    }
}
