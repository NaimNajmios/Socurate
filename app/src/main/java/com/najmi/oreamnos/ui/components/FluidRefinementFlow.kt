package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.GenerationPill
import kotlinx.coroutines.delay

/**
 * Fluid Refinement Flow
 * A staggering flow layout for refinement chips with fluid entrance animations
 * and delightful interaction states.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FluidRefinementFlow(
    options: List<Pair<String, String>>,
    selectedOptions: List<String>,
    customPills: List<GenerationPill>,
    selectedPillIds: List<String>,
    onToggleOption: (String) -> Unit,
    onTogglePill: (String) -> Unit,
    onCreatePill: () -> Unit,
    onEditPill: (GenerationPill) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State to trigger the staggered entrance
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Combine all items to index them for staggering
    val totalItems = options.size + customPills.size + 1 // +1 for Add button

    // OPTIMIZATION: Convert lists to Sets for O(1) lookup performance
    val selectedOptionsSet = remember(selectedOptions) { selectedOptions.toSet() }
    val selectedPillIdsSet = remember(selectedPillIds) { selectedPillIds.toSet() }

    NeoCard(modifier = modifier.fillMaxWidth()) {
        Text("REFINE OUTPUT", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Built-in Options
            options.forEachIndexed { index, (key, label) ->
                // OPTIMIZATION: Use key to help Compose identify items across recompositions
                key(key) {
                    StaggeredEntranceItem(
                        visible = isVisible,
                        index = index,
                        totalItems = totalItems
                    ) {
                        // OPTIMIZATION: Memoize onClick to prevent unnecessary recomposition of NeoChip
                        val isSelected = selectedOptionsSet.contains(key)
                        val onClick = remember(key, onToggleOption) { { onToggleOption(key) } }

                        NeoChip(
                            selected = isSelected,
                            onClick = onClick,
                            text = label
                        )
                    }
                }
            }

            // Custom Pills
            customPills.forEachIndexed { index, pill ->
                key(pill.id) {
                    StaggeredEntranceItem(
                        visible = isVisible,
                        index = options.size + index,
                        totalItems = totalItems
                    ) {
                        val isSelected = selectedPillIdsSet.contains(pill.id)
                        val onClick = remember(pill.id, onTogglePill) { { onTogglePill(pill.id) } }
                        val onLongClick = remember(pill, onEditPill) { { onEditPill(pill) } }

                        NeoChip(
                            selected = isSelected,
                            onClick = onClick,
                            onLongClick = onLongClick,
                            text = pill.name,
                            unselectedBorderColor = Color(0xFFFF9800), // Orange
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Add Button
            key("add_button") {
                StaggeredEntranceItem(
                    visible = isVisible,
                    index = options.size + customPills.size,
                    totalItems = totalItems
                ) {
                    val onClick = remember(onCreatePill) { onCreatePill }
                    NeoAddButton(onClick = onClick)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Smart Regenerate Button
        // Animates appearance/emphasis when selections are made
        val hasSelection = selectedOptions.isNotEmpty() || selectedPillIds.isNotEmpty()

        AnimatedVisibility(
            visible = true, // Always visible but changes state
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
             NeoButton(
                onClick = onRegenerate,
                modifier = Modifier.fillMaxWidth(),
                text = if (hasSelection) "APPLY REFINEMENTS" else "REGENERATE",
                containerColor = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (hasSelection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * An item that animates in with a delay based on its index.
 */
@Composable
fun StaggeredEntranceItem(
    visible: Boolean,
    index: Int,
    totalItems: Int,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(targetState = visible, label = "entrance")

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, delayMillis = index * 30)
        },
        label = "alpha"
    ) { state ->
        if (state) 1f else 0f
    }

    val scale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = 0.001f
            )
        },
        label = "scale"
    ) { state ->
        if (state) 1f else 0.8f
    }

    val translationY by transition.animateFloat(
        transitionSpec = {
             tween(durationMillis = 300, delayMillis = index * 30)
        },
        label = "translationY"
    ) { state ->
        if (state) 0f else 20f
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translationY
            }
    ) {
        content()
    }
}

/**
 * A specialized Neo-style button for adding new items.
 */
@Composable
fun NeoAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(0.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add custom refinement",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "CUSTOM",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
