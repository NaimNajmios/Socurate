package com.najmi.oreamnos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Warning card shown when vision extraction falls back to OCR.
 */
@Composable
fun FallbackNotificationCard(
    modifier: Modifier = Modifier
) {
    NeoCard(
        modifier = modifier,
        borderColor = Color(0xFFFFA000), // Amber
        backgroundColor = Color(0xFFFFF8E1) // Light Amber
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFFFFA000)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Vision extraction fell back to OCR. You may want to review the text below.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037)
            )
        }
    }
}
