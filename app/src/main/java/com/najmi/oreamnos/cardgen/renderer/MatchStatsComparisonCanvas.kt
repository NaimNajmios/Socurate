package com.najmi.oreamnos.cardgen.renderer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData

@Composable
fun MatchStatsComparisonCanvas(
    data: CardData.MatchStatsComparison,
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
            Text(
                text = "MATCH COMPARISON",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.homeTeam.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "VS",
                    color = Color(0xFFFFD100),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = data.awayTeam.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.stats.forEach { stat ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.homeValue,
                                color = CardTextPrimary,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stat.label.uppercase(),
                                color = CardTextMuted,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stat.awayValue,
                                color = CardTextPrimary,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            val hVal = stat.homeValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
                            val aVal = stat.awayValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
                            val total = hVal + aVal
                            val hWeight = if (total > 0) hVal / total else 0.5f
                            
                            Box(modifier = Modifier.weight(hWeight).fillMaxHeight().background(config.colorPair.first))
                            Box(modifier = Modifier.weight(1f - hWeight).fillMaxHeight().background(config.colorPair.second))
                        }
                    }
                }
            }
        }
    }
}
