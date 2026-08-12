package com.mehmetbozkurt.questlog.domain.model

import kotlin.time.Instant

data class Category(
    val id: String,
    val ownerId: String,
    val name: String,
    val colorHex: String,
    val createdAt: Instant,
)