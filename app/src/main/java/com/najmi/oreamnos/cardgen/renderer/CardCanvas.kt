package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.ImagePosition
import com.najmi.oreamnos.cardgen.model.PhotoFilter
import com.najmi.oreamnos.cardgen.utils.ColorExtractor
import com.najmi.oreamnos.cardgen.utils.GradientBuilder

internal val CardBorder = Color.White.copy(alpha = 0.15f)

internal val CardTextPrimary = Color.White
internal val CardTextSecondary = Color.White.copy(alpha = 0.75f)
internal val CardTextMuted = Color.White.copy(alpha = 0.55f)

internal fun Int.scaleSp(multiplier: Float): TextUnit = (this * multiplier).sp

internal fun PhotoFilter.toColorFilter(): ColorFilter? = when (this) {
    PhotoFilter.NONE -> null
    PhotoFilter.BLACK_WHITE -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    PhotoFilter.VINTAGE -> ColorFilter.colorMatrix(ColorMatrix().apply {
        val matrix = floatArrayOf(
            0.9f, 0.5f, 0.1f, 0f, 0f,
            0.3f, 0.8f, 0.1f, 0f, 0f,
            0.2f, 0.3f, 0.5f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        set(ColorMatrix(matrix))
    })
    PhotoFilter.VIBRANT -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.8f) })
    PhotoFilter.HIGH_CONTRAST -> ColorFilter.colorMatrix(ColorMatrix().apply {
        val contrast = 1.5f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val matrix = floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        set(ColorMatrix(matrix))
    })
}

internal fun TextUnit.scaleSp(multiplier: Float): TextUnit = (this.value * multiplier).sp

@Composable
internal fun CardBackground(
    config: CardConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    val customFontFamily = when(config.primaryFontFamilyName) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> null
    }

    val textShadow = if (config.textShadowRadius > 0f) {
        Shadow(
            color = config.textShadowColor,
            offset = if (config.isGlowEnabled) androidx.compose.ui.geometry.Offset.Zero else androidx.compose.ui.geometry.Offset(2f, 2f),
            blurRadius = config.textShadowRadius
        )
    } else null

    val baseTypography = MaterialTheme.typography
    val scaledTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.displayLarge.fontFamily, shadow = textShadow),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.displayMedium.fontFamily, shadow = textShadow),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.displaySmall.fontFamily, shadow = textShadow),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.headlineLarge.fontFamily, shadow = textShadow),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.headlineMedium.fontFamily, shadow = textShadow),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.headlineSmall.fontFamily, shadow = textShadow),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.titleLarge.fontFamily, shadow = textShadow),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.titleMedium.fontFamily, shadow = textShadow),
        titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.titleSmall.fontFamily, shadow = textShadow),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.bodyLarge.fontFamily, shadow = textShadow),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.bodyMedium.fontFamily, shadow = textShadow),
        bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.bodySmall.fontFamily, shadow = textShadow),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.labelLarge.fontFamily, shadow = textShadow),
        labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.labelMedium.fontFamily, shadow = textShadow),
        labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * config.fontSizeMultiplier, fontFamily = customFontFamily ?: baseTypography.labelSmall.fontFamily, shadow = textShadow)
    )

    val configWithPalette = if (config.useAutoPalette && config.backgroundBitmap != null) {
        val extractedPair = ColorExtractor.extractPalette(config.backgroundBitmap)
        config.copy(colorPair = extractedPair)
    } else config

    MaterialTheme(typography = scaledTypography) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density * config.previewScale, 
                fontScale = density.fontScale * config.fontSizeMultiplier
            )
        ) {
            Box(modifier = modifier) {
                when (configWithPalette.imagePosition) {
                    ImagePosition.BACKGROUND -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(configWithPalette.colorPair))
                        ) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(
                                    bitmap = configWithPalette.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .let { if (configWithPalette.backgroundBlurRadius > 0f) it.blur(configWithPalette.backgroundBlurRadius.dp) else it },
                                    contentScale = ContentScale.Crop,
                                    alpha = configWithPalette.imageOpacity,
                                    colorFilter = configWithPalette.photoFilter.toColorFilter()
                                )
                                if (configWithPalette.showScrim) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.getScrim(configWithPalette.scrimType, configWithPalette.overlayOpacity))
                                    )
                                }
                            }
                            content()
                        }
                    }

                    ImagePosition.SPLIT_LEFT -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.55f)) {
                                if (configWithPalette.backgroundBitmap != null) {
                                    Image(
                                        bitmap = configWithPalette.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().let { if (configWithPalette.backgroundBlurRadius > 0f) it.blur(configWithPalette.backgroundBlurRadius.dp) else it },
                                        contentScale = ContentScale.Crop,
                                        alpha = configWithPalette.imageOpacity,
                                        colorFilter = configWithPalette.photoFilter.toColorFilter()
                                    )
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.horizontalScrim(configWithPalette.overlayOpacity)))
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair)))
                                }
                            }
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.9f))))) {
                                content()
                            }
                        }
                    }

                    ImagePosition.SPLIT_RIGHT -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.45f).background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.7f))))) {
                                content()
                            }
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth()) {
                                if (configWithPalette.backgroundBitmap != null) {
                                    Image(
                                        bitmap = configWithPalette.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().let { if (configWithPalette.backgroundBlurRadius > 0f) it.blur(configWithPalette.backgroundBlurRadius.dp) else it },
                                        contentScale = ContentScale.Crop,
                                        alpha = configWithPalette.imageOpacity,
                                        colorFilter = configWithPalette.photoFilter.toColorFilter()
                                    )
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.reverseHorizontalScrim(configWithPalette.overlayOpacity)))
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair)))
                                }
                            }
                        }
                    }

                    ImagePosition.OVERLAY_TOP -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = configWithPalette.imageOpacity, colorFilter = configWithPalette.photoFilter.toColorFilter())
                                if (configWithPalette.showScrim) {
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.lightScrim(configWithPalette.overlayOpacity)))
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair)))
                            }
                            content()
                        }
                    }

                    ImagePosition.CUTOUT -> {
                        Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair))) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f, colorFilter = configWithPalette.photoFilter.toColorFilter())
                            }
                            if (configWithPalette.cutoutBitmap != null) {
                                Image(bitmap = configWithPalette.cutoutBitmap.asImageBitmap(), contentDescription = "Player cutout", modifier = Modifier.fillMaxSize().padding(16.dp), contentScale = ContentScale.Fit, alpha = configWithPalette.imageOpacity)
                            }
                            content()
                        }
                    }

                    ImagePosition.MINIMAL -> {
                        Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair))) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.25f }, contentScale = ContentScale.Crop, colorFilter = configWithPalette.photoFilter.toColorFilter())
                                if (configWithPalette.showScrim) {
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.minimalScrim(configWithPalette.overlayOpacity)))
                                }
                            }
                            content()
                        }
                    }

                    ImagePosition.MAGAZINE_BOLD -> {
                        Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair))) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = configWithPalette.imageOpacity, colorFilter = configWithPalette.photoFilter.toColorFilter())
                                if (configWithPalette.showScrim) {
                                    Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.getScrim(configWithPalette.scrimType, configWithPalette.overlayOpacity)))
                                }
                            }
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp).background((configWithPalette.accentColor ?: configWithPalette.colorPair.first).copy(alpha = 0.9f), RoundedCornerShape(0.dp)).border(4.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(0.dp))) {
                                content()
                            }
                        }
                    }

                    ImagePosition.OFFSET_CARD -> {
                        Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair))) {
                            Box(modifier = Modifier.fillMaxSize(0.85f).align(Alignment.TopStart).padding(16.dp).border(2.dp, CardBorder)) {
                                if (configWithPalette.backgroundBitmap != null) {
                                    Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = configWithPalette.imageOpacity, colorFilter = configWithPalette.photoFilter.toColorFilter())
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.6f).align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(0.dp)).border(1.dp, CardBorder)) {
                                content()
                            }
                        }
                    }

                    ImagePosition.BRUTALIST -> {
                        Row(modifier = Modifier.fillMaxSize().border(4.dp, Color.White)) {
                            Box(modifier = Modifier.fillMaxHeight().weight(0.4f).border(4.dp, Color.White).background(GradientBuilder.vertical(configWithPalette.colorPair))) {
                                if (configWithPalette.backgroundBitmap != null) {
                                    Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = configWithPalette.imageOpacity, colorFilter = configWithPalette.photoFilter.toColorFilter())
                                }
                            }
                            Box(modifier = Modifier.fillMaxHeight().weight(0.6f).background(Color.Black)) {
                                content()
                            }
                        }
                    }

                    ImagePosition.FLOAT_WINDOW -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (configWithPalette.backgroundBitmap != null) {
                                Image(bitmap = configWithPalette.backgroundBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().blur(20.dp), contentScale = ContentScale.Crop, alpha = configWithPalette.imageOpacity, colorFilter = configWithPalette.photoFilter.toColorFilter())
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(GradientBuilder.vertical(configWithPalette.colorPair)).blur(20.dp))
                            }
                            Box(modifier = Modifier.fillMaxSize(0.9f).align(Alignment.Center).background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(0.dp)).border(2.dp, CardBorder)) {
                                content()
                            }
                        }
                    }
                }

                if (configWithPalette.watermarkBitmap != null && configWithPalette.isWatermarkEnabled) {
                    Image(
                        bitmap = configWithPalette.watermarkBitmap.asImageBitmap(),
                        contentDescription = "Watermark",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(configWithPalette.watermarkSize.dp),
                        alpha = 0.6f
                    )
                }

                if (!configWithPalette.badgeText.isNullOrBlank()) {
                    CardBadge(
                        text = configWithPalette.badgeText,
                        color = configWithPalette.accentColor ?: configWithPalette.colorPair.first,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = if (color.luminance() > 0.5f) Color.Black else Color.White
        )
    }
}
