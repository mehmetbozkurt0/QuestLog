package com.mehmetbozkurt.questlog.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthRouteKey

@Serializable
data object HomeRouteKey

@Serializable
data class LogDetailRouteKey(val id: String)

@Serializable
data class LogEditRouteKey(val logId: String?)

@Serializable
data object ProfileRouteKey

@Serializable
data object CrewRouteKey

@Serializable
data object CampaignsRouteKey

@Serializable
data object CharacterRouteKey

@Serializable
data object PathwayListRouteKey

@Serializable
data class PathwayDetailRouteKey(val pathwayId: String)