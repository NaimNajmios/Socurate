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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.najmi.oreamnos.cardgen.model.TableRow
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun TableStandingsCanvas(
    data: CardData.TableStandings,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    val scale = config.fontSizeMultiplier

    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.leagueName.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = data.matchday.uppercase(),
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TableHeader(scale = scale)

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            val displayRows = data.standings.take(5)

            displayRows.forEachIndexed { index, row ->
                TableRowItem(
                    row = row,
                    isHighlighted = row.teamName.equals(data.highlightedTeam, ignoreCase = true),
                    isTop3 = row.position <= 3,
                    scale = scale
                )
                if (index < displayRows.lastIndex) {
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "#${data.leagueName.lowercase().filter { it.isLetter() }.take(3)} #socu",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun TableHeader(scale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "#",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "TEAM",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "P",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "W",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "D",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "L",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "PTS",
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TableRowItem(
    row: TableRow,
    isHighlighted: Boolean,
    isTop3: Boolean,
    scale: Float
) {
    val backgroundColor = when {
        isHighlighted -> Color(0xFFFFD100).copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        isTop3 -> Color(0xFFFFD100)
        isHighlighted -> Color(0xFFFFD100)
        else -> CardTextPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = row.position.toString(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp.scaleSp(scale),
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        AutoSizeText(
            text = row.teamName.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize.scaleSp(scale)
            ),
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )

        Text(
            text = row.played.toString(),
            color = CardTextSecondary,
            fontSize = 12.sp.scaleSp(scale),
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = row.won.toString(),
            color = if (row.won >= 15) Color(0xFF4CAF50) else CardTextSecondary,
            fontSize = 12.sp.scaleSp(scale),
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = row.drawn.toString(),
            color = CardTextSecondary,
            fontSize = 12.sp.scaleSp(scale),
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = row.lost.toString(),
            color = if (row.lost >= 10) Color(0xFFF44336) else CardTextSecondary,
            fontSize = 12.sp.scaleSp(scale),
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = row.points.toString(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp.scaleSp(scale),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
    }
}
