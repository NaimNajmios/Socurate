package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.times
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.ImagePosition
import com.najmi.oreamnos.cardgen.model.PhotoFilter
import com.najmi.oreamnos.cardgen.model.StatItem
import com.najmi.oreamnos.cardgen.ui.DraggableCanvasElement
import com.najmi.oreamnos.cardgen.utils.ColorExtractor
import com.najmi.oreamnos.cardgen.utils.GradientBuilder
import com.najmi.oreamnos.ui.components.AutoSizeText
import com.najmi.oreamnos.ui.theme.SocurateTheme

// ──────────────────────────────────────────────────────────────
// Shared helpers
// ──────────────────────────────────────────────────────────────

/** Standard 2dp border color used on all cards. */
internal val CardBorder = Color.White.copy(alpha = 0.15f)

/** Text colors for card overlays. */
internal val CardTextPrimary = Color.White
internal val CardTextSecondary = Color.White.copy(alpha = 0.75f)
internal val CardTextMuted = Color.White.copy(alpha = 0.55f)

/**
 * Returns scaled sp value based on multiplier.
 */
internal fun Int.scaleSp(multiplier: Float): TextUnit = (this * multiplier).sp

/**
 * Maps [PhotoFilter] to Compose [ColorFilter].
 */
internal fun PhotoFilter.toColorFilter(): ColorFilter? = when (this) {
    PhotoFilter.NONE -> null
    PhotoFilter.BLACK_WHITE -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    PhotoFilter.VINTAGE -> ColorFilter.colorMatrix(ColorMatrix().apply {
        // Subtle sepia/vintage tones
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

/**
 * Returns scaled sp value from a TextUnit based on multiplier.
 */
internal fun TextUnit.scaleSp(multiplier: Float): TextUnit = (this.value * multiplier).sp

/**
 * Applies the background based on [config]'s imagePosition setting.
 * Handles gradient backgrounds, gallery images, and various layout modes.
 */
@Composable
internal fun CardBackground(
    config: CardConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    // Determine custom font family if selected, otherwise fallback to Theme default
    val customFontFamily = when(config.primaryFontFamilyName) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> null
    }

    // Apply scaling, shadows, and optionally override the font family
    val textShadow = if (config.textShadowRadius > 0f) {
        Shadow(
            color = config.textShadowColor,
            offset = if (config.isGlowEnabled) Offset.Zero else Offset(2f, 2f),
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

    MaterialTheme(typography = scaledTypography) {
        CompositionLocalProvider(
            LocalDensity provides Density(density = density.density, fontScale = density.fontScale * config.fontSizeMultiplier)
        ) {
            Box(modifier = modifier) {
                when (config.imagePosition) {
                    ImagePosition.BACKGROUND -> {
                        // Original: Full background with scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(config.colorPair))
                        ) {
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .let { if (config.backgroundBlurRadius > 0f) it.blur(config.backgroundBlurRadius.dp) else it },
                                    contentScale = ContentScale.Crop,
                                    alpha = config.imageOpacity,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                                if (config.showScrim) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.getScrim(config.scrimType, config.overlayOpacity))
                                    )
                                }
                            }
                            content()
                        }
                    }

                    ImagePosition.SPLIT_LEFT -> {
                        // Image on left (60%), text on right - NBA quote card style
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left side - Image
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.55f)
                            ) {
                                if (config.backgroundBitmap != null) {
                                    Image(
                                        bitmap = config.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .let { if (config.backgroundBlurRadius > 0f) it.blur(config.backgroundBlurRadius.dp) else it },
                                        contentScale = ContentScale.Crop,
                                        alpha = config.imageOpacity,
                                        colorFilter = config.photoFilter.toColorFilter()
                                    )
                                    // Horizontal scrim darkening from left to right
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.horizontalScrim(config.overlayOpacity))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.vertical(config.colorPair))
                                    )
                                }
                            }
                            // Right side - Text content with gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.7f),
                                                Color.Black.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            ) {
                                content()
                            }
                        }
                    }

                    ImagePosition.SPLIT_RIGHT -> {
                        // Image on right (60%), text on left - match highlights style
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left side - Text content with gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.45f)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.9f),
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            ) {
                                content()
                            }
                            // Right side - Image
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                            ) {
                                if (config.backgroundBitmap != null) {
                                    Image(
                                        bitmap = config.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .let { if (config.backgroundBlurRadius > 0f) it.blur(config.backgroundBlurRadius.dp) else it },
                                        contentScale = ContentScale.Crop,
                                        alpha = config.imageOpacity,
                                        colorFilter = config.photoFilter.toColorFilter()
                                    )
                                    // Reverse horizontal scrim
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.reverseHorizontalScrim(config.overlayOpacity))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.vertical(config.colorPair))
                                    )
                                }
                            }
                        }
                    }

                    ImagePosition.OVERLAY_TOP -> {
                        // Full-bleed image with text at bottom - player spotlight style
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Full image background
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = config.imageOpacity,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                                if (config.showScrim) {
                                    // Bottom-heavy scrim for text legibility
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.lightScrim(config.overlayOpacity))
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GradientBuilder.vertical(config.colorPair))
                                )
                            }
                            content()
                        }
                    }

                    ImagePosition.CUTOUT -> {
                        // Cutout mode - player image with transparent background, text beside/overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(config.colorPair))
                        ) {
                            // Background image if present (will show through transparent areas)
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.3f,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                            }
                            
                            // Cutout bitmap overlay (transparent PNG of player)
                            if (config.cutoutBitmap != null) {
                                Image(
                                    bitmap = config.cutoutBitmap.asImageBitmap(),
                                    contentDescription = "Player cutout",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentScale = ContentScale.Fit,
                                    alpha = config.imageOpacity
                                )
                            }
                            
                            content()
                        }
                    }

                    ImagePosition.MINIMAL -> {
                        // Subtle background, prominent text
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(config.colorPair))
                        ) {
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = 0.25f },
                                    contentScale = ContentScale.Crop,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                                if (config.showScrim) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.minimalScrim(config.overlayOpacity))
                                    )
                                }
                            }
                            content()
                        }
                    }

                    ImagePosition.MAGAZINE_BOLD -> {
                        // High-impact solid blocks with thick borders
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(config.colorPair))
                        ) {
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = config.imageOpacity,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                                if (config.showScrim) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GradientBuilder.getScrim(config.scrimType, config.overlayOpacity))
                                    )
                                }
                            }
                            // Text is housed in a bold rectangle
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .background(
                                        (config.accentColor ?: config.colorPair.first).copy(alpha = 0.9f),
                                        RoundedCornerShape(0.dp)
                                    )
                                    .border(
                                        4.dp,
                                        Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(0.dp)
                                    )
                            ) {
                                content()
                            }
                        }
                    }

                    ImagePosition.OFFSET_CARD -> {
                        // Inset image with overlapping text card
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.vertical(config.colorPair))
                        ) {
                            // Inset background image
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .border(2.dp, CardBorder)
                            ) {
                                if (config.backgroundBitmap != null) {
                                    Image(
                                        bitmap = config.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = config.imageOpacity,
                                        colorFilter = config.photoFilter.toColorFilter()
                                    )
                                }
                            }
                            
                            // Overlapping text card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.6f)
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(0.dp))
                                    .border(1.dp, CardBorder)
                            ) {
                                content()
                            }
                        }
                    }

                    ImagePosition.BRUTALIST -> {
                        // Raw grid, high contrast, no rounded corners
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(4.dp, Color.White)
                        ) {
                            // Left column: Image or Color
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(0.4f)
                                    .border(4.dp, Color.White)
                                    .background(GradientBuilder.vertical(config.colorPair))
                            ) {
                                if (config.backgroundBitmap != null) {
                                    Image(
                                        bitmap = config.backgroundBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = config.imageOpacity,
                                        colorFilter = config.photoFilter.toColorFilter()
                                    )
                                }
                            }
                            // Right column: Pure black background for text
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(0.6f)
                                    .background(Color.Black)
                            ) {
                                content()
                            }
                        }
                    }

                    ImagePosition.FLOAT_WINDOW -> {
                        // Floating window over blurred background
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Full background image with heavy blur
                            if (config.backgroundBitmap != null) {
                                Image(
                                    bitmap = config.backgroundBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(20.dp),
                                    contentScale = ContentScale.Crop,
                                    alpha = config.imageOpacity,
                                    colorFilter = config.photoFilter.toColorFilter()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GradientBuilder.vertical(config.colorPair))
                                        .blur(20.dp)
                                )
                            }
                            
                            // Floating window
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.9f)
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(0.dp))
                                    .border(2.dp, CardBorder)
                            ) {
                                content()
                            }
                        }
                    }
                }

                // Watermark Branding Overlay
                if (config.watermarkBitmap != null) {
                    Image(
                        bitmap = config.watermarkBitmap.asImageBitmap(),
                        contentDescription = "Watermark",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(60.dp),
                        alpha = 0.6f
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 2. HEADLINE / QUOTE CANVAS
// ──────────────────────────────────────────────────────────────

/**
 * Headline / Quote card with a Canvas-drawn decorative opening quotation mark.
 */
@Composable
fun HeadlineQuoteCanvas(
    data: CardData.HeadlineQuote,
    config: CardConfig,
    modifier: Modifier = Modifier,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit = { _, _ -> }
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f)
    ) {
        DraggableCanvasElement(
            elementId = "headline_quote",
            cardConfig = config,
            onOffsetChange = onOffsetChange,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top branding
                Text(
                    text = "HEADLINE",
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // The Quote
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Large quote mark graphic
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val w = size.width
                        val h = size.height
                        val stroke = Stroke(width = 4.dp.toPx())
                        
                        drawArc(
                            color = Color(0xFFFFD100), // Gold accent
                            startAngle = 180f, sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(0f, 0f),
                            size = Size(w * 0.4f, h * 0.5f),
                            style = stroke
                        )
                        drawArc(
                            color = Color(0xFFFFD100),
                            startAngle = 180f, sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.5f, 0f),
                            size = Size(w * 0.4f, h * 0.5f),
                            style = stroke
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (data.quoteAuthor.isNotBlank()) {
                            Text(
                                text = data.quoteAuthor.uppercase(),
                                color = Color(0xFFFFD100),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        AutoSizeText(
                            text = data.headline.uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                lineHeight = 36.sp
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Bottom spacer removed to anchor content
                
                androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                
                // Bottom Section: Context (Left)
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (data.subtext.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.subtext,
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
/** Draws a stylised double-quotation mark using DrawScope lines. */
private fun drawQuoteMark(scope: DrawScope, color: Color) {
    val strokeWidthPx = with(scope) { 4.dp.toPx() }
    val stroke = Stroke(width = strokeWidthPx)
    val w = scope.size.width
    val h = scope.size.height

    // Left serif mark
    scope.drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(0f, h * 0.1f),
        size = Size(w * 0.35f, h * 0.45f),
        style = stroke
    )

    // Right serif mark
    scope.drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.45f, h * 0.1f),
        size = Size(w * 0.35f, h * 0.45f),
        style = stroke
    )
}

// ──────────────────────────────────────────────────────────────
// 3. PLAYER SPOTLIGHT CANVAS
// ──────────────────────────────────────────────────────────────

/**
 * Player Spotlight card.
 *
 * Uses a gradient background (or gallery image via Coil) with a dark scrim,
 * player info overlaid at the bottom.
 */
@Composable
fun PlayerSpotlightCanvas(
    data: CardData.PlayerSpotlight,
    config: CardConfig,
    modifier: Modifier = Modifier,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit = { _, _ -> }
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f) // Square compact
    ) {
        DraggableCanvasElement(
            elementId = "player_spotlight",
            cardConfig = config,
            onOffsetChange = onOffsetChange,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top branding
                Text(
                    text = "PLAYER SPOTLIGHT",
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (data.keyAction.isNotBlank() && data.keyAction != "—") {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD100), MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = data.keyAction.uppercase(),
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // The Player Name (Center Stage if no photo)
                AutoSizeText(
                    text = data.playerName.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 40.sp
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Bottom spacer removed to anchor content
                
                androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                
                // Bottom Section: Context (Left) and Stats (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Author/Context Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.club.uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = data.position.uppercase(),
                            color = CardTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                        if (data.keyQuote.isNotBlank() && data.keyQuote != "—") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\u201C${data.keyQuote}\u201D",
                                color = CardTextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 4 Key Stats
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val stats = listOf(
                            StatItem(label = "Min", value = "${data.minutesPlayed}", context = ""),
                            StatItem(label = "Gl", value = "${data.goals}", context = ""),
                            StatItem(label = "Ast", value = "${data.assists}", context = ""),
                            StatItem(label = "Rat", value = String.format("%.1f", data.rating), context = "")
                        )
                        stats.forEach { stat ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stat.value,
                                    color = Color(0xFFFFD100), // Gold
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = stat.label.uppercase(),
                                    color = CardTextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 4. TOP STATS CANVAS
// ──────────────────────────────────────────────────────────────

/**
 * Top 3 Stats card — three horizontal stat rows with alternating accent colors.
 */
@Composable
fun TopStatsCanvas(
    data: CardData.TopStats,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top branding
            Text(
                text = "TOP STATS",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Center Content: Huge graphic or just leave blank if there's a photo
            
            // Bottom spacer removed to anchor content
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section: Context (Left) and Stats (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Context Info
                Column(modifier = Modifier.weight(1f)) {
                    if (data.matchContext.isNotBlank()) {
                        Text(
                            text = data.matchContext.uppercase(),
                            color = Color(0xFFFFD100),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = "MATCH STATS",
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 3 Key Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    data.stats.forEach { stat ->
                        if (stat.label.isNotBlank() && stat.label != "—" && stat.value.isNotBlank() && stat.value != "—" && stat.value != "0") {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stat.value,
                                    color = Color(0xFFFFD100), // Gold
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = stat.label.uppercase(),
                                    color = CardTextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




// ──────────────────────────────────────────────────────────────
// @Preview functions
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Headline Quote Card")
@Composable
private fun PreviewHeadlineQuote() {
    SocurateTheme {
        HeadlineQuoteCanvas(
            data = CardData.HeadlineQuote(
                headline = "Harimau Malaya layak ke pusingan akhir Piala AFF",
                subtext = "Kemenangan bersejarah di Stadium Bukit Jalil",
                quoteAuthor = "Kim Pan Gon"
            ),
            config = CardConfig()
        )
    }
}

@Preview(showBackground = true, name = "Player Spotlight Card")
@Composable
private fun PreviewPlayerSpotlight() {
    SocurateTheme {
        PlayerSpotlightCanvas(
            data = CardData.PlayerSpotlight(
                playerName = "Arif Aiman",
                club = "JDT",
                position = "Penjaring",
                rating = 8.5f,
                goals = 2,
                assists = 1,
                minutesPlayed = 90,
                keyAction = "MOTM",
                keyQuote = "Prestasi cemerlang dengan dua gol dan satu aist malam ini"
            ),
            config = CardConfig()
        )
    }
}

@Preview(showBackground = true, name = "Top Stats Card")
@Composable
private fun PreviewTopStats() {
    SocurateTheme {
        TopStatsCanvas(
            data = CardData.TopStats(
                matchContext = "Piala FA Akhir: JDT 2-0 KL City",
                stats = listOf(
                    StatItem(label = "Possession", value = "67%", context = "JDT dominated the match"),
                    StatItem(label = "Shots", value = "24", context = "14 on target"),
                    StatItem(label = "Saves", value = "11", context = "Best goalkeeper of the match")
                )
            ),
            config = CardConfig()
        )
    }
}

