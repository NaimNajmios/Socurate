package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData

@Composable
fun OnThisDayCanvas(
    data: CardData.OnThisDay,
    config: CardConfig
) {
    val scale = config.fontSizeMultiplier

    CardBackground(
        config = config,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top branding
            Text(
                text = "ON THIS DAY",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Middle Content
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (data.yearsAgo > 0) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD100), MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${data.yearsAgo} TAHUN LALU",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                val dateText = buildString {
                    append(data.dateLabel.uppercase())
                    if (data.competition.isNotBlank() && data.competition != "—") append(" • ${data.competition.uppercase()}")
                }
                Text(
                    text = dateText,
                    color = Color(0xFFFFD100), // Gold
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize.scaleSp(scale),
                        letterSpacing = 1.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = data.headline,
                color = CardTextPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize.scaleSp(scale),
                    lineHeight = 32.sp.scaleSp(scale)
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Key Stats & Context Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        data.keyStats.take(3).forEach { stat ->
                            if (stat.label.isNotBlank() && stat.value.isNotBlank() && stat.value != "0") {
                                Column {
                                    Text(
                                        text = stat.value,
                                        color = CardTextPrimary,
                                        fontSize = 24.sp.scaleSp(scale),
                                        fontWeight = FontWeight.Black
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
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
                }
            }
        }
    }
}
