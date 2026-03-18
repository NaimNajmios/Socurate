package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun RivalryCanvas(
    data: CardData.Rivalry,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    val scale = config.fontSizeMultiplier

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
                text = "RIVALRY",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.matchContext.uppercase(),
                color = CardTextSecondary,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerColumn(
                    playerName = data.player1Name,
                    stats = data.player1Stats,
                    isWinner = data.verdict.contains(data.player1Name, ignoreCase = true),
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "VS",
                    color = Color(0xFFFFD100),
                    fontSize = 28.sp.scaleSp(scale),
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                PlayerColumn(
                    playerName = data.player2Name,
                    stats = data.player2Stats,
                    isWinner = data.verdict.contains(data.player2Name, ignoreCase = true),
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "H2H: ${data.headToHead}",
                    color = CardTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = data.verdict,
                    color = Color(0xFFFFD100),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlayerColumn(
    playerName: String,
    stats: List<com.najmi.oreamnos.cardgen.model.StatItem>,
    isWinner: Boolean,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isWinner) Color(0xFFFFD100).copy(alpha = 0.2f)
                    else Color.Transparent,
                    shape = MaterialTheme.shapes.small
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                text = playerName.uppercase(),
                color = if (isWinner) Color(0xFFFFD100) else CardTextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale)
                ),
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        stats.take(3).forEach { stat ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.value,
                    color = CardTextPrimary,
                    fontSize = 18.sp.scaleSp(scale),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stat.label,
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
