package com.mehmetbozkurt.questlog.domain.model

enum class StatType {
    STR, DEX, CON, INT, WIS, CHA
}

fun StatType.displayName(): String = when (this) {
    StatType.STR -> "Güç"
    StatType.DEX -> "Çeviklik"
    StatType.CON -> "Dayanıklılık"
    StatType.INT -> "Zeka"
    StatType.WIS -> "Bilgelik"
    StatType.CHA -> "Karizma"
}

fun StatType.shortLabel(): String = when (this) {
    StatType.STR -> "GÜÇ"
    StatType.DEX -> "ÇEV"
    StatType.CON -> "DAY"
    StatType.INT -> "ZEK"
    StatType.WIS -> "BİL"
    StatType.CHA -> "KAR"
}

fun StatType.shortName(): String = name

fun StatType.description(): String = when (this) {
    StatType.STR -> "Antrenman, kuvvet, fiziksel çaba"
    StatType.DEX -> "Beceri, koordinasyon, el işi"
    StatType.CON -> "Uyku, beslenme, sağlık"
    StatType.INT -> "Öğrenme, okuma, çalışma"
    StatType.WIS -> "Meditasyon, günlük, öz farkındalık"
    StatType.CHA -> "Sosyal, iletişim, sunum"
}

fun StatType.colorHex(): String = when (this) {
    StatType.STR -> "#C1443A"
    StatType.DEX -> "#6E8F6B"
    StatType.CON -> "#8C5A3C"
    StatType.INT -> "#5B8FA8"
    StatType.WIS -> "#7B5EA7"
    StatType.CHA -> "#C8A951"
}