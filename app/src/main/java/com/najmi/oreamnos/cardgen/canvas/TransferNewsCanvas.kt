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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData

@Composable
fun TransferNewsCanvas(
    data: CardData.TransferNews,
    config: CardConfig
) {
    CardBackground(config = config) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(24.dp)
            ) {
                // Large action text block (e.g., AGREEMENT REACHED)
                Text(
                    text = data.action.uppercase(),
                    color = Color.Yellow,
                    fontSize = 32.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp * config.fontSizeMultiplier
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Player Name
                Text(
                    text = data.playerName.uppercase(),
                    color = Color.White,
                    fontSize = 24.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Transfer Details
                Text(
                    text = "${data.fromTeam} ➔ ${data.toTeam}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp * config.fontSizeMultiplier,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "FEE / CONTRACT: ${data.fee}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp * config.fontSizeMultiplier,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (data.quote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "\"${data.quote}\"",
                        color = Color.White,
                        fontSize = 16.sp * config.fontSizeMultiplier,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
