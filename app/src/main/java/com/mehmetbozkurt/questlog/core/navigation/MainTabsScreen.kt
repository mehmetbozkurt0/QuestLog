package com.mehmetbozkurt.questlog.core.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellBottomBar
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellTab
import com.mehmetbozkurt.questlog.core.designsystem.component.ShellTopBar
import com.mehmetbozkurt.questlog.feature.character.CharacterRoute
import com.mehmetbozkurt.questlog.feature.crew.CrewRoute
import com.mehmetbozkurt.questlog.feature.pathway.PathwayListRoute
import com.mehmetbozkurt.questlog.feature.profile.ProfileRoute
import com.mehmetbozkurt.questlog.feature.questlog.QuestLogListRoute
import kotlinx.coroutines.launch

@Composable
fun MainTabsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: (Int) -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToPathwayDetail: (String) -> Unit,
    onNavigateToCrewMember: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    openCrewChat: Boolean = false,
    onCrewChatHandled: () -> Unit = {},
    shellViewModel: AppShellViewModel = hiltViewModel(),
) {
    val unreadMessages by shellViewModel.unreadMessages.collectAsStateWithLifecycle()
    val header by shellViewModel.header.collectAsStateWithLifecycle()
    val items = BottomNavItem.entries
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    var startCrewOnChat by rememberSaveable { mutableStateOf(false) }

    NotificationPermissionGate()

    fun goTo(item: BottomNavItem) {
        scope.launch { pagerState.animateScrollToPage(items.indexOf(item)) }
    }

    LaunchedEffect(openCrewChat) {
        if (!openCrewChat) return@LaunchedEffect
        startCrewOnChat = true
        pagerState.animateScrollToPage(items.indexOf(BottomNavItem.CREW))
        onCrewChatHandled()
    }

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    val tabs = items.map { item ->
        ShellTab(
            label = stringResource(item.labelRes),
            icon = item.icon,
            badge = if (item == BottomNavItem.CREW) unreadMessages else 0,
        )
    }

    Scaffold(
        topBar = {
            ShellTopBar(
                level = header.level,
                levelProgress = header.levelProgress,
                streak = header.streak,
                onCrestClick = { goTo(BottomNavItem.CHARACTER) },
            )
        },
        bottomBar = {
            ShellBottomBar(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onSelect = { goTo(items[it]) },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            beyondViewportPageCount = 1,
            snapPosition = SnapPosition.Start,
            key = { items[it].name },
        ) { page ->
            when (items[page]) {
                BottomNavItem.LOGS -> QuestLogListRoute(
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToCreate = onNavigateToCreate,
                    onNavigateToCatalog = onNavigateToCatalog,
                    onNavigateToPathways = { goTo(BottomNavItem.PATHWAYS) },
                    onNavigateToPathwayDetail = onNavigateToPathwayDetail,
                    onNavigateToCharacter = { goTo(BottomNavItem.CHARACTER) },
                )

                BottomNavItem.CHARACTER -> CharacterRoute()

                BottomNavItem.PATHWAYS -> PathwayListRoute(
                    onNavigateToDetail = onNavigateToPathwayDetail,
                )

                BottomNavItem.CREW -> CrewRoute(
                    onNavigateToMember = onNavigateToCrewMember,
                    startOnChat = startCrewOnChat,
                    onStartOnChatHandled = { startCrewOnChat = false },
                )

                BottomNavItem.PROFILE -> ProfileRoute(onNavigateToAuth = onNavigateToAuth)
            }
        }
    }
}

@Composable
private fun NotificationPermissionGate() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (requested) return@LaunchedEffect
        requested = true
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
