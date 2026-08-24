package com.mehmetbozkurt.questlog.domain.model

enum class ProofLevel(val multiplier: Double) {
    NONE(1.0),
    NOTE(1.15),
    PHOTO(1.30),
}
