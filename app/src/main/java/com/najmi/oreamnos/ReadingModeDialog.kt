package com.najmi.oreamnos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.utils.HapticHelper
import kotlin.math.roundToInt

/**
 * Full-screen Reading Mode Dialog for focused content viewing
 * with swipe-to-dismiss gesture
 */
@Composable
fun ReadingModeDialog(
    outputText: String,
    textSize: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hapticHelper = remember { HapticHelper(context) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 200f
    
    // Animate offset back to 0 if released before threshold
    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swipe_offset"
    )
    
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Only allow downward drag
                        if (offsetY + dragAmount >= 0) {
                            offsetY += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (offsetY > dismissThreshold) {
                            hapticHelper.onCopy()
                            onDismiss()
                        } else {
                            // Spring back to original position
                            offsetY = 0f
                        }
                    }
                )
            }
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .graphicsLayer {
                    translationY = animatedOffset
                    alpha = 1f - (animatedOffset / (dismissThreshold * 2)).coerceIn(0f, 0.5f)
                },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reading Mode", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp, max = 600.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        com.najmi.oreamnos.ui.components.TypewriterText(
                            text = com.najmi.oreamnos.utils.MarkdownUtils.parseMarkdownToAnnotatedString(outputText),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = textSize.sp,
                                lineHeight = (textSize * 1.5f).sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Generated Content", outputText))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("COPY")
                    }
                    TextButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, outputText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share content"))
                        }
                    ) {
                        Text("SHARE")
                    }
                }
            }
        )
    }
}
