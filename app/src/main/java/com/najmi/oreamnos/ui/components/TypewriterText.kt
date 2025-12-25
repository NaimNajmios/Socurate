package com.najmi.oreamnos.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

/**
 * Typewriter Text Effect
 * Animates text character by character.
 * Supports both String and AnnotatedString for markdown formatting.
 */
@Composable
fun TypewriterText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onFinished: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf(AnnotatedString("")) }
    
    // Reset when text changes
    LaunchedEffect(text) {
        displayedText = AnnotatedString("")
        var currentIndex = 0
        val plainText = text.text
        while (currentIndex < plainText.length) {
            // Append 3 characters at a time for "near instant" speed
            val endIndex = (currentIndex + 3).coerceAtMost(plainText.length)
            // Preserve styling by using subSequence
            displayedText = text.subSequence(0, endIndex)
            currentIndex = endIndex
            delay(1) // 1ms delay
        }
        onFinished()
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        style = style
    )
}
