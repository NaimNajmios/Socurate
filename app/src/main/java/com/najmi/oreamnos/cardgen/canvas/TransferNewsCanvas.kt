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
fun TransferNewsCanvas(
    data: CardData.TransferNews,
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
                text = "TRANSFER NEWS",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Middle Content
            Text(
                text = data.action.uppercase(),
                color = Color(0xFFFFD100), // Gold
                fontSize = 24.sp.scaleSp(scale),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            AutoSizeText(
                text = data.playerName.uppercase(),
                color = CardTextPrimary,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize.scaleSp(scale),
                    lineHeight = 36.sp.scaleSp(scale)
                ),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
            
            // Bottom Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Context Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${data.fromTeam} ➔ ${data.toTeam}".uppercase(),
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale)
                        )
                    )
                    val details = listOf(
                        if (data.transferType.isNotBlank() && data.transferType != "—") data.transferType else null,
                        if (data.contractLength.isNotBlank() && data.contractLength != "—") "KONTRAK: ${data.contractLength}" else null,
                        if (data.fee.isNotBlank() && data.fee != "—") "YURAN: ${data.fee}" else null
                    ).filterNotNull()

                    if (details.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = details.joinToString(" • ").uppercase(),
                            color = CardTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    if (data.quote.isNotBlank() && data.quote != "—") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${data.quote}\"",
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize.scaleSp(scale),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
