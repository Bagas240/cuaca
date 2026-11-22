package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.data.model.SolarThemePhase

// Base Veridia Atmospheric Telemetry Palette Constants (legacy/static accents)
val PastelSky = Color(0xFFE8F3FA)
val PastelMint = Color(0xFFE9F5EF)
val PastelSurface = Color(0xFFFFFFFF)
val PastelSubtle = Color(0xFFF4F7F9)
val PastelPrimary = Color(0xFF2C6ECB)
val OnPastelPrimary = Color(0xFFFFFFFF)
val PastelAccent = Color(0xFF34C759)
val PastelTextMain = Color(0xFF1C1D1F)
val PastelTextSub = Color(0xFF4B515D)
val PastelBorder = Color(0xFFE5E9EE)

val PastelDangerBg = Color(0xFFFFF5F6)
val PastelDangerBorder = Color(0xFFFCD4DA)
val PastelDangerText = Color(0xFFA82020)
val PastelSunset = Color(0xFF7C5200)

val WarningAmber = Color(0xFFFFB020)
val DangerCrimson = Color(0xFFFF3366)
val SecondaryGreenDark = Color(0xFF0A4822)
val SecondaryGreenBright = Color(0xFF38EF7D)
val NeonPurple = Color(0xFFC04BF8)
val SkyHorizonDark = Color(0xFF1C2834)
val RoadSheenDark = Color(0xFF8EA6BA)
val RoadSheenLight = Color(0xFFA8BFD1)
val CoastlineBlue = Color(0xFF88A8C2)

@Immutable
data class VeridiaColorPalette(
    val phase: SolarThemePhase,
    val isDark: Boolean,
    val sky: Color,
    val mint: Color,
    val surface: Color,
    val subtle: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val textMain: Color,
    val textSub: Color,
    val border: Color,
    val dangerBg: Color,
    val dangerBorder: Color,
    val dangerText: Color,
    val sunset: Color,
    val greenText: Color,
    val warning: Color,
    val backdropTop: Color,
    val backdropMid: Color,
    val backdropBottom: Color,
)

// Sunrise / Sunset ("kuning keputihan" luminous whitish-yellow dawn/golden hour)
val SunriseSunsetVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.SUNRISE_SUNSET,
    isDark = false,
    sky = Color(0xFFFFF5C8),
    mint = Color(0xFFF1F5D6),
    surface = Color(0xFFFFFCEF),
    subtle = Color(0xFFFCEEAF),
    primary = Color(0xFFB36800),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF2E8B57),
    textMain = Color(0xFF241C06),
    textSub = Color(0xFF665326),
    border = Color(0xFFEAD48A),
    dangerBg = Color(0xFFFDE8E6),
    dangerBorder = Color(0xFFF5BCB6),
    dangerText = Color(0xFF9F1A27),
    sunset = Color(0xFF8A4900),
    greenText = Color(0xFF145A36),
    warning = Color(0xFFD97706),
    backdropTop = Color(0xFF435387),
    backdropMid = Color(0xFFE5AC4D),
    backdropBottom = Color(0xFFFFEA9F),
)

// Morning / Day ("putih tapi ada kuning-kuningnya" warm yellow-tinted white)
val DayVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.DAY,
    isDark = false,
    sky = Color(0xFFFAF1D0),
    mint = Color(0xFFEBF4D8),
    surface = Color(0xFFFFFCEB),
    subtle = Color(0xFFF3E9C2),
    primary = Color(0xFF1F5EB8),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF2FA84F),
    textMain = Color(0xFF1E1A0D),
    textSub = Color(0xFF5C5234),
    border = Color(0xFFE5D8A8),
    dangerBg = Color(0xFFFFF0EC),
    dangerBorder = Color(0xFFF7C9BE),
    dangerText = Color(0xFFA82020),
    sunset = Color(0xFF8A5000),
    greenText = Color(0xFF0A4822),
    warning = Color(0xFFD98200),
    backdropTop = Color(0xFF1A6AC4),
    backdropMid = Color(0xFF5CA8E8),
    backdropBottom = Color(0xFFEAE1B0),
)

// Afternoon / Sore ("biru kebiruan" cool atmospheric bluish theme)
val AfternoonDuskVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.AFTERNOON_DUSK,
    isDark = false,
    sky = Color(0xFFC5DDF8),
    mint = Color(0xFFCAE6EA),
    surface = Color(0xFFE2EEFC),
    subtle = Color(0xFFD0E3FA),
    primary = Color(0xFF1D4ED8),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF1E9B63),
    textMain = Color(0xFF0E1D36),
    textSub = Color(0xFF2E4B73),
    border = Color(0xFF94B9E8),
    dangerBg = Color(0xFFF5DADF),
    dangerBorder = Color(0xFFE5B1BC),
    dangerText = Color(0xFF941B29),
    sunset = Color(0xFF1E40AF),
    greenText = Color(0xFF0F4C3A),
    warning = Color(0xFFD98212),
    backdropTop = Color(0xFF133369),
    backdropMid = Color(0xFF356FB8),
    backdropBottom = Color(0xFF9AC2F2),
)

// Night ("gelap keseluruhan" deep obsidian dark mode)
val NightVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.NIGHT,
    isDark = true,
    sky = Color(0xFF060B16),
    mint = Color(0xFF0F2625),
    surface = Color(0xFF101A2C),
    subtle = Color(0xFF17253D),
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF082F49),
    accent = Color(0xFF34D399),
    textMain = Color(0xFFF1F5F9),
    textSub = Color(0xFF9FB2CE),
    border = Color(0xFF263856),
    dangerBg = Color(0xFF2E141D),
    dangerBorder = Color(0xFF682A39),
    dangerText = Color(0xFFFCA5A5),
    sunset = Color(0xFFFBBF24),
    greenText = Color(0xFF6EE7B7),
    warning = Color(0xFFFBBF24),
    backdropTop = Color(0xFF030712),
    backdropMid = Color(0xFF0C1830),
    backdropBottom = Color(0xFF152542),
)

fun paletteForPhase(phase: SolarThemePhase): VeridiaColorPalette = when (phase) {
    SolarThemePhase.SUNRISE_SUNSET -> SunriseSunsetVeridiaPalette
    SolarThemePhase.DAY -> DayVeridiaPalette
    SolarThemePhase.AFTERNOON_DUSK -> AfternoonDuskVeridiaPalette
    SolarThemePhase.NIGHT -> NightVeridiaPalette
}
