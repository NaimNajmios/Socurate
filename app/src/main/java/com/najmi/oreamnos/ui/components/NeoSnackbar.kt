package com.najmi.oreamnos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

/**
 * A Neo-styled Snackbar component.
 * Replaces the standard rounded Material snackbar with a sharp, high-contrast card.
 *
 * Features:
 * - Rectangular shape (Neo-brutalism)
 * - High contrast (Inverse Surface background)
 * - Bold, uppercase text
 * - Border styling via NeoCard logic (simulated with standard Snackbar parameters or wrapper)
 */
@Composable
fun NeoSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    // We use a NeoCard-like appearance but implemented directly here
    // to work within the SnackbarHost constraints easily.

    NeoCard(
        modifier = modifier.padding(16.dp),
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        borderColor = MaterialTheme.colorScheme.inverseOnSurface,
        borderWidth = 2.dp
    ) {
        // Content of the snackbar
        Snackbar(
            snackbarData = snackbarData,
            shape = RectangleShape, // Neo style: no rounded corners
            containerColor = Color.Transparent, // Let NeoCard background show
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.primaryContainer, // High contrast action
            dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}
