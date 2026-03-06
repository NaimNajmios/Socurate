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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.StatItem
import com.najmi.oreamnos.cardgen.model.TeamStats
import com.najmi.oreamnos.cardgen.utils.ColorExtractor
import com.najmi.oreamnos.cardgen.utils.GradientBuilder
import com.najmi.oreamnos.ui.theme.SocurateTheme

// ──────────────────────────────────────────────────────────────
// Shared helpers
// ──────────────────────────────────────────────────────────────

/** Standard 2dp border color used on all cards. */
private val CardBorder = Color.White.copy(alpha = 0.15f)

/** Text colors for card overlays. */
private val CardTextPrimary = Color.White
private val CardTextSecondary = Color.White.copy(alpha = 0.75f)
private val CardTextMuted = Color.White.copy(alpha = 0.55f)

/**
 * Applies the background (gradient or gallery image + scrim) from [config].
 */
@Composable
private fun CardBackground(config: CardConfig, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .background(GradientBuilder.vertical(config.colorPair))
    ) {
        // Gallery bitmap overlay with scrim
        if (config.backgroundBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GradientBuilder.darkScrim)
            )
        }
        content()
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

    CardBackground(
        config = config.copy(colorPair = gradientColors),
        modifier = modifier.aspectRatio(4f / 5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = data.homeTeam.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${data.awayScore}",
                        color = CardTextPrimary,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = data.awayTeam.uppercase(),
                        color = CardTextSecondary,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = data.matchDate.uppercase(),
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )
                    if (data.keyMoment.isNotBlank() && data.keyMoment != "—") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = data.keyMoment,
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Key Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val statNames = listOf("Poss", "Shots", "SOT")
                    val homeVals = listOf("${data.homeStats.possession}%", "${data.homeStats.shots}", "${data.homeStats.shotsOnTarget}")
                    val awayVals = listOf("${data.awayStats.possession}%", "${data.awayStats.shots}", "${data.awayStats.shotsOnTarget}")
                    
                    for (i in 0..2) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = homeVals[i],
                                color = Color(0xFFFFD100), // Gold for Home
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 24.sp
                            )
                            Text(
                                text = awayVals[i],
                                color = CardTextPrimary, // White for Away
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
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
        modifier = modifier.aspectRatio(4f / 5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            
            // Bottom Section: Context (Left)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = data.source.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
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
        modifier = modifier.aspectRatio(4f / 5f) // Portrait
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp
                )
            )
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            
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
                    Spacer(modifier = Modifier.height(8.dp))
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
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 32.sp
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
        modifier = modifier.aspectRatio(4f / 5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            
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
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stat.value,
                                color = Color(0xFFFFD100), // Gold
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 32.sp
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
// Shared footer branding
// ──────────────────────────────────────────────────────────────

@Composable
private fun CardFooter() {
    Text(
        text = "SOCURATE",
        color = CardTextMuted,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 3.sp
    )
}

// ──────────────────────────────────────────────────────────────
// 5. NBA STYLE CANVAS
// ──────────────────────────────────────────────────────────────

/**
 * NBA Style Quote card.
 * Features a large, punchy quote taking center stage, with the author
 * info on bottom-left and 3 key stats explicitly called out on bottom-right.
 */
@Composable
fun NbaStyleCanvas(
    data: CardData.NbaStyleQuote,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(4f / 5f) // Portrait
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top branding
            Text(
                text = "PLAYER QUOTE",
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
                    text = data.quote.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            Divider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            
            // Bottom Section: Author (Left) and Stats (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Author Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.authorName,
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = data.authorContext.uppercase(),
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 3 Key Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    data.stats.forEach { stat ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stat.value,
                                color = Color(0xFFFFD100), // Gold
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 32.sp
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

@Preview(showBackground = true, name = "NBA Style Card")
@Composable
private fun PreviewNbaStyle() {
    SocurateTheme {
        NbaStyleCanvas(
            data = CardData.NbaStyleQuote(
                quote = "We controlled the game but failed to finish our chances.",
                authorName = "Arif Aiman",
                authorContext = "JDT Winger",
                stats = listOf(
                    StatItem(label = "Gl", value = "1", context = ""),
                    StatItem(label = "As", value = "2", context = ""),
                    StatItem(label = "Rat", value = "8.5", context = "")
                )
            ),
            config = CardConfig()
        )
    }
}
