package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun MatchPreviewCanvas(
    data: CardData.MatchPreview,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Competition Name
                Text(
                    text = data.competition.uppercase(),
                    color = Color.Yellow,
                    fontSize = 18.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // The VS Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.homeTeam.uppercase(),
                        color = Color.White,
                        fontSize = 32.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "VS",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 24.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Text(
                        text = data.awayTeam.uppercase(),
                        color = Color.White,
                        fontSize = 32.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Match Details Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = data.matchTime,
                        color = Color.White,
                        fontSize = 20.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.stadium.uppercase(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp * config.fontSizeMultiplier,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
