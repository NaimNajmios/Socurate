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
        modifier = modifier.aspectRatio(1f) // Square by default; caller can override
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Text(
                    text = data.competition.uppercase(),
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp
                )
                Text(
                    text = data.matchDate,
                    color = CardTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Score section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = data.homeTeam.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${data.homeScore}",
                        color = CardTextPrimary,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp
                    )
                    Text(
                        text = "VS",
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "${data.awayScore}",
                        color = CardTextPrimary,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp
                    )
                }

                // Away team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = data.awayTeam.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Key moment
            if (data.keyMoment.isNotBlank() && data.keyMoment != "—") {
                Text(
                    text = "\"${data.keyMoment}\"",
                    color = CardTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Divider(color = CardBorder, thickness = 1.dp)

            // Stats row: Home
            StatRowSection(label = data.homeTeam, stats = data.homeStats)
            // Stats row: Away
            StatRowSection(label = data.awayTeam, stats = data.awayStats)

            // Footer branding
            CardFooter()
        }
    }
}

@Composable
private fun StatRowSection(label: String, stats: TeamStats) {
    Column {
        Text(
            text = label.uppercase(),
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell(value = "${stats.possession}%", label = "Milikan")
            StatCell(value = "${stats.shots}", label = "Tendangan")
            StatCell(value = "${stats.shotsOnTarget}", label = "Tepat")
        }
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = CardTextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall
        )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Decorative large quote mark via Canvas
            Canvas(modifier = Modifier.size(80.dp)) {
                drawQuoteMark(this, color = Color.White.copy(alpha = 0.15f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = "\u201C${data.headline}\u201D",
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    ),
                    textAlign = TextAlign.Center
                )

                if (data.subtext.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = data.subtext,
                        color = CardTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Source credit at bottom-right
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = data.source.uppercase(),
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    CardFooter()
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(4f / 5f) // Portrait
            .background(GradientBuilder.vertical(config.colorPair))
    ) {
        // Gallery background image
        if (config.backgroundBitmap != null) {
            Image(
                bitmap = config.backgroundBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Dark scrim for legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GradientBuilder.darkScrim)
        )

        // Rating badge: top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            RatingBadge(rating = data.rating)
        }

        // Player info: bottom overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = data.position.uppercase(),
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp
            )
            Text(
                text = data.playerName,
                color = CardTextPrimary,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = data.club.uppercase(),
                color = CardTextSecondary,
                style = MaterialTheme.typography.titleSmall
            )

            Divider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Stats row: goals and assists
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCell(value = "${data.goals}", label = "Gol")
                StatCell(value = "${data.assists}", label = "Aist")
                Spacer(Modifier.width(1.dp)) // padding filler
            }

            if (data.keyQuote.isNotBlank() && data.keyQuote != "—") {
                Text(
                    text = "\u201C${data.keyQuote}\u201D",
                    color = CardTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            CardFooter()
        }
    }
}

@Composable
private fun RatingBadge(rating: Float) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFFFD100),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = String.format("%.1f", rating),
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
        )
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
    // Accent colors alternating per row
    val accents = listOf(
        Color(0xFFFF4500), // International Orange
        Color(0xFFFFD100), // Gold
        Color(0xFF00C853)  // Sharp Green
    )

    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STATISTIK TERBAIK",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(8.dp))

            data.stats.forEachIndexed { index, item ->
                StatRowCard(
                    item = item,
                    accent = accents.getOrElse(index) { accents[0] }
                )
                if (index < data.stats.size - 1) {
                    Divider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(Modifier.weight(1f))
            CardFooter()
        }
    }
}

@Composable
private fun StatRowCard(item: StatItem, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label.uppercase(),
                color = CardTextSecondary,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp
            )
            if (item.context.isNotBlank()) {
                Text(
                    text = item.context,
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Text(
            text = item.value,
            color = accent,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 48.sp
        )
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
                    StatItem(label = "Milikan Bola", value = "67%", context = "JDT menguasai permainan"),
                    StatItem(label = "Tendangan", value = "24", context = "14 tepat sasaran"),
                    StatItem(label = "Simpanan", value = "11", context = "Kiper terbaik perlawanan")
                )
            ),
            config = CardConfig()
        )
    }
}
