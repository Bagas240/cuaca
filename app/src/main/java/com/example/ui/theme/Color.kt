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

// Sunrise / Sunset ("putih kemerahan" warm rose-tinted white)
val SunriseSunsetVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.SUNRISE_SUNSET,
    isDark = false,
    sky = Color(0xFFFFEFEA),
    mint = Color(0xFFFBECEF),
    surface = Color(0xFFFFF9F7),
    subtle = Color(0xFFFCE5DF),
    primary = Color(0xFFC84B47),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF2E8B57),
    textMain = Color(0xFF2B1616),
    textSub = Color(0xFF6B4745),
    border = Color(0xFFF2CBC5),
    dangerBg = Color(0xFFFDE6EA),
    dangerBorder = Color(0xFFF5B6C0),
    dangerText = Color(0xFF9F1A27),
    sunset = Color(0xFF9D3F1D),
    greenText = Color(0xFF145A36),
    warning = Color(0xFFE5862A),
    backdropTop = Color(0xFF5D2645),
    backdropMid = Color(0xFFC65A5E),
    backdropBottom = Color(0xFFFBC7BA),
)

// Day (bright & warm daylight)
val DayVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.DAY,
    isDark = false,
    sky = Color(0xFFEEF5FB),
    mint = Color(0xFFE9F5EF),
    surface = Color(0xFFFFFFFF),
    subtle = Color(0xFFF4F7FA),
    primary = Color(0xFF2C6ECB),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF34C759),
    textMain = Color(0xFF1C1D1F),
    textSub = Color(0xFF4B515D),
    border = Color(0xFFE5E9EE),
    dangerBg = Color(0xFFFFF5F6),
    dangerBorder = Color(0xFFFCD4DA),
    dangerText = Color(0xFFA82020),
    sunset = Color(0xFF7C5200),
    greenText = Color(0xFF0A4822),
    warning = Color(0xFFFFB020),
    backdropTop = Color(0xFF1565C0),
    backdropMid = Color(0xFF4BA3F2),
    backdropBottom = Color(0xFFBBE1FA),
)

// Afternoon / Dusk ("sedikit menggelap" slightly shaded slate-twilight)
val AfternoonDuskVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.AFTERNOON_DUSK,
    isDark = false,
    sky = Color(0xFFC8D3E0),
    mint = Color(0xFFD1E2DC),
    surface = Color(0xFFE5ECF3),
    subtle = Color(0xFFD8E1EB),
    primary = Color(0xFF2A5494),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFF239B4D),
    textMain = Color(0xFF1A2331),
    textSub = Color(0xFF425064),
    border = Color(0xFFB3C2D4),
    dangerBg = Color(0xFFF5DADF),
    dangerBorder = Color(0xFFE5B1BC),
    dangerText = Color(0xFF941B29),
    sunset = Color(0xFF8C4E12),
    greenText = Color(0xFF11522E),
    warning = Color(0xFFDF9216),
    backdropTop = Color(0xFF24344D),
    backdropMid = Color(0xFF566B85),
    backdropBottom = Color(0xFF9BAEC4),
)

// Night ("gelap & sejuk" cool dark mode)
val NightVeridiaPalette = VeridiaColorPalette(
    phase = SolarThemePhase.NIGHT,
    isDark = true,
    sky = Color(0xFF0B1220),
    mint = Color(0xFF122826),
    surface = Color(0xFF152033),
    subtle = Color(0xFF1D2C44),
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF082F49),
    accent = Color(0xFF34D399),
    textMain = Color(0xFFF1F5F9),
    textSub = Color(0xFF9FB0C7),
    border = Color(0xFF2A3C58),
    dangerBg = Color(0xFF321721),
    dangerBorder = Color(0xFF682A39),
    dangerText = Color(0xFFFCA5A5),
    sunset = Color(0xFFFBBF24),
    greenText = Color(0xFF6EE7B7),
    warning = Color(0xFFFBBF24),
    backdropTop = Color(0xFF050914),
    backdropMid = Color(0xFF0F1B33),
    backdropBottom = Color(0xFF182A46),
)

fun paletteForPhase(phase: SolarThemePhase): VeridiaColorPalette = when (phase) {
    SolarThemePhase.SUNRISE_SUNSET -> SunriseSunsetVeridiaPalette
    SolarThemePhase.DAY -> DayVeridiaPalette
    SolarThemePhase.AFTERNOON_DUSK -> AfternoonDuskVeridiaPalette
    SolarThemePhase.NIGHT -> NightVeridiaPalette
}
