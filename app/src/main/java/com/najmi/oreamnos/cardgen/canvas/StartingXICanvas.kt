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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun StartingXICanvas(
    data: CardData.StartingXI,
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
                text = "STARTING XI",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(0.2f))
            
            // Middle Content - Team & Formation
            AutoSizeText(
                text = "${data.teamName} • ${data.formation}".uppercase(),
                color = Color(0xFFFFD100), // Gold
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale),
                    letterSpacing = 1.sp
                ),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Starters split into 2 columns for space
            Row(modifier = Modifier.fillMaxWidth()) {
                val mid = (data.starters.size + 1) / 2
                val leftCol = data.starters.take(mid)
                val rightCol = data.starters.drop(mid)
                
                Column(modifier = Modifier.weight(1f)) {
                    leftCol.forEach { player ->
                        PlayerRow(player.number, player.name, scale)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    rightCol.forEach { player ->
                        PlayerRow(player.number, player.name, scale)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section: Context (Manager & Subs summary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Context Info
                Column(modifier = Modifier.weight(1f)) {
                    if (data.manager.isNotBlank() && data.manager != "—") {
                        Text(
                            text = "MANAGER: ${data.manager}".uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize.scaleSp(scale)
                            )
                        )
                    }
                    
                    val validSubs = data.subs.filter { it.name.isNotBlank() && it.name != "—" }
                    if (validSubs.isNotEmpty()) {
                        val subsText = validSubs.joinToString(", ") { it.name }
                        Text(
                            text = "SUBS: $subsText",
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CardFooter()
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(number: String, name: String, scale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (number.isNotBlank() && number != "—") {
            Text(
                text = number,
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale)
                ),
                modifier = Modifier.width(20.dp)
            )
        }
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize.scaleSp(scale)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
