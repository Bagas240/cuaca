package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.example.data.model.SolarThemePhase

val LocalVeridiaColors = compositionLocalOf { DayVeridiaPalette }

val veridiaColors: VeridiaColorPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalVeridiaColors.current

val VeridiaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun VeridiaTheme(
    themePhase: SolarThemePhase = SolarThemePhase.DAY,
    content: @Composable () -> Unit,
) {
    val target = paletteForPhase(themePhase)
    val spec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 520)

    val sky by animateColorAsState(target.sky, spec, label = "theme_sky")
    val mint by animateColorAsState(target.mint, spec, label = "theme_mint")
    val surface by animateColorAsState(target.surface, spec, label = "theme_surface")
    val subtle by animateColorAsState(target.subtle, spec, label = "theme_subtle")
    val primary by animateColorAsState(target.primary, spec, label = "theme_primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "theme_on_primary")
    val accent by animateColorAsState(target.accent, spec, label = "theme_accent")
    val textMain by animateColorAsState(target.textMain, spec, label = "theme_text_main")
    val textSub by animateColorAsState(target.textSub, spec, label = "theme_text_sub")
    val border by animateColorAsState(target.border, spec, label = "theme_border")
    val dangerBg by animateColorAsState(target.dangerBg, spec, label = "theme_danger_bg")
    val dangerBorder by animateColorAsState(target.dangerBorder, spec, label = "theme_danger_border")
    val dangerText by animateColorAsState(target.dangerText, spec, label = "theme_danger_text")
    val sunset by animateColorAsState(target.sunset, spec, label = "theme_sunset")
    val greenText by animateColorAsState(target.greenText, spec, label = "theme_green_text")
    val warning by animateColorAsState(target.warning, spec, label = "theme_warning")
    val backdropTop by animateColorAsState(target.backdropTop, spec, label = "theme_backdrop_top")
    val backdropMid by animateColorAsState(target.backdropMid, spec, label = "theme_backdrop_mid")
    val backdropBottom by animateColorAsState(target.backdropBottom, spec, label = "theme_backdrop_bottom")

    val animatedPalette = VeridiaColorPalette(
        phase = target.phase,
        isDark = target.isDark,
        sky = sky,
        mint = mint,
        surface = surface,
        subtle = subtle,
        primary = primary,
        onPrimary = onPrimary,
        accent = accent,
        textMain = textMain,
        textSub = textSub,
        border = border,
        dangerBg = dangerBg,
        dangerBorder = dangerBorder,
        dangerText = dangerText,
        sunset = sunset,
        greenText = greenText,
        warning = warning,
        backdropTop = backdropTop,
        backdropMid = backdropMid,
        backdropBottom = backdropBottom,
    )

    val colorScheme = if (target.isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = subtle,
            onPrimaryContainer = primary,
            secondary = accent,
            onSecondary = surface,
            secondaryContainer = mint,
            onSecondaryContainer = greenText,
            tertiary = warning,
            onTertiary = textMain,
            background = sky,
            onBackground = textMain,
            surface = surface,
            onSurface = textMain,
            surfaceVariant = subtle,
            onSurfaceVariant = textSub,
            outline = border,
            outlineVariant = border,
            error = DangerCrimson,
            onError = surface,
            errorContainer = dangerBg,
            onErrorContainer = dangerText,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = sky,
            onPrimaryContainer = primary,
            secondary = accent,
            onSecondary = surface,
            secondaryContainer = mint,
            onSecondaryContainer = greenText,
            tertiary = warning,
            onTertiary = textMain,
            background = sky,
            onBackground = textMain,
            surface = surface,
            onSurface = textMain,
            surfaceVariant = subtle,
            onSurfaceVariant = textSub,
            outline = border,
            outlineVariant = border,
            error = DangerCrimson,
            onError = surface,
            errorContainer = dangerBg,
            onErrorContainer = dangerText,
        )
    }

    CompositionLocalProvider(LocalVeridiaColors provides animatedPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VeridiaTypography,
            shapes = VeridiaShapes,
            content = content,
        )
    }
}
