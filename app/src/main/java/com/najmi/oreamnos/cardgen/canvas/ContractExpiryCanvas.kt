package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.najmi.oreamnos.cardgen.model.ContractPlayer

@Composable
fun ContractExpiryCanvas(
    data: CardData.ContractExpiry,
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "EXPIRING CONTRACTS",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = data.teamName.uppercase(),
                color = CardTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize.scaleSp(scale)
                )
            )

            Text(
                text = "${data.seasonYear} Season",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.expiringPlayers.isNotEmpty()) {
                ContractSection(
                    title = "EXPIRING SOON",
                    titleColor = Color(0xFFFF6B6B),
                    items = data.expiringPlayers,
                    scale = scale
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (data.renewals.isNotEmpty()) {
                ContractSection(
                    title = "RENEWED",
                    titleColor = Color(0xFF4CAF50),
                    items = data.renewals,
                    scale = scale
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "#contract #${data.teamName.lowercase().filter { it.isLetter() }.take(3)} #socu",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun ContractSection(
    title: String,
    titleColor: Color,
    items: List<ContractPlayer>,
    scale: Float
) {
    Column {
        Text(
            text = "🔴 $title",
            color = titleColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        items.take(4).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "•",
                    color = CardTextSecondary,
                    fontSize = 14.sp.scaleSp(scale),
                    modifier = Modifier.width(16.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.playerName.uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize.scaleSp(scale)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (item.position.isNotBlank()) {
                            Text(
                                text = ", ${item.position}",
                                color = CardTextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row {
                        if (item.expiresIn.isNotBlank()) {
                            Text(
                                text = item.expiresIn,
                                color = CardTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (item.marketValue.isNotBlank()) {
                            Text(
                                text = if (item.expiresIn.isNotBlank()) " • ${item.marketValue}" else item.marketValue,
                                color = Color(0xFFFFD100),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}
