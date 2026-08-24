package com.mehmetbozkurt.questlog.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun String.uppercaseLocalized(): String =
    uppercase(LocalConfiguration.current.locales[0])
