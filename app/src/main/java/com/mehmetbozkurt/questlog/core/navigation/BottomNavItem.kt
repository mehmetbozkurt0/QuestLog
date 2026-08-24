package com.mehmetbozkurt.questlog.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.mehmetbozkurt.questlog.R

enum class BottomNavItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    LOGS(R.string.nav_logs, Icons.AutoMirrored.Filled.MenuBook),
    PATHWAYS(R.string.nav_pathways, Icons.Default.Explore),
    CHARACTER(R.string.nav_character, Icons.Default.Shield),
    CREW(R.string.nav_crew, Icons.Default.Groups),
    PROFILE(R.string.nav_profile, Icons.Default.Person)
}
