package com.najmi.oreamnos.ui.generate

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.services.WebContentExtractor
import com.najmi.oreamnos.ui.components.ClearAction
import com.najmi.oreamnos.ui.components.LinkPreviewSection
import com.najmi.oreamnos.ui.components.NeoCard
import com.najmi.oreamnos.ui.components.NeoChip
import com.najmi.oreamnos.ui.components.NeoInput
import com.najmi.oreamnos.ui.components.NeoSwitch
import com.najmi.oreamnos.ui.components.PasteAction
import com.najmi.oreamnos.utils.ReadabilityUtils

@Composable
fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    keepStructure: Boolean,
    onKeepStructureChange: (Boolean) -> Unit,
    linkPreviewData: WebContentExtractor.UrlMetadata?,
    isLoadingPreview: Boolean,
    onExtractContent: (String) -> Unit,
    onDismissPreview: () -> Unit,
    isError: Boolean = false,
    onOcrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NeoCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            NeoInput(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = "SOURCE MATERIAL",
                placeholder = "Paste article text or URL...",
                minLines = 5,
                maxLines = 10,
                isError = isError,
                trailingIcon = {
                    AnimatedContent(
                        targetState = inputText.isNotEmpty(),
                        transitionSpec = {
                            (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                        },
                        label = "input_action"
                    ) { hasText ->
                        if (hasText) {
                            ClearAction(onClear = { onInputChange("") })
                        } else {
                            PasteAction(onPaste = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                if (clipboard.hasPrimaryClip()) {
                                    val text = clipboard.primaryClip?.getItemAt(0)?.text
                                    if (text != null) {
                                        onInputChange(text.toString())
                                        Toast.makeText(context, "Content pasted", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                },
                supportingText = {
                    AnimatedVisibility(
                        visible = inputText.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val charCount = inputText.length
                        val wordCount = remember(inputText) { ReadabilityUtils.countWords(inputText) }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "$charCount chars / $wordCount words",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PRESERVE STRUCTURE", style = MaterialTheme.typography.labelLarge)

                NeoSwitch(
                    checked = keepStructure,
                    onCheckedChange = onKeepStructureChange
                )
            }

            Spacer(Modifier.height(16.dp))

            NeoChip(
                text = "FROM SCREENSHOT",
                selected = false,
                onClick = onOcrClick,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = linkPreviewData != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (linkPreviewData != null) {
                    LinkPreviewSection(
                        linkPreviewData = linkPreviewData,
                        onExtract = { onExtractContent(linkPreviewData.originalUrl) },
                        onDismiss = onDismissPreview
                    )
                }
            }

            AnimatedVisibility(
                visible = isLoadingPreview,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text("Loading preview...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
