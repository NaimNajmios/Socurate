package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.R

@Composable
private fun AnimatedEmptyIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyStateAnimations")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(300),
        label = "scaleAnimation"
    )

    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(300),
        label = "offsetAnimation"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier
            .size(80.dp)
            .scale(animatedScale)
            .graphicsLayer {
                translationY = animatedOffset
            },
        tint = tint
    )
}

@Composable
private fun EmptyStateContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        AnimatedEmptyIcon(
            icon = icon
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(32.dp))
            NeoOutlinedButton(
                onClick = onAction,
                text = actionLabel
            )
        }
    }
}

/**
 * Delightful Empty State Card
 * Features a "breathing" and "floating" icon animation to invite interaction.
 */
@Composable
fun EmptyStateCard(
    onPaste: () -> Unit,
    isVisible: Boolean = true
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_state),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(1f)
                    .graphicsLayer {
                        translationY = 0f
                    },
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "READY TO GENERATE",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Paste content or enter a URL above",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            NeoOutlinedButton(
                onClick = onPaste,
                text = "PASTE FROM CLIPBOARD"
            )
        }
    }
}

/**
 * Empty state for search results with no matches
 */
@Composable
fun EmptySearchCard(
    searchQuery: String,
    onClear: () -> Unit
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        EmptyStateContent(
            icon = Icons.Default.SearchOff,
            title = "NO RESULTS FOUND",
            subtitle = "No matches for \"$searchQuery\"",
            actionLabel = "CLEAR SEARCH",
            onAction = onClear
        )
    }
}

/**
 * Empty state for history with no previous generations
 */
@Composable
fun EmptyHistoryCard(
    onClear: (() -> Unit)? = null
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        EmptyStateContent(
            icon = Icons.Default.History,
            title = "NO HISTORY YET",
            subtitle = "Your generated content will appear here",
            actionLabel = if (onClear != null) "START GENERATING" else null,
            onAction = if (onClear != null) {{}} else null
        )
    }
}

/**
 * Empty state for favorites/saved cards
 */
@Composable
fun EmptyFavoritesCard(
    onExplore: () -> Unit
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        EmptyStateContent(
            icon = Icons.Default.BookmarkBorder,
            title = "NO SAVED CARDS",
            subtitle = "Save cards to access them quickly later",
            actionLabel = "EXPLORE TEMPLATES",
            onAction = onExplore
        )
    }
}

/**
 * Empty state for sessions with no active items
 */
@Composable
fun EmptySessionsCard() {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        EmptyStateContent(
            icon = Icons.Default.History,
            title = "NO ACTIVE SESSIONS",
            subtitle = "Start a new generation to create a session"
        )
    }
}
