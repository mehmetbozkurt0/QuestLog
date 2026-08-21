package com.mehmetbozkurt.questlog.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    LOGS("Görevler", Icons.AutoMirrored.Filled.MenuBook),
    PATHWAYS("Yollar", Icons.Default.Explore),
    CHARACTER("Karakter", Icons.Default.Shield),
    CREW("Ekip", Icons.Default.Groups),
    PROFILE("Profil", Icons.Default.Person)
}
