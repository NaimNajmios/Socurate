package com.najmi.oreamnos.ui.generate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.model.GenerationPill
import com.najmi.oreamnos.ui.components.FluidEditButton
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoCard
import com.najmi.oreamnos.ui.components.NeoChip
import com.najmi.oreamnos.ui.components.NeoCopyButton
import com.najmi.oreamnos.ui.components.NeoInput
import com.najmi.oreamnos.ui.components.NeoOutlinedButton
import com.najmi.oreamnos.ui.components.SwipeableOutputBox
import com.najmi.oreamnos.utils.ReadabilityUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun OutputCard(
    outputText: String,
    isEditMode: Boolean,
    onOutputChange: (String) -> Unit,
    includeTitle: Boolean,
    includeHashtags: Boolean,
    includeSource: Boolean,
    hasHashtags: Boolean,
    isSourceEnabled: Boolean,
    onIncludeTitleChange: (Boolean) -> Unit,
    onIncludeHashtagsChange: (Boolean) -> Unit,
    onIncludeSourceChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onExpandClick: () -> Unit,
    onCreateCard: () -> Unit = {},
    textSize: Int,
    modifier: Modifier = Modifier
) {
    NeoCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DISPLAY OPTIONS", style = MaterialTheme.typography.labelLarge)
            
            IconButton(onClick = onExpandClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Expand to reading mode",
                    modifier = Modifier.rotate(90f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeoChip(selected = includeTitle, onClick = { onIncludeTitleChange(!includeTitle) }, text = "Title")
            if (hasHashtags) {
                NeoChip(selected = includeHashtags, onClick = { onIncludeHashtagsChange(!includeHashtags) }, text = "Hashtags")
            }
            if (isSourceEnabled) {
                NeoChip(selected = includeSource, onClick = { onIncludeSourceChange(!includeSource) }, text = "Source")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            AnimatedContent(
                targetState = isEditMode,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.98f)) togetherWith
                        (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.98f))
                    } else {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.98f)) togetherWith
                        (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.98f))
                    }
                },
                label = "edit_mode_transition"
            ) { editing ->
                if (editing) {
                    NeoInput(
                        value = outputText,
                        onValueChange = onOutputChange,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = false,
                        minLines = 8,
                        maxLines = 20,
                        label = "GENERATED CONTENT",
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = textSize.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                } else {
                    SwipeableOutputBox(
                        outputText = outputText,
                        textSize = textSize,
                        onCopy = onCopyClick,
                        onShare = onShareClick
                    )
                }
            }
        }
        
        val wordCount = remember(outputText) {
            ReadabilityUtils.countWords(outputText)
        }
        val gradeLevel = remember(outputText) {
            ReadabilityUtils.calculateFleschKincaidGradeLevel(outputText)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("WORDS: $wordCount", style = MaterialTheme.typography.labelMedium)
            Text("GRADE: ${String.format("%.1f", gradeLevel)}", style = MaterialTheme.typography.labelMedium)
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluidEditButton(
                isEditing = isEditMode,
                onToggle = onEditClick,
                modifier = Modifier.weight(1f)
            )
            NeoCopyButton(
                onCopy = onCopyClick,
                modifier = Modifier.weight(1f)
            )
            NeoButton(onClick = onShareClick, modifier = Modifier.weight(1f), text = "Share")
        }
        NeoOutlinedButton(
            text = "CREATE CARD",
            onClick = { onCreateCard() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
