package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun SocialPostCanvas(
    data: CardData.SocialPost,
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
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CardTextMuted.copy(alpha = 0.2f))
                        .border(1.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.name.take(1).uppercase(),
                        color = Color(0xFFFFD100),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = data.name,
                        color = CardTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = data.handle,
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = CardTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AutoSizeText(
                text = data.content,
                color = CardTextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 6,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = data.timestamp,
                color = CardTextMuted,
                style = MaterialTheme.typography.labelMedium
            )
            
            HorizontalDivider(
                color = CardBorder,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            Text(
                text = data.metrics,
                color = CardTextSecondary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
