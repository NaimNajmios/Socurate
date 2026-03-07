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
fun BreakingNewsCanvas(
    data: CardData.BreakingNews,
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
                    .background(Color.Red.copy(alpha = 0.85f))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Eyebrow Label
                Text(
                    text = data.label.uppercase(),
                    color = Color.White,
                    fontSize = 16.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main Headline
                Text(
                    text = data.headline.uppercase(),
                    color = Color.Yellow,
                    fontSize = 36.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp * config.fontSizeMultiplier
                )
                
                if (data.subtext.isNotBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = data.subtext,
                        color = Color.White,
                        fontSize = 18.sp * config.fontSizeMultiplier,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp * config.fontSizeMultiplier
                    )
                }
            }
        }
    }
}
