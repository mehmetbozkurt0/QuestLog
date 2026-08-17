package com.mehmetbozkurt.questlog.domain.model

enum class ProofLevel(val multiplier: Double) {
    NONE(1.0),
    NOTE(1.15),
    PHOTO(1.30),
}

fun ProofLevel.displayName(): String = when (this) {
    ProofLevel.NONE -> "Kanıtsız"
    ProofLevel.NOTE -> "Not"
    ProofLevel.PHOTO -> "Fotoğraf"
}