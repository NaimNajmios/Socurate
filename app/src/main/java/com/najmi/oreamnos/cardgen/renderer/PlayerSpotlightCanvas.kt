package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.StatItem
import com.najmi.oreamnos.cardgen.ui.DraggableCanvasElement
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun PlayerSpotlightCanvas(
    data: CardData.PlayerSpotlight,
    config: CardConfig,
    modifier: Modifier = Modifier,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit = { _, _ -> }
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f)
    ) {
        DraggableCanvasElement(
            elementId = "player_spotlight",
            cardConfig = config,
            onOffsetChange = onOffsetChange,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "PLAYER SPOTLIGHT",
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (data.keyAction.isNotBlank() && data.keyAction != "—") {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD100), MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = data.keyAction.uppercase(),
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                AutoSizeText(
                    text = data.playerName.uppercase(),
                    color = CardTextPrimary,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 40.sp
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.club.uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = data.position.uppercase(),
                            color = CardTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                        if (data.keyQuote.isNotBlank() && data.keyQuote != "—") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\u201C${data.keyQuote}\u201D",
                                color = CardTextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val stats = listOf(
                            StatItem(label = "Min", value = "${data.minutesPlayed}", context = ""),
                            StatItem(label = "Gl", value = "${data.goals}", context = ""),
                            StatItem(label = "Ast", value = "${data.assists}", context = ""),
                            StatItem(label = "Rat", value = String.format("%.1f", data.rating), context = "")
                        )
                        stats.forEach { stat ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stat.value,
                                    color = Color(0xFFFFD100),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = stat.label.uppercase(),
                                    color = CardTextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
