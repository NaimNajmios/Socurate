package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun MatchPreviewCanvas(
    data: CardData.MatchPreview,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    val scale = config.fontSizeMultiplier

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
                text = "MATCH PREVIEW",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Middle Content: VS Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    AutoSizeText(
                        text = data.homeTeam.uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize.scaleSp(scale),
                            lineHeight = 36.sp.scaleSp(scale),
                            textAlign = TextAlign.Start
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (data.homeForm.isNotBlank() && data.homeForm != "—") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.homeForm.uppercase(),
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                    }
                }
                
                Text(
                    text = "vs",
                    color = Color(0xFFFFD100), // Gold
                    fontSize = 28.sp.scaleSp(scale),
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    AutoSizeText(
                        text = data.awayTeam.uppercase(),
                        color = CardTextSecondary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize.scaleSp(scale),
                            lineHeight = 36.sp.scaleSp(scale),
                            textAlign = TextAlign.End
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (data.awayForm.isNotBlank() && data.awayForm != "—") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.awayForm.uppercase(),
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section
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
                    val timeStr = data.matchTime.takeIf { it.isNotBlank() && it != "—" }
                    val stadiumStr = data.stadium.takeIf { it.isNotBlank() && it != "—" }
                    val combinedStr = listOfNotNull(timeStr, stadiumStr).joinToString(" • ")
                    
                    if (combinedStr.isNotEmpty()) {
                        Text(
                            text = combinedStr.uppercase(),
                            color = CardTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
