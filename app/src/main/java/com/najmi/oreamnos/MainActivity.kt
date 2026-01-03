package com.najmi.oreamnos

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.najmi.oreamnos.curator.CuratorFactory
import com.najmi.oreamnos.model.GenerationPill
import com.najmi.oreamnos.services.ContentGenerationService
import com.najmi.oreamnos.services.WebContentExtractor
import com.najmi.oreamnos.ui.theme.ErrorRed
import com.najmi.oreamnos.ui.theme.SocurateTheme
import com.najmi.oreamnos.utils.HapticHelper
import com.najmi.oreamnos.utils.PreferencesManager
import com.najmi.oreamnos.utils.ReadabilityUtils
import com.najmi.oreamnos.utils.StringUtils
import com.najmi.oreamnos.utils.MarkdownUtils
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoCard
import com.najmi.oreamnos.ui.components.NeoChip
import com.najmi.oreamnos.ui.components.NeoCopyButton
import com.najmi.oreamnos.ui.components.NeoInput
import com.najmi.oreamnos.ui.components.NeoOutlinedButton
import com.najmi.oreamnos.ui.components.SwipeableOutputBox
import com.najmi.oreamnos.ui.components.AnimatedCheckmark
import com.najmi.oreamnos.ui.components.EnhancedLoadingCard
import com.najmi.oreamnos.ui.components.EmptyStateCard
import com.najmi.oreamnos.ui.components.FluidRefinementFlow
import com.najmi.oreamnos.ui.components.LinkPreviewSection
import com.najmi.oreamnos.ui.components.PasteAction
import com.najmi.oreamnos.ui.components.ClearAction
import com.najmi.oreamnos.viewmodel.MainViewModel
import androidx.compose.animation.animateContentSize

// File-level regex patterns for use in composables
private val SOURCE_CITATION_PATTERN = Regex("(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$")
private val TRAILING_NEWLINES_PATTERN = Regex("\\n+$")
private val WHITESPACE_PATTERN = Regex("\\s+")

/**
 * Main activity for the Socurate app.
 * Converted to Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var prefsManager: PreferencesManager
    private lateinit var viewModel: MainViewModel
    
    // State for broadcast receiver results
    private val generationResult = mutableStateOf<GenerationResult?>(null)
    
    data class GenerationResult(
        val success: Boolean,
        val result: String? = null,
        val error: String? = null,
        val isRefinement: Boolean = false,
        val isRateLimit: Boolean = false,
        val rateLimitProvider: String? = null,
        val retryDelayMs: Long = 0
    )
    
    private val serviceResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(ContentGenerationService.EXTRA_SUCCESS, false)
            val isRefinement = intent.getBooleanExtra(ContentGenerationService.EXTRA_IS_REFINEMENT, false)
            val isRateLimit = intent.getBooleanExtra(ContentGenerationService.EXTRA_IS_RATE_LIMIT, false)
            
            if (isRateLimit) {
                generationResult.value = GenerationResult(
                    success = false,
                    isRateLimit = true,
                    isRefinement = isRefinement,
                    rateLimitProvider = intent.getStringExtra(ContentGenerationService.EXTRA_RATE_LIMIT_PROVIDER),
                    retryDelayMs = intent.getLongExtra(ContentGenerationService.EXTRA_RETRY_DELAY_MS, 0)
                )
            } else if (success) {
                generationResult.value = GenerationResult(
                    success = true,
                    result = intent.getStringExtra(ContentGenerationService.EXTRA_RESULT),
                    isRefinement = isRefinement
                )
            } else {
                generationResult.value = GenerationResult(
                    success = false,
                    error = intent.getStringExtra(ContentGenerationService.EXTRA_ERROR),
                    isRefinement = isRefinement
                )
            }
        }
    }

    // Theme state
    private val currentTheme = mutableStateOf(PreferencesManager.THEME_SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "=== MainActivity onCreate ===")
        
        prefsManager = PreferencesManager(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Initialize theme
        currentTheme.value = prefsManager.getTheme()
        applyTheme(currentTheme.value)
        
        // Get incoming intent data
        val sharedText = intent?.getStringExtra("shared_text")
        val generatedContent = intent?.getStringExtra("generated_content")
        val intentTitle = intent?.getStringExtra("generated_title")
        val intentBody = intent?.getStringExtra("generated_body")
        val intentSource = intent?.getStringExtra("generated_source")
        
        setContent {
            val themeValue by currentTheme

            SocurateTheme(themeMode = themeValue) {
                MainScreen(
                    prefsManager = prefsManager,
                    generationResult = generationResult.value,
                    onClearResult = { generationResult.value = null },
                    onNavigateToSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onNavigateToUsage = { startActivity(Intent(this, UsageActivity::class.java)) },
                    onGenerate = { input, includeSource, keepStructure -> startGeneration(input, includeSource, keepStructure) },
                    onRefine = { originalPost, refinements, includeSource -> startRefinement(originalPost, refinements, includeSource) },
                    initialSharedText = sharedText,
                    initialTitle = intentTitle,
                    initialBody = intentBody,
                    initialSource = intentSource
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            serviceResultReceiver,
            IntentFilter(ContentGenerationService.BROADCAST_RESULT)
        )
        
        // Check for theme changes
        val savedTheme = prefsManager.getTheme()
        if (currentTheme.value != savedTheme) {
            currentTheme.value = savedTheme
            applyTheme(savedTheme)
        }
    }
    
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceResultReceiver)
    }
    
    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            PreferencesManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    private fun startGeneration(input: String, includeSource: Boolean, keepStructure: Boolean) {
        val serviceIntent = Intent(this, ContentGenerationService::class.java).apply {
            action = ContentGenerationService.ACTION_GENERATE
            putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, input)
            putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource)
            putExtra(ContentGenerationService.EXTRA_KEEP_STRUCTURE, keepStructure)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    private fun startRefinement(originalPost: String, refinements: List<String>, includeSource: Boolean) {
        val serviceIntent = Intent(this, ContentGenerationService::class.java).apply {
            action = ContentGenerationService.ACTION_REFINE
            putExtra(ContentGenerationService.EXTRA_ORIGINAL_POST, originalPost)
            putStringArrayListExtra(ContentGenerationService.EXTRA_REFINEMENTS, ArrayList(refinements))
            putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    prefsManager: PreferencesManager,
    generationResult: MainActivity.GenerationResult?,
    onClearResult: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onGenerate: (String, Boolean, Boolean) -> Unit,
    onRefine: (String, List<String>, Boolean) -> Unit,
    initialSharedText: String? = null,
    initialTitle: String? = null,
    initialBody: String? = null,
    initialSource: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var inputText by remember { mutableStateOf(initialSharedText ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var hasResult by remember { mutableStateOf(initialBody?.isNotEmpty() == true) }
    var generatedTitle by remember { mutableStateOf(initialTitle ?: "") }
    var generatedBody by remember { mutableStateOf(initialBody ?: "") }
    var generatedSource by remember { mutableStateOf(initialSource ?: "") }
    var outputText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var keepStructure by remember { mutableStateOf(false) }
    
    // Output toggles
    var includeTitle by remember { mutableStateOf(true) }
    var includeHashtags by remember { mutableStateOf(prefsManager.areHashtagsEnabled()) }
    var includeSource by remember { mutableStateOf(prefsManager.isSourceEnabled()) }
    
    // Refinement state
    val refinementOptions = remember { mutableStateListOf<String>() }
    var showRateLimitDialog by remember { mutableStateOf(false) }
    var rateLimitInfo by remember { mutableStateOf<MainActivity.GenerationResult?>(null) }
    
    // Provider selector state
    var showProviderSelector by remember { mutableStateOf(false) }
    
    // Reading mode dialog
    var showReadingDialog by remember { mutableStateOf(false) }
    
    // Text size - track as state to update when returning from settings
    val textSizeState = remember { mutableIntStateOf(prefsManager.getTextSize()) }
    textSizeState.intValue = prefsManager.getTextSize()
    
    // Link preview state
    var linkPreviewData by remember { mutableStateOf<WebContentExtractor.UrlMetadata?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }
    var previewError by remember { mutableStateOf<String?>(null) }
    
    // Pill management state
    var showCreatePillDialog by remember { mutableStateOf(false) }
    var showEditPillDialog by remember { mutableStateOf(false) }
    var pillToEdit by remember { mutableStateOf<GenerationPill?>(null) }
    
    // Custom pills
    val customPills = remember { mutableStateListOf<GenerationPill>() }
    val selectedPillIds = remember { mutableStateListOf<String>() }
    
    // Visual feedback states
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var inputHasChanged by remember { mutableStateOf(false) }
    var previousInput by remember { mutableStateOf(initialSharedText ?: "") }
    var triggerShakeAnimation by remember { mutableStateOf(false) }

    // Input validation state
    var isInputError by remember { mutableStateOf(false) }
    var triggerInputShake by remember { mutableStateOf(false) }

    
    // Load custom pills
    LaunchedEffect(Unit) {
        customPills.clear()
        customPills.addAll(prefsManager.getPills())
    }
    
    // Detect URL and fetch metadata
    LaunchedEffect(inputText) {
        // Optimized: Remove redundant trim(), isUrl handles CharSequence efficiently
        if (WebContentExtractor.isUrl(inputText)) {
            isLoadingPreview = true
            previewError = null
            try {
                val extractor = WebContentExtractor()
                val metadata = withContext(Dispatchers.IO) {
                    extractor.extractMetadata(inputText.trim())
                }
                linkPreviewData = metadata
                isLoadingPreview = false
            } catch (e: Exception) {
                previewError = e.message
                isLoadingPreview = false
            }
        } else {
            // Clear preview if input is not a URL
            linkPreviewData = null
            isLoadingPreview = false
            previewError = null
        }
    }
    
    // Helper function to rebuild output - remembered to stay stable across recompositions
    // This prevents OutputCard from recomposing when unrelated state (like inputText) changes
    val rebuildOutput = remember(generatedTitle, generatedBody, generatedSource, includeTitle, includeHashtags, includeSource) {
        {
            val builder = StringBuilder()
            if (includeTitle && generatedTitle.isNotEmpty()) {
                builder.append(StringUtils.stripLeadingEmojis(generatedTitle)).append("\n\n")
            }

            // If title is NOT included, we should check if the body starts with the title to avoid duplication
            // or if we need to strip it from the body if it was part of the original result
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
    }
    
    // Process generation result
    LaunchedEffect(generationResult) {
        if (generationResult == null) return@LaunchedEffect
        
        isLoading = false
        
        when {
            generationResult.isRateLimit -> {
                rateLimitInfo = generationResult
                showRateLimitDialog = true
            }
            generationResult.success && generationResult.result != null -> {
                // Trigger success animation before showing result
                showSuccessAnimation = true
                // Extract source citation
                val result = generationResult.result
                val matcher = SOURCE_CITATION_PATTERN.find(result)
                if (matcher != null) {
                    generatedSource = matcher.value.trim()
                    val contentWithoutSource = SOURCE_CITATION_PATTERN.replace(result, "").trim()
                    val cleaned = TRAILING_NEWLINES_PATTERN.replace(contentWithoutSource, "").trim()
                    
                    // Try splitting by double newline first
                    var parts = cleaned.split("\n\n", limit = 2)
                    
                    // If that fails, try splitting by single newline, but only if the first part looks like a title (short enough)
                    if (parts.size < 2) {
                        val singleNewlineParts = cleaned.split("\n", limit = 2)
                        if (singleNewlineParts.size >= 2 && singleNewlineParts[0].length < 100) {
                            parts = singleNewlineParts
                        }
                    }
                    
                    if (parts.size >= 2 && parts[0].length < 150) {
                        generatedTitle = parts[0].trim()
                        generatedBody = parts[1].trim()
                    } else {
                        // Fallback: if we can't split, assume the first line might be a title if it's short
                        val firstLineEnd = cleaned.indexOf('\n')
                        if (firstLineEnd != -1 && firstLineEnd < 100) {
                            generatedTitle = cleaned.substring(0, firstLineEnd).trim()
                            generatedBody = cleaned.substring(firstLineEnd + 1).trim()
                        } else {
                            generatedTitle = ""
                            generatedBody = cleaned
                        }
                    }
                } else {
                    generatedSource = ""
                    // Same logic for when no source is found
                    var parts = result.split("\n\n", limit = 2)
                    
                    if (parts.size < 2) {
                         val singleNewlineParts = result.split("\n", limit = 2)
                        if (singleNewlineParts.size >= 2 && singleNewlineParts[0].length < 100) {
                            parts = singleNewlineParts
                        }
                    }

                    if (parts.size >= 2 && parts[0].length < 150) {
                        generatedTitle = parts[0].trim()
                        generatedBody = parts[1].trim()
                    } else {
                        val firstLineEnd = result.indexOf('\n')
                        if (firstLineEnd != -1 && firstLineEnd < 100) {
                            generatedTitle = result.substring(0, firstLineEnd).trim()
                            generatedBody = result.substring(firstLineEnd + 1).trim()
                        } else {
                            generatedTitle = ""
                            generatedBody = result
                        }
                    }
                }
                rebuildOutput()
                hasResult = true
                error = null
                // Reset input changed flag
                inputHasChanged = false
                previousInput = inputText
            }
            !generationResult.success -> {
                error = generationResult.error ?: "Generation failed"
                // Trigger shake animation on error
                triggerShakeAnimation = true
            }
        }
        onClearResult()
    }
    
    // Rebuild output when toggles change
    LaunchedEffect(includeTitle, includeHashtags, includeSource) {
        if (hasResult) rebuildOutput()
    }
    
    // Initialize output if we have initial data
    LaunchedEffect(Unit) {
        if (hasResult) rebuildOutput()
    }
    
    // Track input changes for pulse animation
    LaunchedEffect(inputText) {
        if (inputText != previousInput && inputText.isNotBlank() && !isLoading) {
            inputHasChanged = true
        }
    }
    
    // Reset shake animation after it triggers
    LaunchedEffect(triggerShakeAnimation) {
        if (triggerShakeAnimation) {
            kotlinx.coroutines.delay(500)
            triggerShakeAnimation = false
        }
    }

    // Reset input shake animation after it triggers
    LaunchedEffect(triggerInputShake) {
        if (triggerInputShake) {
            kotlinx.coroutines.delay(500)
            triggerInputShake = false
        }
    }
    
    // Success animation timeout
    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            kotlinx.coroutines.delay(1000)
            showSuccessAnimation = false
        }
    }
    
    // Rate limit dialog
    if (showRateLimitDialog && rateLimitInfo != null) {
        RateLimitDialog(
            currentProvider = rateLimitInfo!!.rateLimitProvider ?: "",
            retryDelayMs = rateLimitInfo!!.retryDelayMs,
            isRefinement = rateLimitInfo!!.isRefinement,
            prefsManager = prefsManager,
            onSwitchAndRetry = { newProvider ->
                prefsManager.saveProvider(newProvider)
                showRateLimitDialog = false
                if (rateLimitInfo!!.isRefinement) {
                    val allRefinements = refinementOptions.toList() + 
                        customPills.filter { selectedPillIds.contains(it.id) }.map { it.command }
                    onRefine(outputText, allRefinements, prefsManager.isSourceEnabled())
                } else {
                    onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                }
            },
            onDismiss = { showRateLimitDialog = false }
        )
    }
    
    // Provider selector sheet
    if (showProviderSelector) {
        ProviderSelectorSheet(
            prefsManager = prefsManager,
            onProviderSelected = { newProvider ->
                prefsManager.saveProvider(newProvider)
                // Retry the last operation
                if (error != null) {
                    error = null
                    isLoading = true
                    onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                }
            },
            onDismiss = { showProviderSelector = false }
        )
    }
    
    // Pill creation dialog
    if (showCreatePillDialog) {
        CreatePillDialog(
            onDismiss = { showCreatePillDialog = false },
            onConfirm = { name, command ->
                val newPill = GenerationPill(name = name, command = command)
                prefsManager.savePill(newPill)
                customPills.clear()
                customPills.addAll(prefsManager.getPills())
                showCreatePillDialog = false
                Toast.makeText(context, "Custom refinement created", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Pill edit dialog
    if (showEditPillDialog && pillToEdit != null) {
        EditPillDialog(
            pill = pillToEdit!!,
            onDismiss = { 
                showEditPillDialog = false
                pillToEdit = null
            },
            onSave = { name, command ->
                val updatedPill = pillToEdit!!.copy(name = name, command = command)
                prefsManager.savePill(updatedPill)
                customPills.clear()
                customPills.addAll(prefsManager.getPills())
                showEditPillDialog = false
                pillToEdit = null
                Toast.makeText(context, "Custom refinement updated", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                prefsManager.deletePill(pillToEdit!!.id)
                customPills.clear()
                customPills.addAll(prefsManager.getPills())
                // Deselect if it was selected
                if (selectedPillIds.contains(pillToEdit!!.id)) {
                    selectedPillIds.remove(pillToEdit!!.id)
                }
                showEditPillDialog = false
                pillToEdit = null
                Toast.makeText(context, "Custom refinement deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Neo Bottom Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Pulse animation for Generate button when input changes
                    val infiniteTransition = rememberInfiniteTransition(label = "generatePulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (inputHasChanged) 1.05f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (inputHasChanged) 0.85f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )
                    
                    NeoButton(
                        onClick = {
                            inputHasChanged = false // Stop pulsing when clicked
                            if (inputText.isBlank()) {
                                isInputError = true
                                triggerInputShake = true
                                HapticHelper(context).onError()
                            } else if (!prefsManager.hasApiKey()) {
                                Toast.makeText(context, R.string.api_key_required, Toast.LENGTH_LONG).show()
                                onNavigateToSettings()
                            } else {
                                isLoading = true
                                error = null
                                onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                            }
                        },
                        text = "GENERATE",
                        isLoading = isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                val currentScale = if (inputHasChanged) pulseScale else 1f
                                scaleX = currentScale
                                scaleY = currentScale
                                alpha = if (inputHasChanged) pulseAlpha else 1f
                            }
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onNavigateToUsage) {
                        Text("USAGE", style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = onNavigateToSettings) {
                        Text("SETTINGS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Neo Header
            Text(
                text = "SOCURATE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Input Card
            val inputShakeOffset by animateFloatAsState(
                targetValue = if (triggerInputShake) 1f else 0f,
                animationSpec = if (triggerInputShake) {
                    spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                } else {
                    tween(0)
                },
                label = "inputShake"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = kotlin.math.sin(inputShakeOffset * 8 * kotlin.math.PI.toFloat()) * 10f
                }
            ) {
                InputCard(
                    inputText = inputText,
                    onInputChange = {
                        inputText = it
                        if (isInputError) isInputError = false
                    },
                    keepStructure = keepStructure,
                    onKeepStructureChange = { keepStructure = it },
                    linkPreviewData = linkPreviewData,
                    isLoadingPreview = isLoadingPreview,
                    onExtractContent = { url ->
                        scope.launch {
                            try {
                                val extractor = WebContentExtractor()
                                val extractedContent = withContext(Dispatchers.IO) {
                                    extractor.extractContent(url)
                                }
                                inputText = extractedContent
                                if (isInputError) isInputError = false
                            } catch (e: Exception) {
                                error = "Failed to extract content: ${e.message}"
                            }
                        }
                    },
                    onDismissPreview = { linkPreviewData = null },
                    isError = isInputError
                )
            }
            
            // Loading State
            AnimatedVisibility(visible = isLoading, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                EnhancedLoadingCard()
            }
            
            // Error State with Shake Animation
            AnimatedVisibility(visible = error != null && !isLoading, enter = fadeIn(), exit = fadeOut()) {
                val shakeOffset by animateFloatAsState(
                    targetValue = if (triggerShakeAnimation) 1f else 0f,
                    animationSpec = if (triggerShakeAnimation) {
                        spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    } else {
                        tween(0)
                    },
                    label = "shake"
                )
                
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = kotlin.math.sin(shakeOffset * 8 * kotlin.math.PI.toFloat()) * 10f
                    }
                ) {
                    ErrorCard(
                        error = error ?: "",
                        onRetry = {
                            error = null
                            isLoading = true
                            onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                        },
                        onChangeProvider = {
                            showProviderSelector = true
                        }
                    )
                }
            }
            
            // Output Card
            AnimatedVisibility(visible = hasResult && !isLoading, enter = fadeIn() + expandVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Stable callbacks to prevent OutputCard recomposition on input change
                    val onOutputChange = remember { { it: String -> if (isEditMode) outputText = it } }
                    val onIncludeTitleChange = remember(rebuildOutput) { { it: Boolean -> includeTitle = it; rebuildOutput() } }
                    val onIncludeHashtagsChange = remember(rebuildOutput) { { it: Boolean -> includeHashtags = it; rebuildOutput() } }
                    val onIncludeSourceChange = remember(rebuildOutput) { { it: Boolean -> includeSource = it; rebuildOutput() } }
                    val onEditClick = remember(rebuildOutput) {
                        {
                            isEditMode = !isEditMode
                            if (!isEditMode) rebuildOutput()
                        }
                    }
                    val onCopyClick = remember {
                        {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Socurate Post", outputText))
                            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                        }
                    }
                    val onShareClick = remember(outputText) {
                        {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, outputText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        }
                    }
                    val onExpandClick = remember { { showReadingDialog = true } }

                    OutputCard(
                        outputText = outputText,
                        isEditMode = isEditMode,
                        onOutputChange = onOutputChange,
                        includeTitle = includeTitle,
                        includeHashtags = includeHashtags,
                        includeSource = includeSource,
                        hasHashtags = prefsManager.getHashtags()?.isNotEmpty() == true,
                        isSourceEnabled = prefsManager.isSourceEnabled(),
                        onIncludeTitleChange = onIncludeTitleChange,
                        onIncludeHashtagsChange = onIncludeHashtagsChange,
                        onIncludeSourceChange = onIncludeSourceChange,
                        onEditClick = onEditClick,
                        onCopyClick = onCopyClick,
                        onShareClick = onShareClick,
                        onExpandClick = onExpandClick,
                        textSize = textSizeState.intValue
                    )
                    
                    // Refinement Card
                    val onToggleOption = remember {
                        { option: String ->
                            if (refinementOptions.contains(option)) refinementOptions.remove(option)
                            else refinementOptions.add(option)
                        }
                    }
                    val onTogglePill = remember {
                        { pillId: String ->
                            if (selectedPillIds.contains(pillId)) selectedPillIds.remove(pillId)
                            else selectedPillIds.add(pillId)
                        }
                    }
                    val onRegenerateAction = remember(outputText) {
                        {
                            val allRefinements = refinementOptions.toList() + 
                                customPills.filter { selectedPillIds.contains(it.id) }.map { it.command }
                            if (allRefinements.isEmpty()) {
                                Toast.makeText(context, "Please select at least one refinement", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                hasResult = false
                                onRefine(outputText, allRefinements, prefsManager.isSourceEnabled())
                            }
                        }
                    }
                    val onCreatePill = remember { { showCreatePillDialog = true } }
                    val onEditPill = remember { { pill: GenerationPill -> pillToEdit = pill; showEditPillDialog = true } }

                    RefinementCard(
                        selectedOptions = refinementOptions,
                        customPills = customPills,
                        selectedPillIds = selectedPillIds,
                        onToggleOption = onToggleOption,
                        onTogglePill = onTogglePill,
                        onRegenerate = onRegenerateAction,
                        onCreatePill = onCreatePill,
                        onEditPill = onEditPill
                    )
                }
            }
            
            // Empty State
            AnimatedVisibility(visible = !hasResult && !isLoading && error == null && inputText.isBlank(), enter = fadeIn(), exit = fadeOut()) {
                EmptyStateCard(
                    onPaste = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip()) {
                            val text = clipboard.primaryClip?.getItemAt(0)?.text
                            if (text != null) {
                                inputText = text.toString()
                                Toast.makeText(context, "Content pasted", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            
            Spacer(Modifier.height(80.dp)) // Space for FAB
        }
        
        // Reading mode dialog
        if (showReadingDialog) {
            ReadingModeDialog(
                outputText = outputText,
                textSize = textSizeState.intValue,
                onDismiss = { showReadingDialog = false }
            )
        }
    }
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
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
    isError: Boolean = false
) {
    val context = LocalContext.current

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    ) {
        // Animate content size change when preview appears
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
                    androidx.compose.animation.AnimatedContent(
                        targetState = inputText.isNotEmpty(),
                        transitionSpec = {
                            (scaleIn() + fadeIn()) with (scaleOut() + fadeOut())
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
                }
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PRESERVE STRUCTURE", style = MaterialTheme.typography.labelLarge)

                Switch(
                    checked = keepStructure,
                    onCheckedChange = onKeepStructureChange,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.surface,
                        checkedBorderColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Animated Link Preview Section
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

            // Animated Loading State
            AnimatedVisibility(
                visible = isLoadingPreview,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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

@OptIn(ExperimentalLayoutApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
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
    textSize: Int
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header with Expand button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DISPLAY OPTIONS", style = MaterialTheme.typography.labelLarge)
            
            // Expand button for reading mode
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
        
        // Output toggles
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
        
        
        // Output text with swipe gestures and long-press menu
        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            AnimatedContent(
                targetState = isEditMode,
                transitionSpec = {
                    if (targetState) {
                        // Entering Edit Mode: Subtle expansion feel
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.98f)) with
                        (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.98f))
                    } else {
                        // Exiting Edit Mode: Subtle collapse feel
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.98f)) with
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
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
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
        
        // Stats
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
            
            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeoOutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f), text = if (isEditMode) "Save" else "Edit")
                NeoCopyButton(
                    onCopy = onCopyClick,
                    modifier = Modifier.weight(1f)
                )
                NeoButton(onClick = onShareClick, modifier = Modifier.weight(1f), text = "Share")
            }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RefinementCard(
    selectedOptions: List<String>,
    customPills: List<GenerationPill>,
    selectedPillIds: List<String>,
    onToggleOption: (String) -> Unit,
    onTogglePill: (String) -> Unit,
    onRegenerate: () -> Unit,
    onCreatePill: () -> Unit,
    onEditPill: (GenerationPill) -> Unit
) {
    val refinementLabels = listOf(
        "rephrase" to "Rephrase",
        "recheck_flow" to "Check Flow",
        "recheck_wording" to "Check Wording"
    )
    
    FluidRefinementFlow(
        options = refinementLabels,
        selectedOptions = selectedOptions,
        customPills = customPills,
        selectedPillIds = selectedPillIds,
        onToggleOption = onToggleOption,
        onTogglePill = onTogglePill,
        onCreatePill = onCreatePill,
        onEditPill = onEditPill,
        onRegenerate = onRegenerate
    )
}

@Composable
fun LoadingCard() {
    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(24.dp))
            Text("GENERATING...", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ErrorCard(error: String, onRetry: () -> Unit, onChangeProvider: () -> Unit) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ErrorRed
    ) {
        Text("GENERATION FAILED", style = MaterialTheme.typography.titleMedium, color = ErrorRed)
        Spacer(Modifier.height(8.dp))
        Text(error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeoOutlinedButton(
                onClick = onChangeProvider,
                modifier = Modifier.weight(1f),
                text = "CHANGE PROVIDER"
            )
            NeoButton(
                onClick = onRetry,
                text = "TRY AGAIN",
                containerColor = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateLimitDialog(
    currentProvider: String,
    retryDelayMs: Long,
    isRefinement: Boolean,
    prefsManager: PreferencesManager,
    onSwitchAndRetry: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allProviders = remember { prefsManager.getAllProvidersWithStatus() }
    val recommendedFallback = remember { prefsManager.getRecommendedFallbackProvider(currentProvider) }
    
    val waitTime = remember(retryDelayMs) {
        if (retryDelayMs > 60000) "${retryDelayMs / 60000} minutes" 
        else if (retryDelayMs > 0) "${retryDelayMs / 1000} seconds"
        else "a minute"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "⚠️ Rate Limit Hit",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Current status
                Text(
                    "${CuratorFactory.getProviderDisplayName(currentProvider)} is currently rate limited.",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (retryDelayMs > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Estimated wait time: ~$waitTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    "Switch to another provider:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(8.dp))
                
                // Provider options
                allProviders.forEach { (providerValue, displayName, hasApiKey) ->
                    if (providerValue != currentProvider) {
                        val isRecommended = providerValue == recommendedFallback
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = hasApiKey) {
                                    onSwitchAndRetry(providerValue)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                !hasApiKey -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                isRecommended -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = if (isRecommended) androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (hasApiKey) MaterialTheme.colorScheme.onSurface
                                                   else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isRecommended) {
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "RECOMMENDED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        if (hasApiKey) "✓ API Key Configured" else "⚠ API Key Required",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (hasApiKey) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.error
                                    )
                                }
                                
                                if (hasApiKey) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Switch",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Check if no alternatives available
                val hasAlternatives = allProviders.any { it.first != currentProvider && it.third }
                if (!hasAlternatives) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Configure API keys for other providers in Settings to enable fallback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                    onDismiss()
                }
            ) {
                Text("SETTINGS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("WAIT")
            }
        }
    )
}

/**
 * Provider selector sheet for changing AI provider and model.
 * Shown when user encounters errors or wants to manually switch providers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSelectorSheet(
    prefsManager: PreferencesManager,
    onProviderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentProvider = remember { prefsManager.getProvider() }
    val allProviders = remember { prefsManager.getAllProvidersWithStatus() }
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select AI Provider",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Current: ${CuratorFactory.getProviderDisplayName(currentProvider)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                allProviders.forEach { (providerValue, displayName, hasApiKey) ->
                    val isSelected = providerValue == selectedProvider
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedProvider = providerValue
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            !hasApiKey -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected || hasApiKey) 
                                        MaterialTheme.colorScheme.onSurface
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (hasApiKey) "✓ Ready to use" else "⚠ API Key Required",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasApiKey) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                            }
                            
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(android.R.drawable.radiobutton_on_background),
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Help text
                Text(
                    "Tip: Configure API keys for multiple providers in Settings for automatic fallback during rate limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                        onDismiss()
                    }
                ) {
                    Text("SETTINGS")
                }
                
                Button(
                    onClick = {
                        if (prefsManager.hasApiKeyForProvider(selectedProvider)) {
                            onProviderSelected(selectedProvider)
                            onDismiss()
                        } else {
                            Toast.makeText(
                                context,
                                "Please configure API key for this provider first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = prefsManager.hasApiKeyForProvider(selectedProvider)
                ) {
                    Text("SWITCH")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

/**
 * Dialog for creating a new custom refinement pill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, command: String) -> Unit
) {
    var pillName by remember { mutableStateOf("") }
    var pillCommand by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Create Custom Refinement",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Create a reusable refinement command that appears as a chip alongside built-in options.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                NeoInput(
                    value = pillName,
                    onValueChange = { pillName = it; showError = false },
                    label = "NAME",
                    placeholder = "e.g., Make it punchy",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                
                Spacer(Modifier.height(12.dp))
                
                NeoInput(
                    value = pillCommand,
                    onValueChange = { pillCommand = it; showError = false },
                    label = "COMMAND",
                    placeholder = "e.g., Make the text punchy and energetic",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                if (showError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Both name and command are required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pillName.isBlank() || pillCommand.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(pillName.trim(), pillCommand.trim())
                    }
                }
            ) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

/**
 * Dialog for editing or deleting an existing custom pill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPillDialog(
    pill: GenerationPill,
    onDismiss: () -> Unit,
    onSave: (name: String, command: String) -> Unit,
    onDelete: () -> Unit
) {
    var pillName by remember { mutableStateOf(pill.name) }
    var pillCommand by remember { mutableStateOf(pill.command) }
    var showError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Refinement?") },
            text = { Text("Are you sure you want to delete \"${pill.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Custom Refinement",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                NeoInput(
                    value = pillName,
                    onValueChange = { pillName = it; showError = false },
                    label = "NAME",
                    placeholder = "e.g., Make it punchy",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                
                Spacer(Modifier.height(12.dp))
                
                NeoInput(
                    value = pillCommand,
                    onValueChange = { pillCommand = it; showError = false },
                    label = "COMMAND",
                    placeholder = "e.g., Make the text punchy and energetic",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                if (showError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Both name and command are required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { showDeleteConfirm = true }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error)
                }
                
                Button(
                    onClick = {
                        if (pillName.isBlank() || pillCommand.isBlank()) {
                            showError = true
                        } else {
                            onSave(pillName.trim(), pillCommand.trim())
                        }
                    }
                ) {
                    Text("SAVE")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}


