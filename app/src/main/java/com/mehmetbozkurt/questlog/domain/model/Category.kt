package com.mehmetbozkurt.questlog.domain.model

import java.time.Instant

data class Category(
    val id: String,
    val ownerId: String,
    val name: String,
    val colorHex: String,
    val createdAt: Instant,
)