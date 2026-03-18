package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData

@Composable
fun TopStatsCanvas(
    data: CardData.TopStats,
    config: CardConfig,
    modifier: Modifier = Modifier
) {
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
                text = data.matchContext.uppercase(),
                color = CardTextSecondary,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                data.stats.forEachIndexed { index, stat ->
                    val rowColor = if (index % 2 == 0) config.colorPair.first else config.colorPair.second
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, rowColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.label.uppercase(),
                                color = rowColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            if (stat.context.isNotEmpty()) {
                                Text(
                                    text = stat.context,
                                    color = CardTextPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Text(
                            text = stat.value,
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}
