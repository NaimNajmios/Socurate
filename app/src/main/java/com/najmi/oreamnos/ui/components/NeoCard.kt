package com.najmi.oreamnos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Define default shape as constant
private val DefaultNeoShape = RoundedCornerShape(0.dp)

/**
 * Neo-Editorial Card
 * High contrast, thick border, sharp corners.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    shape: Shape = DefaultNeoShape, // Sharp corners
    borderWidth: Dp = 2.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp // Flat
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
