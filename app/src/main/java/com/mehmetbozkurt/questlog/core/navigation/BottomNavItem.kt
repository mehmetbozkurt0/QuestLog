package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
) {
    LOGS(HomeRouteKey, "Görevler", Icons.AutoMirrored.Filled.MenuBook),
    CHARACTER(CharacterRouteKey, "Karakter", Icons.Default.Shield),
    CREW(CrewRouteKey, "Ekip", Icons.Default.Groups),
    PROFILE(ProfileRouteKey, "Profil", Icons.Default.Person)
}