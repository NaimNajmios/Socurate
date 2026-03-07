package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun OnThisDayCanvas(
    data: CardData.OnThisDay,
    config: CardConfig
) {
    CardBackground(config = config) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f)) // More subtle for historical feel
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Eyebrow Label
                Text(
                    text = data.dateLabel.uppercase(),
                    color = Color.Yellow,
                    fontSize = 18.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Main Headline
                Text(
                    text = data.headline,
                    color = Color.White,
                    fontSize = 28.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp * config.fontSizeMultiplier,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Key Stats
                data.keyStats.forEach { stat ->
                    if (stat.label.isNotBlank() && stat.value.isNotBlank() && stat.value != "0") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stat.value,
                                color = Color.White,
                                fontSize = 24.sp * config.fontSizeMultiplier,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = stat.label.uppercase(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp * config.fontSizeMultiplier,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
