package com.renova.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores personalizados para Renova
data class RenovaColorScheme(
    val surface: androidx.compose.ui.graphics.Color,
    val onSurface: androidx.compose.ui.graphics.Color,
    val cardBackground: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val borderFocused: androidx.compose.ui.graphics.Color,
    val iconTint: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientMid1: androidx.compose.ui.graphics.Color,
    val gradientMid2: androidx.compose.ui.graphics.Color? = null,
    val gradientEnd: androidx.compose.ui.graphics.Color,
    val discoverGradientStart: androidx.compose.ui.graphics.Color,
    val discoverGradientEnd: androidx.compose.ui.graphics.Color,
)

val LightRenovaColors = RenovaColorScheme(
    surface = RenovaColors.Light.Surface,
    onSurface = RenovaColors.Light.OnSurface,
    cardBackground = RenovaColors.Light.CardBackground,
    textPrimary = RenovaColors.Light.TextPrimary,
    textSecondary = RenovaColors.Light.TextSecondary,
    border = RenovaColors.Light.Border,
    borderFocused = RenovaColors.Light.BorderFocused,
    iconTint = RenovaColors.Light.IconTint,
    gradientStart = RenovaColors.Light.GradientStart,
    gradientMid1 = RenovaColors.Light.GradientMid1,
    gradientMid2 = RenovaColors.Light.GradientMid2,
    gradientEnd = RenovaColors.Light.GradientEnd,
    discoverGradientStart = RenovaColors.Light.DiscoverGradientStart,
    discoverGradientEnd = RenovaColors.Light.DiscoverGradientEnd,
    )

val DarkRenovaColors = RenovaColorScheme(
    surface = RenovaColors.Dark.Surface,
    onSurface = RenovaColors.Dark.OnSurface,
    cardBackground = RenovaColors.Dark.CardBackground,
    textPrimary = RenovaColors.Dark.TextPrimary,
    textSecondary = RenovaColors.Dark.TextSecondary,
    border = RenovaColors.Dark.Border,
    borderFocused = RenovaColors.Dark.BorderFocused,
    iconTint = RenovaColors.Dark.IconTint,
    gradientStart = RenovaColors.Dark.GradientStart,
    gradientMid1 = RenovaColors.Dark.GradientMid1,
    gradientEnd = RenovaColors.Dark.GradientEnd,
    discoverGradientStart = RenovaColors.Dark.GradientStart,
    discoverGradientEnd = RenovaColors.Dark.GradientEnd,
)

// CompositionLocal para acceder a los colores desde cualquier composable
val LocalRenovaColors = staticCompositionLocalOf { LightRenovaColors }

private val DarkColorScheme = darkColorScheme(
    primary = RenovaColors.Primary,
    onPrimary = RenovaColors.Dark.OnPrimary,
    secondary = RenovaColors.Secondary,
    onSecondary = RenovaColors.Dark.OnSecondary,
    tertiary = RenovaColors.PrimaryVariant,
    background = RenovaColors.Dark.Background,
    onBackground = RenovaColors.Dark.OnBackground,
    surface = RenovaColors.Dark.Surface,
    onSurface = RenovaColors.Dark.OnSurface,
    error = RenovaColors.Error,
)

private val LightColorScheme = lightColorScheme(
    primary = RenovaColors.Primary,
    onPrimary = RenovaColors.Light.OnPrimary,
    secondary = RenovaColors.Secondary,
    onSecondary = RenovaColors.Light.OnSecondary,
    tertiary = RenovaColors.PrimaryVariant,
    background = RenovaColors.Light.Background,
    onBackground = RenovaColors.Light.OnBackground,
    surface = RenovaColors.Light.Surface,
    onSurface = RenovaColors.Light.OnSurface,
    error = RenovaColors.Error,
)

@Composable
fun RenovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalView.current.context
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val renovaColors = if (darkTheme) DarkRenovaColors else LightRenovaColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalRenovaColors provides renovaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}