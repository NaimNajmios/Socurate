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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SocurateTypography,
        content = content
    )
}
