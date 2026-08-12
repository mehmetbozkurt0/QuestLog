package com.mehmetbozkurt.questlog.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthRouteKey

@Serializable
data object HomeRouteKey

@Serializable
data object QuestLogListRouteKey

@Serializable
data object CreateLogRouteKey

@Serializable
data class LogDetailRouteKey(val id: String)