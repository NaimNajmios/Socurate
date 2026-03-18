package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.NomineeItem

@Composable
fun AwardNomineeCanvas(
    data: CardData.AwardNominee,
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
                    text = "🏆",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.awardName.uppercase(),
                    color = Color(0xFFFFD100),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize.scaleSp(scale)
                    )
                )
            }

            if (data.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.category,
                    color = CardTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.currentFavorite.isNotBlank()) {
                FavoriteSection(
                    favorite = data.currentFavorite,
                    scale = scale
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            NomineesSection(
                title = "NOMINEES",
                nominees = data.nominees,
                favoriteName = data.currentFavorite,
                scale = scale
            )

            Spacer(modifier = Modifier.weight(1f))

            if (data.ceremonyDate.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗳️",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Winner: ${data.ceremonyDate}",
                        color = CardTextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "#${data.awardName.lowercase().filter { it.isLetter() }.take(6)} #socu",
                color = CardTextMuted,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun FavoriteSection(
    favorite: String,
    scale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFFD100).copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⭐",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FAVORITE",
                color = Color(0xFFFFD100),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = favorite.uppercase(),
            color = CardTextPrimary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = MaterialTheme.typography.titleLarge.fontSize.scaleSp(scale)
            )
        )
    }
}

@Composable
private fun NomineesSection(
    title: String,
    nominees: List<NomineeItem>,
    favoriteName: String,
    scale: Float
) {
    Column {
        Text(
            text = title,
            color = CardTextMuted,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        nominees.take(5).forEachIndexed { index, nominee ->
            val isFavorite = nominee.playerName.equals(favoriteName, ignoreCase = true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFavorite) Color(0xFFFFD100) else CardBorder
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = if (isFavorite) Color.Black else CardTextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nominee.playerName.uppercase(),
                        color = if (isFavorite) Color(0xFFFFD100) else CardTextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize.scaleSp(scale)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row {
                        if (nominee.club.isNotBlank()) {
                            Text(
                                text = nominee.club,
                                color = CardTextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (nominee.achievement.isNotBlank() && nominee.club.isNotBlank()) {
                            Text(
                                text = " • ",
                                color = CardTextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (nominee.achievement.isNotBlank()) {
                            Text(
                                text = nominee.achievement,
                                color = CardTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (nominee.odds.isNotBlank()) {
                    Text(
                        text = nominee.odds,
                        color = if (isFavorite) Color(0xFFFFD100) else CardTextMuted,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
