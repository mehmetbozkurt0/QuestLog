package com.mehmetbozkurt.questlog.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun String.toComposeColor(): Color = Color(this.toColorInt())