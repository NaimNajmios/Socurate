package com.najmi.oreamnos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Neo-Editorial Snackbar
 * High contrast, sharp corners, bold typography.
 * Replaces system Toasts for a cohesive design language.
 */
@Composable
fun NeoSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    // NeoCard adds its own internal padding (16.dp).
    // We add external padding to float it above the bottom bar/edge.
    NeoCard(
        modifier = modifier.padding(16.dp),
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        borderColor = MaterialTheme.colorScheme.primary,
        borderWidth = 2.dp
    ) {
        Text(
            text = snackbarData.visuals.message.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}
