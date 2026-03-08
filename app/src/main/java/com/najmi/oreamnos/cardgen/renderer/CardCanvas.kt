package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.ImagePosition
import com.najmi.oreamnos.cardgen.model.StatItem
import com.najmi.oreamnos.cardgen.model.TeamStats
import com.najmi.oreamnos.cardgen.utils.ColorExtractor
import com.najmi.oreamnos.cardgen.utils.GradientBuilder
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
    CompositionLocalProvider(
        LocalDensity provides Density(density = density.density, fontScale = density.fontScale * config.fontSizeMultiplier)
    ) {
        when (config.imagePosition) {
        ImagePosition.BACKGROUND -> {
            // Original: Full background with scrim
            Box(
                modifier = modifier
                    .background(GradientBuilder.vertical(config.colorPair))
            ) {
                if (config.backgroundBitmap != null) {
                    Image(
                        bitmap = config.backgroundBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = config.imageOpacity
                    )
                    if (config.showScrim) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.getScrim(config.scrimType))
                        )
                    }
                }
                content()
            }
        }

        ImagePosition.SPLIT_LEFT -> {
            // Image on left (60%), text on right - NBA quote card style
            Box(modifier = modifier) {
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
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = config.imageOpacity
                            )
                            // Horizontal scrim darkening from left to right
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GradientBuilder.horizontalScrim)
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
        }

        ImagePosition.SPLIT_RIGHT -> {
            // Image on right (60%), text on left - match highlights style
            Box(modifier = modifier) {
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
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = config.imageOpacity
                            )
                            // Reverse horizontal scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GradientBuilder.reverseHorizontalScrim)
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
        }

        ImagePosition.OVERLAY_TOP -> {
            // Full-bleed image with text at bottom - player spotlight style
            Box(modifier = modifier.fillMaxSize()) {
                // Full image background
                if (config.backgroundBitmap != null) {
                    Image(
                        bitmap = config.backgroundBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = config.imageOpacity
                    )
                    if (config.showScrim) {
                        // Bottom-heavy scrim for text legibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.lightScrim)
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
                modifier = modifier
                    .background(GradientBuilder.vertical(config.colorPair))
            ) {
                // Background image if present (will show through transparent areas)
                if (config.backgroundBitmap != null) {
                    Image(
                        bitmap = config.backgroundBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f
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
                modifier = modifier
                    .background(GradientBuilder.vertical(config.colorPair))
            ) {
                if (config.backgroundBitmap != null) {
                    Image(
                        bitmap = config.backgroundBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 0.25f },
                        contentScale = ContentScale.Crop
                    )
                    if (config.showScrim) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GradientBuilder.minimalScrim)
                        )
                    }
                }
                content()
            }
        }
    }
    }
}

// ──────────────────────────────────────────────────────────────
// 1. MATCH RESULT CANVAS
// ──────────────────────────────────────────────────────────────

/**
 * Match Result card composable.
 *
 * Layout:
 * - Competition name + date (top)
 * - Two-column team section with score in center
 * - Horizontal divider
 * - 3-column stat row per team (possession, shots, shots on target)
 * - Branding footer
 */
@Composable
fun MatchResultCanvas(
    data: CardData.MatchResult,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    val gradientColors = ColorExtractor.getMatchColors(data.homeTeam, data.awayTeam)
    val scale = config.fontSizeMultiplier

    CardBackground(
        config = config.copy(colorPair = gradientColors),
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top branding
            Text(
                text = "MATCH RESULT",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Center content - Large Score
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${data.homeScore}",
                        color = Color(0xFFFFD100),
                        fontSize = 56.sp.scaleSp(scale),
                        fontWeight = FontWeight.Black,
                        lineHeight = 56.sp.scaleSp(scale)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = data.homeTeam.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize.scaleSp(scale)
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${data.awayScore}",
                        color = CardTextPrimary,
                        fontSize = 56.sp.scaleSp(scale),
                        fontWeight = FontWeight.Black,
                        lineHeight = 56.sp.scaleSp(scale)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = data.awayTeam.uppercase(),
                        color = CardTextSecondary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize.scaleSp(scale)
                        )
                    )
                }
            }
            
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
                    Text(
                        text = data.competition.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize.scaleSp(scale)
                        )
                    )
                    if (data.matchDate.isNotBlank()) {
                        Text(
                            text = data.matchDate.uppercase(),
                            color = CardTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                    }
                    if (data.keyMoment.isNotBlank() && data.keyMoment != "—") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = data.keyMoment,
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CardFooter()
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Key Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val statNames = listOf("Poss", "Shots", "SOT")
                    val homeVals = listOf("${data.homeStats.possession}%", "${data.homeStats.shots}", "${data.homeStats.shotsOnTarget}")
                    val awayVals = listOf("${data.awayStats.possession}%", "${data.awayStats.shots}", "${data.awayStats.shotsOnTarget}")
                    
                    for (i in 0..2) {
                        if (homeVals[i] != "0%" && awayVals[i] != "0%" && homeVals[i] != "0" && awayVals[i] != "0") {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = homeVals[i],
                                    color = Color(0xFFFFD100), // Gold for Home
                                    fontSize = 22.sp.scaleSp(scale),
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 22.sp.scaleSp(scale)
                                )
                                Text(
                                    text = awayVals[i],
                                    color = CardTextPrimary, // White for Away
                                    fontSize = 18.sp.scaleSp(scale),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp.scaleSp(scale)
                                )
                                Text(
                                    text = statNames[i].uppercase(),
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
// 2. HEADLINE / QUOTE CANVAS
// ──────────────────────────────────────────────────────────────
    
/**
 * Headline / Quote card with a Canvas-drawn decorative opening quotation mark.
 */
@Composable
fun HeadlineQuoteCanvas(
    data: CardData.HeadlineQuote,
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = data.headline.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp
                    )
                )
            }
            
            // Bottom spacer removed to anchor content
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section: Context (Left)
            Column(modifier = Modifier.fillMaxWidth()) {
                if (data.source.isNotBlank()) {
                    Text(
                        text = data.source.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (data.subtext.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.subtext,
                        color = CardTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CardFooter()
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
    modifier: Modifier = Modifier
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f) // Square compact
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
            
            // The Player Name (Center Stage if no photo)
            Text(
                text = data.playerName.uppercase(),
                color = CardTextPrimary,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 40.sp
                )
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
                    CardFooter()
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 3 Key Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val stats = listOf(
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
                    Text(
                        text = "MATCH STATS",
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
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
// Shared footer branding
// ──────────────────────────────────────────────────────────────

@Composable
internal fun CardFooter() {
    Text(
        text = "SOCURATE",
        color = CardTextMuted,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 3.sp
    )
}


// ──────────────────────────────────────────────────────────────
// @Preview functions
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Match Result Card")
@Composable
private fun PreviewMatchResult() {
    SocurateTheme {
        MatchResultCanvas(
            data = CardData.MatchResult(
                homeTeam = "JDT",
                awayTeam = "Selangor",
                homeScore = 3,
                awayScore = 1,
                competition = "Liga Super",
                matchDate = "5 Mac 2026",
                homeStats = TeamStats(possession = 58, shots = 14, shotsOnTarget = 8),
                awayStats = TeamStats(possession = 42, shots = 9, shotsOnTarget = 3),
                keyMoment = "JDT dominasi babak pertama dengan tiga gol tanpa balas"
            ),
            config = CardConfig()
        )
    }
}

@Preview(showBackground = true, name = "Headline Quote Card")
@Composable
private fun PreviewHeadlineQuote() {
    SocurateTheme {
        HeadlineQuoteCanvas(
            data = CardData.HeadlineQuote(
                headline = "Harimau Malaya layak ke pusingan akhir Piala AFF",
                subtext = "Kemenangan bersejarah di Stadium Bukit Jalil",
                source = "Utusan Malaysia"
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

