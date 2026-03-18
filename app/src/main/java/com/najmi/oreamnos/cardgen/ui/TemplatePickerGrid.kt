package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardTemplate

@Composable
fun TemplatePickerGrid(
    selectedTemplate: CardTemplate,
    onTemplateSelected: (CardTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CardTemplate.all) { template ->
            TemplatePreviewCard(
                template = template,
                isSelected = template == selectedTemplate,
                onClick = { onTemplateSelected(template) }
            )
        }
    }
}

@Composable
private fun TemplatePreviewCard(
    template: CardTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                          else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Mini preview canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(0.dp))
                    .background(template.previewGradient)
            ) {
                // Simplified template visualization
                TemplatePreviewContent(template = template)
                
                // Selection indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = template.displayName.uppercase(),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = template.description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TemplatePreviewContent(template: CardTemplate) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        when (template) {
            CardTemplate.HeadlineQuote -> HeadlinePreview()
            CardTemplate.PlayerSpotlight -> PlayerSpotlightPreview()
            CardTemplate.TopStats -> TopStatsPreview()
            CardTemplate.TransferNews -> TransferPreview()
            CardTemplate.BreakingNews -> BreakingPreview()
            CardTemplate.MatchPreview -> MatchPreviewPreview()
            CardTemplate.DetailedScoreboard -> ScoreboardPreview()
            CardTemplate.OnThisDay -> OnThisDayPreview()
            CardTemplate.StartingXI -> LineupPreview()
            CardTemplate.MatchStatsComparison -> ComparisonPreview()
            CardTemplate.SocialPost -> SocialPreview()
            CardTemplate.Rivalry -> RivalryPreview()
            CardTemplate.TableStandings -> TableStandingsPreview()
            CardTemplate.InjuryReport -> InjuryReportPreview()
            CardTemplate.ContractExpiry -> ContractExpiryPreview()
            CardTemplate.AwardNominee -> AwardNomineePreview()
        }
    }
}

@Composable
private fun HeadlinePreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "HEADLINE",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 6.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(8.dp)) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFFFD100),
                        startAngle = 180f, sweepAngle = 180f,
                        useCenter = false,
                        size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TAJUK UTAMA",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 3,
                lineHeight = 9.sp
            )
        }
    }
}

@Composable
private fun PlayerSpotlightPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "PLAYER",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 6.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "PLAYER NAME",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "CLUB • POS",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 6.sp
        )
        Row {
            Text(text = "G: 0", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "A: 0", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TopStatsPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        repeat(3) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "STAT", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                Text(text = "00%", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TransferPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFD100), RoundedCornerShape(0.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(text = "SIGNED", color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "PLAYER NAME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text(text = "CLUB → CLUB", color = Color.White.copy(alpha = 0.7f), fontSize = 6.sp)
    }
}

@Composable
private fun BreakingPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "🚨 BREAKING", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(text = "URGENT NEWS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 2)
    }
}

@Composable
private fun MatchPreviewPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "PREVIEW", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "HOME", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "VS", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(text = "AWAY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "League • Time", color = Color.White.copy(alpha = 0.7f), fontSize = 6.sp)
    }
}

@Composable
private fun ScoreboardPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "HOME", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "0 - 0", color = Color(0xFFFFD100), fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(text = "AWAY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "Competition • FT", color = Color.White.copy(alpha = 0.7f), fontSize = 6.sp)
    }
}

@Composable
private fun OnThisDayPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "📅 TODAY", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "On This Day Headline", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        Text(text = "X years ago", color = Color.White.copy(alpha = 0.6f), fontSize = 6.sp)
    }
}

@Composable
private fun LineupPreview() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "LINEUP", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp)
        Text(text = "TEAM NAME", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = "4-3-3", color = Color(0xFFFFD100), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(text = "Manager", color = Color.White.copy(alpha = 0.7f), fontSize = 6.sp)
    }
}

@Composable
private fun ComparisonPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "HOME", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(text = "VS", color = Color(0xFFFFD100), fontSize = 6.sp)
            Text(text = "AWAY", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        repeat(3) { index ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "50%", color = Color.White, fontSize = 7.sp)
                Text(text = "STAT", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp)
                Text(text = "50%", color = Color.White, fontSize = 7.sp)
            }
        }
    }
}

@Composable
private fun SocialPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = "Name", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                Text(text = "@handle", color = Color.White.copy(alpha = 0.5f), fontSize = 5.sp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Social post content preview...",
            color = Color.White,
            fontSize = 7.sp,
            maxLines = 3,
            lineHeight = 8.sp
        )
    }
}

@Composable
private fun RivalryPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "RIVALRY", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Player 1 VS Player 2", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(text = "H2H: 3-2-1", color = Color.White.copy(alpha = 0.7f), fontSize = 6.sp)
    }
}

@Composable
private fun TableStandingsPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "LEAGUE TABLE", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "1. Arsenal 69pts", color = Color.White, fontSize = 7.sp)
        Text(text = "2. Liverpool 67pts", color = Color.White, fontSize = 7.sp)
        Text(text = "3. Man City 59pts", color = Color.White, fontSize = 7.sp)
    }
}

@Composable
private fun InjuryReportPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "INJURY REPORT", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Player A - Out 3 weeks", color = Color.White, fontSize = 7.sp)
        Text(text = "Player B - Doubtful", color = Color.White, fontSize = 7.sp)
    }
}

@Composable
private fun ContractExpiryPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "CONTRACT EXPIRY", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Player A - June 2025", color = Color.White, fontSize = 7.sp)
        Text(text = "Player B - Negotiating", color = Color.White, fontSize = 7.sp)
    }
}

@Composable
private fun AwardNomineePreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "AWARD NOMINEE", color = Color.White.copy(alpha = 0.5f), fontSize = 6.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Favorite: Player X", color = Color.Yellow, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(text = "Nominee 1, 2, 3...", color = Color.White, fontSize = 6.sp)
    }
}

private val CardTemplate.previewGradient: Brush
    get() = when (this) {
        CardTemplate.HeadlineQuote -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1)))
        CardTemplate.PlayerSpotlight -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF311B92)))
        CardTemplate.TopStats -> Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF00695C)))
        CardTemplate.TransferNews -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C)))
        CardTemplate.BreakingNews -> Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFFD32F2F)))
        CardTemplate.MatchPreview -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1)))
        CardTemplate.DetailedScoreboard -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1)))
        CardTemplate.OnThisDay -> Brush.verticalGradient(listOf(Color(0xFF3E2723), Color(0xFF5D4037)))
        CardTemplate.StartingXI -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1)))
        CardTemplate.MatchStatsComparison -> Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF00695C)))
        CardTemplate.SocialPost -> Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF424242)))
        CardTemplate.Rivalry -> Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800)))
        CardTemplate.TableStandings -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF303F9F)))
        CardTemplate.InjuryReport -> Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935)))
        CardTemplate.ContractExpiry -> Brush.verticalGradient(listOf(Color(0xFF4E342E), Color(0xFF6D4C41)))
        CardTemplate.AwardNominee -> Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFC107)))
    }
