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
 * Socurate Dark Color Scheme - Premium Deep Navy
 */
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    
    secondary = ElectricBlueLight,
    onSecondary = DeepNavyBackground,
    secondaryContainer = SlateSurface,
    onSecondaryContainer = TextPrimary,
    
    tertiary = TextSecondary,
    onTertiary = DeepNavyBackground,
    tertiaryContainer = Outline,
    onTertiaryContainer = TextTertiary,
    
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    background = DeepNavyBackground,
    onBackground = TextPrimary,
    
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = Outline,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary
)

/**
 * Light theme uses same dark palette for now (app is primarily dark-themed)
 */
private val LightColorScheme = DarkColorScheme

/**
 * Socurate App Theme
 */
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
