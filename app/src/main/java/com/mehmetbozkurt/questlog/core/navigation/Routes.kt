package com.mehmetbozkurt.questlog.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthRouteKey

@Serializable
data object OnboardingRouteKey

@Serializable
data object HomeRouteKey

@Serializable
data class LogDetailRouteKey(val id: String)

@Serializable
data class LogEditRouteKey(val logId: String?)

@Serializable
data class PathwayDetailRouteKey(val pathwayId: String)
