package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VeridiaLightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = OnPastelPrimary,
    primaryContainer = PastelSky,
    onPrimaryContainer = PastelPrimary,
    secondary = PastelAccent,
    onSecondary = PastelSurface,
    secondaryContainer = PastelMint,
    onSecondaryContainer = SecondaryGreenDark,
    tertiary = WarningAmber,
    onTertiary = PastelTextMain,
    background = PastelSky,
    onBackground = PastelTextMain,
    surface = PastelSurface,
    onSurface = PastelTextMain,
    surfaceVariant = PastelSubtle,
    onSurfaceVariant = PastelTextSub,
    outline = PastelBorder,
    outlineVariant = PastelBorder,
    error = DangerCrimson,
    onError = PastelSurface,
    errorContainer = PastelDangerBg,
    onErrorContainer = PastelDangerText,
)

val VeridiaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun VeridiaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VeridiaLightColorScheme,
        typography = VeridiaTypography,
        shapes = VeridiaShapes,
        content = content,
    )
}
