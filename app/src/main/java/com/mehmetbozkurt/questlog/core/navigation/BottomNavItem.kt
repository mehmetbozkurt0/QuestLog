package com.mehmetbozkurt.questlog.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.mehmetbozkurt.questlog.R

enum class BottomNavItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    LOGS(R.string.nav_logs, Icons.AutoMirrored.Outlined.Assignment),
    CHARACTER(R.string.nav_character, Icons.Outlined.Person),
    PATHWAYS(R.string.nav_pathways, Icons.AutoMirrored.Outlined.AltRoute),
    CREW(R.string.nav_crew, Icons.Outlined.Groups),
    PROFILE(R.string.nav_profile, Icons.Outlined.AccountCircle)
}
