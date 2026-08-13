package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
) {
    LOGS(HomeRouteKey, "Defter", Icons.AutoMirrored.Filled.MenuBook),
    CAMPAIGNS(CampaignsRouteKey, "Oturumlar", Icons.Default.Map),
    CREW(CrewRouteKey, "Ekip", Icons.Default.Groups),
    PROFILE(ProfileRouteKey, "Profil", Icons.Default.Person)
}