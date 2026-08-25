package com.mehmetbozkurt.questlog.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class PaletteSpec(
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val gold: Color,
    val goldDeep: Color,
    val text: Color,
    val textDim: Color,
    val outline: Color,
    val str: Color,
    val dex: Color,
    val con: Color,
    val int: Color,
    val wis: Color,
    val cha: Color,
    val low: Color,
    val medium: Color,
    val high: Color,
)

// --- Derin mürekkep: mum ışığında meşin cilt ---
val MurekkepDark = PaletteSpec(
    bg = Color(0xFF0E0B08),
    surface = Color(0xFF1A150F),
    surfaceHigh = Color(0xFF251E15),
    gold = Color(0xFFD4B25C),
    goldDeep = Color(0xFF8F6E22),
    text = Color(0xFFEFE6D0),
    textDim = Color(0xFF9C8C70),
    outline = Color(0xFF52432F),
    str = Color(0xFFD4483C),
    dex = Color(0xFF7FA86B),
    con = Color(0xFFB0703F),
    int = Color(0xFF5E9BC4),
    wis = Color(0xFF8E6BC8),
    cha = Color(0xFFD98C3C),
    low = Color(0xFF7FA86B),
    medium = Color(0xFFD9A34E),
    high = Color(0xFFD4483C),
)

val MurekkepLight = PaletteSpec(
    bg = Color(0xFFF5EBD6),
    surface = Color(0xFFFCF6E8),
    surfaceHigh = Color(0xFFEADCC0),
    gold = Color(0xFF8F6E22),
    goldDeep = Color(0xFF6E5416),
    text = Color(0xFF241C13),
    textDim = Color(0xFF6E5D46),
    outline = Color(0xFFBCA985),
    str = Color(0xFFA4322A),
    dex = Color(0xFF4A6B47),
    con = Color(0xFF7A4A28),
    int = Color(0xFF3D6E86),
    wis = Color(0xFF5B4384),
    cha = Color(0xFF9A5C18),
    low = Color(0xFF47734A),
    medium = Color(0xFF966A16),
    high = Color(0xFFB03A2C),
)

// --- Gece mavisi: soğuk zemin, sıcak altın ---
val GeceDark = PaletteSpec(
    bg = Color(0xFF0B0E14),
    surface = Color(0xFF141922),
    surfaceHigh = Color(0xFF1E2531),
    gold = Color(0xFFC9A860),
    goldDeep = Color(0xFF8A7238),
    text = Color(0xFFDFE4EC),
    textDim = Color(0xFF8A93A3),
    outline = Color(0xFF2E3846),
    str = Color(0xFFD2564E),
    dex = Color(0xFF62B08A),
    con = Color(0xFFC08A4A),
    int = Color(0xFF5FA8D8),
    wis = Color(0xFF9B7BE0),
    cha = Color(0xFFE0A24C),
    low = Color(0xFF62B08A),
    medium = Color(0xFFD9A34E),
    high = Color(0xFFD2564E),
)

val GeceLight = PaletteSpec(
    bg = Color(0xFFEEF1F6),
    surface = Color(0xFFF8FAFC),
    surfaceHigh = Color(0xFFDFE5EE),
    gold = Color(0xFF8A6E2E),
    goldDeep = Color(0xFF6B5522),
    text = Color(0xFF161B24),
    textDim = Color(0xFF566172),
    outline = Color(0xFFB7C0CE),
    str = Color(0xFFB23F38),
    dex = Color(0xFF2F7A57),
    con = Color(0xFF93602A),
    int = Color(0xFF2A6C96),
    wis = Color(0xFF6247A8),
    cha = Color(0xFF9E6A20),
    low = Color(0xFF2F7A57),
    medium = Color(0xFF8A6414),
    high = Color(0xFFB23F38),
)

// --- Yüksek kontrast: nötr zemin, renk sadece anlam taşıdığı yerde ---
val KontrastDark = PaletteSpec(
    bg = Color(0xFF0A0A0A),
    surface = Color(0xFF151515),
    surfaceHigh = Color(0xFF1F1F1F),
    gold = Color(0xFFE0BE63),
    goldDeep = Color(0xFF8C7430),
    text = Color(0xFFF0EBE0),
    textDim = Color(0xFF9A9488),
    outline = Color(0xFF333333),
    str = Color(0xFFE0503F),
    dex = Color(0xFF5FB877),
    con = Color(0xFFD08B3C),
    int = Color(0xFF4FA3E3),
    wis = Color(0xFFA277F0),
    cha = Color(0xFFE8763C),
    low = Color(0xFF5FB877),
    medium = Color(0xFFE0BE63),
    high = Color(0xFFE0503F),
)

val KontrastLight = PaletteSpec(
    bg = Color(0xFFF2F2F0),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFE4E4E1),
    gold = Color(0xFF7A6014),
    goldDeep = Color(0xFF5C4810),
    text = Color(0xFF111111),
    textDim = Color(0xFF5A5A56),
    outline = Color(0xFFB8B8B4),
    str = Color(0xFFB53528),
    dex = Color(0xFF23713C),
    con = Color(0xFF8A5310),
    int = Color(0xFF1B6AA8),
    wis = Color(0xFF6438B8),
    cha = Color(0xFF9E4413),
    low = Color(0xFF23713C),
    medium = Color(0xFF7A6014),
    high = Color(0xFFB53528),
)
