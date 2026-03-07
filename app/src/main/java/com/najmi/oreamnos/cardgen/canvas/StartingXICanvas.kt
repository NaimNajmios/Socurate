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
import androidx.compose.foundation.layout.width
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
fun StartingXICanvas(
    data: CardData.StartingXI,
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
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = data.teamName.uppercase(),
                    color = Color.Yellow,
                    fontSize = 28.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "STARTING XI • ${data.formation}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column (Starters)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        data.starters.forEach { player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Number box
                                if (player.number.isNotBlank()) {
                                    Text(
                                        text = player.number,
                                        color = Color.White,
                                        fontSize = 14.sp * config.fontSizeMultiplier,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.width(30.dp),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                Text(
                                    text = player.name,
                                    color = Color.White,
                                    fontSize = 18.sp * config.fontSizeMultiplier,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Right Column (Subs & Manager)
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .padding(start = 24.dp)
                    ) {
                        Text(
                            text = "SUBSTITUTES",
                            color = Color.Yellow,
                            fontSize = 14.sp * config.fontSizeMultiplier,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        data.subs.forEach { sub ->
                            Text(
                                text = if (sub.number.isNotBlank()) "${sub.number}. ${sub.name}" else sub.name,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp * config.fontSizeMultiplier,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "MANAGER",
                            color = Color.Yellow,
                            fontSize = 14.sp * config.fontSizeMultiplier,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = data.manager,
                            color = Color.White,
                            fontSize = 16.sp * config.fontSizeMultiplier,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
