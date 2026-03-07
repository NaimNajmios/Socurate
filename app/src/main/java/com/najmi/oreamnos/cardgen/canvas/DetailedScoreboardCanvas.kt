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
fun DetailedScoreboardCanvas(
    data: CardData.DetailedScoreboard,
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
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Match Status
                Text(
                    text = data.matchStatus.uppercase(),
                    color = Color.Yellow,
                    fontSize = 16.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Score Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.homeTeam.uppercase(),
                        color = Color.White,
                        fontSize = 28.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${data.homeScore} - ${data.awayScore}",
                        color = Color.White,
                        fontSize = 48.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Text(
                        text = data.awayTeam.uppercase(),
                        color = Color.White,
                        fontSize = 28.sp * config.fontSizeMultiplier,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Goalscorers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = data.homeScorers,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp * config.fontSizeMultiplier,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 24.dp))
                    
                    Text(
                        text = data.awayScorers,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp * config.fontSizeMultiplier,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
