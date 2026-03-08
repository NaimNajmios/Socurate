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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.utils.ColorExtractor

@Composable
fun DetailedScoreboardCanvas(
    data: CardData.DetailedScoreboard,
    config: CardConfig
) {
    val scale = config.fontSizeMultiplier
    val gradientColors = ColorExtractor.getMatchColors(data.homeTeam, data.awayTeam)

    CardBackground(
        config = config.copy(colorPair = gradientColors),
        modifier = Modifier.aspectRatio(1f)
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
            
            HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Context Info (Goalscorers)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.matchStatus.uppercase(),
                        color = Color(0xFFFFD100), // Gold
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale)
                        )
                    )
                    if ((data.homeScorers.isNotBlank() && data.homeScorers != "—") || 
                        (data.awayScorers.isNotBlank() && data.awayScorers != "—")) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (data.homeScorers.isNotBlank() && data.homeScorers != "—") {
                            Text(
                                text = "${data.homeTeam}:\n${data.homeScorers}",
                                color = CardTextPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale)
                                )
                            )
                        }
                        if (data.awayScorers.isNotBlank() && data.awayScorers != "—") {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${data.awayTeam}:\n${data.awayScorers}",
                                color = CardTextSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
                }
            }
        }
    }
}
