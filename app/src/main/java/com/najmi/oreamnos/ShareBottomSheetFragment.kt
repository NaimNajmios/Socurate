package com.najmi.oreamnos

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.najmi.oreamnos.services.ContentGenerationService
import com.najmi.oreamnos.services.GeminiService
import com.najmi.oreamnos.services.WebContentExtractor

import com.najmi.oreamnos.ui.theme.ErrorRed
import com.najmi.oreamnos.ui.theme.SocurateTheme
import com.najmi.oreamnos.utils.HapticHelper
import com.najmi.oreamnos.utils.PreferencesManager
import com.najmi.oreamnos.utils.ReadabilityUtils
import com.najmi.oreamnos.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// File-level regex patterns
private val WHITESPACE_PATTERN = Regex("\\s+")
private val SOURCE_CITATION_PATTERN = Regex("(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$")
private val TRAILING_NEWLINES_PATTERN = Regex("\\n+$")

// Helper function to extract title and body from content
private fun parseContentTitleAndBody(content: String): Pair<String, String> {
    if (content.isEmpty()) return "" to ""
    val parts = content.split("\n\n", limit = 2)
    if (parts.size >= 2 && parts[0].length < 150) {
        return parts[0].trim() to parts[1].trim()
    }
    val singleParts = content.split("\n", limit = 2)
    if (singleParts.size >= 2 && singleParts[0].length < 150) {
        return singleParts[0].trim() to singleParts[1].trim()
    }
    return "" to content.trim()
}

/**
 * Bottom Sheet Dialog Fragment for handling shared content.
 * Converted to Jetpack Compose.
 */
class ShareBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_SHARED_TEXT = "shared_text"

        fun newInstance(sharedText: String): ShareBottomSheetFragment {
            return ShareBottomSheetFragment().apply {
                arguments = Bundle().apply { putString(ARG_SHARED_TEXT, sharedText) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Oreamnos_BottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val prefsManager = PreferencesManager(requireContext())
        val themeMode = prefsManager.getTheme()
        
        return ComposeView(requireContext()).apply {
            setContent {
                SocurateTheme(themeMode = themeMode) {
                    ShareBottomSheetContent(
                        sharedText = arguments?.getString(ARG_SHARED_TEXT) ?: "",
                        onDismiss = { dismiss() },
                        onContinue = { content, title, body, source ->
                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                putExtra("shared_text", arguments?.getString(ARG_SHARED_TEXT))
                                putExtra("generated_content", content)
                                putExtra("generated_title", title)
                                putExtra("generated_body", body)
                                putExtra("generated_source", source)
                            }
                            startActivity(intent)
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        (activity as? ShareReceiverActivity)?.onBottomSheetDismissed()
    }
}

@Composable
fun ShareBottomSheetContent(
    sharedText: String,
    onDismiss: () -> Unit,
    onContinue: (content: String, title: String, body: String, source: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }
    val hapticHelper = remember { HapticHelper(context) }

    // State
    var isInputExpanded by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingText by remember { mutableStateOf("Generating post...") }
    var error by remember { mutableStateOf<String?>(null) }
    var generatedTitle by remember { mutableStateOf("") }
    var generatedBody by remember { mutableStateOf("") }
    var generatedSource by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var isFormalTone by remember { mutableStateOf(prefsManager.isFormalTone()) }
    var includeTitle by remember { mutableStateOf(true) }
    var includeHashtags by remember { mutableStateOf(prefsManager.areHashtagsEnabled()) }
    var includeSource by remember { mutableStateOf(prefsManager.isSourceEnabled()) }

    val hasHashtags = prefsManager.getHashtags()?.isNotEmpty() == true
    val hasResult = generatedBody.isNotEmpty()

    // Rebuild output text when toggles change
    fun rebuildOutput() {
        val builder = StringBuilder()
        if (includeTitle && generatedTitle.isNotEmpty()) {
            builder.append(StringUtils.stripLeadingEmojis(generatedTitle)).append("\n\n")
        }
        builder.append(StringUtils.stripLeadingEmojis(generatedBody))
        if (includeSource && generatedSource.isNotEmpty()) {
            builder.append("\n\n").append(generatedSource)
        }
        if (includeHashtags && prefsManager.areHashtagsEnabled()) {
            val hashtags = prefsManager.getFormattedHashtags()
            if (hashtags.isNotEmpty()) {
                builder.append("\n\n").append(hashtags)
            }
        }
        outputText = builder.toString().trim()
    }

    // Start generation
    fun startGeneration() {
        if (isLoading) return
        isLoading = true
        error = null
        hapticHelper.onGenerationStart()

        scope.launch {
            try {
                var textToProcess = sharedText
                
                if (WebContentExtractor.isUrl(sharedText)) {
                    loadingText = "Extracting content..."
                    textToProcess = withContext(Dispatchers.IO) {
                        WebContentExtractor().extractContent(sharedText)
                    }
                }
                
                loadingText = "Generating post..."
                val tone = if (isFormalTone) PreferencesManager.TONE_FORMAL else PreferencesManager.TONE_CASUAL
                
                val result: String? = withContext(Dispatchers.IO) {
                    val curator = com.najmi.oreamnos.curator.CuratorFactory.create(context)
                    curator.curatePost(textToProcess, prefsManager.isSourceEnabled(), false)
                }
                
                // Extract source citation
                val resultText = result ?: ""
                val matcher = SOURCE_CITATION_PATTERN.find(resultText)
                if (matcher != null) {
                    generatedSource = matcher.value.trim()
                    val contentWithoutSource = SOURCE_CITATION_PATTERN.replace(resultText, "").trim()
                    val parsed = parseContentTitleAndBody(TRAILING_NEWLINES_PATTERN.replace(contentWithoutSource, "").trim())
                    generatedTitle = parsed.first
                    generatedBody = parsed.second
                } else {
                    generatedSource = ""
                    val parsed = parseContentTitleAndBody(resultText)
                    generatedTitle = parsed.first
                    generatedBody = parsed.second
                }
                
                rebuildOutput()
                hapticHelper.onGenerationComplete()
                isInputExpanded = false
                
            } catch (e: Exception) {
                error = e.message
                hapticHelper.onError()
            } finally {
                isLoading = false
            }
        }
    }


    // Auto-start if API key is set
    LaunchedEffect(Unit) {
        if (prefsManager.hasApiKey() && sharedText.isNotEmpty()) {
            startGeneration()
        } else if (!prefsManager.hasApiKey()) {
            Toast.makeText(context, R.string.api_key_required, Toast.LENGTH_LONG).show()
        }
    }

    // Update output when toggles change
    LaunchedEffect(includeTitle, includeHashtags, includeSource) {
        if (hasResult) rebuildOutput()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Share Content", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Input Card (Collapsible)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { isInputExpanded = !isInputExpanded },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Input", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${sharedText.length} chars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "Expand",
                                modifier = Modifier.rotate(if (isInputExpanded) 180f else 0f)
                            )
                        }
                    }
                    AnimatedVisibility(visible = isInputExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                        Text(
                            text = sharedText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Tone Toggle (only show before result)
            if (!hasResult && !isLoading) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isFormalTone,
                        onClick = { isFormalTone = false },
                        label = { Text("Casual") }
                    )
                    FilterChip(
                        selected = isFormalTone,
                        onClick = { isFormalTone = true },
                        label = { Text("Formal") }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Loading State
            if (isLoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(loadingText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            // Error State
            error?.let { errorMsg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Generation Failed", style = MaterialTheme.typography.titleMedium, color = ErrorRed)
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { startGeneration() }) { Text("Try Again") }
                    }
                }
            }
            
            // Result
            if (hasResult && error == null && !isLoading) {
                // Output toggles
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = includeTitle, onClick = { includeTitle = !includeTitle }, label = { Text("Title") })
                    if (hasHashtags) {
                        FilterChip(selected = includeHashtags, onClick = { includeHashtags = !includeHashtags }, label = { Text("Hashtags") })
                    }
                    if (prefsManager.isSourceEnabled()) {
                        FilterChip(selected = includeSource, onClick = { includeSource = !includeSource }, label = { Text("Source") })
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Output text
                OutlinedTextField(
                    value = outputText,
                    onValueChange = { if (isEditMode) outputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditMode,
                    minLines = 5,
                    maxLines = 12
                )
                
                // Word count and readability
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val wordCount = if (outputText.isEmpty()) 0 else WHITESPACE_PATTERN.split(outputText).size
                    val gradeLevel = ReadabilityUtils.calculateFleschKincaidGradeLevel(outputText)
                    Text("$wordCount words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Grade: ${String.format("%.1f", gradeLevel)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Action buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { isEditMode = !isEditMode }, modifier = Modifier.weight(1f)) {
                        Text(if (isEditMode) "Save" else "Edit")
                    }
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Socurate Post", outputText))
                            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Copy") }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, outputText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share")
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Continue button
                Button(
                    onClick = { onContinue(outputText, generatedTitle, generatedBody, generatedSource) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue in App") }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
