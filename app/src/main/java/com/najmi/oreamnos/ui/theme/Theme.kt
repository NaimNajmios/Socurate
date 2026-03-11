package com.najmi.oreamnos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Socurate Neo-Editorial Theme
 * High contrast, bold accent.
 */

private val DarkColorScheme = darkColorScheme(
    primary = InternationalOrange,
    onPrimary = NeoWhite,
    primaryContainer = NeoDarkGrey,
    onPrimaryContainer = NeoWhite,
    
    secondary = NeoWhite,
    onSecondary = NeoBlack,
    secondaryContainer = NeoDarkGrey,
    onSecondaryContainer = NeoWhite,
    
    tertiary = NeoLightGrey,
    onTertiary = NeoBlack,
    tertiaryContainer = NeoDarkGrey,
    onTertiaryContainer = NeoWhite,
    
    error = ErrorRed,
    onError = NeoWhite,
    
    background = NeoBlack,
    onBackground = NeoWhite,
    
    surface = NeoBlack,
    onSurface = NeoWhite,
    surfaceVariant = NeoDarkGrey,
    onSurfaceVariant = NeoWhite,
    
    outline = NeoWhite,
    inverseSurface = NeoWhite,
    inverseOnSurface = NeoBlack,
    inversePrimary = InternationalOrangeDark
)

private val DeepBlueColorScheme = darkColorScheme(
    primary = DeepBlueAccent,
    onPrimary = DeepBlueOnSurface,
    primaryContainer = DeepBlueSurface,
    onPrimaryContainer = DeepBlueOnSurface,
    
    secondary = DeepBlueAccent,
    onSecondary = DeepBlueBackground,
    secondaryContainer = DeepBlueSurface,
    onSecondaryContainer = DeepBlueOnSurface,
    
    tertiary = DeepBlueBorder,
    onTertiary = DeepBlueOnSurface,
    tertiaryContainer = DeepBlueSurface,
    onTertiaryContainer = DeepBlueOnSurface,
    
    error = ErrorRed,
    onError = DeepBlueOnSurface,
    
    background = DeepBlueBackground,
    onBackground = DeepBlueOnSurface,
    
    surface = DeepBlueSurface,
    onSurface = DeepBlueOnSurface,
    surfaceVariant = DeepBlueSurface,
    onSurfaceVariant = DeepBlueOnSurface,
    
    outline = DeepBlueBorder,
    inverseSurface = DeepBlueOnSurface,
    inverseOnSurface = DeepBlueBackground,
    inversePrimary = DeepBlueAccent
)

private val MidnightColorScheme = darkColorScheme(
    primary = NeoWhite,
    onPrimary = NeoBlack,
    primaryContainer = MidnightGrey,
    onPrimaryContainer = NeoWhite,
    secondary = NeoLightGrey,
    onSecondary = NeoBlack,
    background = MidnightBlack,
    onBackground = NeoWhite,
    surface = MidnightBlack,
    onSurface = NeoWhite,
    surfaceVariant = MidnightGrey,
    onSurfaceVariant = NeoWhite,
    outline = MidnightAccent
)

private val SolarizedColorScheme = lightColorScheme(
    primary = SolarizedAccent,
    onPrimary = NeoWhite,
    primaryContainer = SolarizedSurface,
    onPrimaryContainer = SolarizedOnSurface,
    secondary = SolarizedSecondary,
    onSecondary = NeoWhite,
    background = SolarizedBackground,
    onBackground = SolarizedOnSurface,
    surface = SolarizedSurface,
    onSurface = SolarizedOnSurface,
    surfaceVariant = SolarizedSurface,
    onSurfaceVariant = SolarizedOnSurface,
    outline = SolarizedOnSurface
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = NeoBlack,
    primaryContainer = CyberpunkSurface,
    onPrimaryContainer = NeoWhite,
    secondary = CyberpunkSecondary,
    onSecondary = NeoBlack,
    tertiary = CyberpunkTertiary,
    onTertiary = NeoBlack,
    background = CyberpunkBackground,
    onBackground = NeoWhite,
    surface = CyberpunkSurface,
    onSurface = NeoWhite,
    surfaceVariant = CyberpunkSurface,
    onSurfaceVariant = NeoWhite,
    outline = CyberpunkPrimary
)

private val MatchdayColorScheme = lightColorScheme(
    primary = MatchdayAccent,
    onPrimary = NeoWhite,
    primaryContainer = MatchdaySurface,
    onPrimaryContainer = MatchdaySecondary,
    secondary = MatchdaySecondary,
    onSecondary = NeoWhite,
    background = MatchdayBackground,
    onBackground = MatchdaySecondary,
    surface = MatchdaySurface,
    onSurface = MatchdaySecondary,
    surfaceVariant = NeoOffWhite,
    onSurfaceVariant = MatchdaySecondary,
    outline = MatchdayAccent
)

private val LightColorScheme = lightColorScheme(
    primary = InternationalOrange,
    onPrimary = NeoWhite,
    primaryContainer = NeoOffWhite,
    onPrimaryContainer = NeoBlack,
    
    secondary = NeoBlack,
    onSecondary = NeoWhite,
    secondaryContainer = NeoLightGrey,
    onSecondaryContainer = NeoBlack,
    
    tertiary = NeoDarkGrey,
    onTertiary = NeoWhite,
    tertiaryContainer = NeoLightGrey,
    onTertiaryContainer = NeoBlack,
    
    error = ErrorRed,
    onError = NeoWhite,
    
    background = NeoWhite,
    onBackground = NeoBlack,
    
    surface = NeoWhite,
    onSurface = NeoBlack,
    surfaceVariant = NeoOffWhite,
    onSurfaceVariant = NeoBlack,
    
    outline = NeoBlack,
    inverseSurface = NeoBlack,
    inverseOnSurface = NeoWhite,
    inversePrimary = InternationalOrange
)

@Composable
fun SocurateTheme(
    themeMode: String = "system", // system, light, dark, deep_blue
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "light" -> LightColorScheme
        "dark" -> DarkColorScheme
        "deep_blue" -> DeepBlueColorScheme
        "midnight" -> MidnightColorScheme
        "solarized" -> SolarizedColorScheme
        "cyberpunk" -> CyberpunkColorScheme
        "matchday" -> MatchdayColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = themeMode == "light" || (!darkTheme && themeMode == "system")
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SocurateTypography,
        content = content
    )
}
