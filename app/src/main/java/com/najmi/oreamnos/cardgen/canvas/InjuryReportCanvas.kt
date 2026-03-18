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
import androidx.compose.foundation.layout.size
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
import com.najmi.oreamnos.cardgen.model.InjuryItem

@Composable
fun InjuryReportCanvas(
    data: CardData.InjuryReport,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INJURY REPORT",
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp
                )
            }

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
                text = data.reportDate,
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.injuries.isNotEmpty()) {
                InjurySection(
                    title = "OUT",
                    titleColor = Color(0xFFFF6B6B),
                    items = data.injuries,
                    scale = scale
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (data.doubtfits.isNotEmpty()) {
                InjurySection(
                    title = "DOUBTFUL",
                    titleColor = Color(0xFFFFD100),
                    items = data.doubtfits,
                    scale = scale
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (data.returns.isNotEmpty()) {
                InjurySection(
                    title = "RETURNING",
                    titleColor = Color(0xFF4CAF50),
                    items = data.returns,
                    scale = scale
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "#injury #squadnews #${data.teamName.lowercase().filter { it.isLetter() }.take(3)}",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun InjurySection(
    title: String,
    titleColor: Color,
    items: List<InjuryItem>,
    scale: Float
) {
    Column {
        Text(
            text = "❌ $title",
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
                    Text(
                        text = "${item.playerName} - ${item.position}",
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize.scaleSp(scale)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.injury} • ${item.status}",
                        color = CardTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
