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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.najmi.oreamnos.curator.CuratorFactory
import com.najmi.oreamnos.model.GenerationPill
import com.najmi.oreamnos.services.ContentGenerationService
import com.najmi.oreamnos.services.WebContentExtractor
import com.najmi.oreamnos.ui.theme.ErrorRed
import com.najmi.oreamnos.ui.theme.SocurateTheme
import com.najmi.oreamnos.utils.PreferencesManager
import com.najmi.oreamnos.utils.ReadabilityUtils
import com.najmi.oreamnos.utils.StringUtils
import com.najmi.oreamnos.viewmodel.MainViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "=== MainActivity onCreate ===")
        
        prefsManager = PreferencesManager(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        applyTheme(prefsManager.getTheme())
        
        // Get incoming intent data
        val sharedText = intent?.getStringExtra("shared_text")
        val generatedContent = intent?.getStringExtra("generated_content")
        val intentTitle = intent?.getStringExtra("generated_title")
        val intentBody = intent?.getStringExtra("generated_body")
        val intentSource = intent?.getStringExtra("generated_source")
        
        setContent {
            SocurateTheme {
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
    
    // Custom pills
    val customPills = remember { mutableStateListOf<GenerationPill>() }
    val selectedPillIds = remember { mutableStateListOf<String>() }
    
    // Load custom pills
    LaunchedEffect(Unit) {
        customPills.clear()
        customPills.addAll(prefsManager.getPills())
    }
    
    // Helper function to rebuild output
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
                // Extract source citation
                val result = generationResult.result
                val matcher = SOURCE_CITATION_PATTERN.find(result)
                if (matcher != null) {
                    generatedSource = matcher.value.trim()
                    val contentWithoutSource = SOURCE_CITATION_PATTERN.replace(result, "").trim()
                    val cleaned = TRAILING_NEWLINES_PATTERN.replace(contentWithoutSource, "").trim()
                    val parts = cleaned.split("\n\n", limit = 2)
                    if (parts.size >= 2 && parts[0].length < 150) {
                        generatedTitle = parts[0].trim()
                        generatedBody = parts[1].trim()
                    } else {
                        generatedTitle = ""
                        generatedBody = cleaned
                    }
                } else {
                    generatedSource = ""
                    val parts = result.split("\n\n", limit = 2)
                    if (parts.size >= 2 && parts[0].length < 150) {
                        generatedTitle = parts[0].trim()
                        generatedBody = parts[1].trim()
                    } else {
                        generatedTitle = ""
                        generatedBody = result
                    }
                }
                rebuildOutput()
                hasResult = true
                error = null
            }
            !generationResult.success -> {
                error = generationResult.error ?: "Generation failed"
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Socurate") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, "Generate") },
                    label = { Text("Generate") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_usage), "Usage") },
                    label = { Text("Usage") },
                    selected = false,
                    onClick = onNavigateToUsage
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = onNavigateToSettings
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (inputText.isBlank()) {
                        Toast.makeText(context, R.string.input_required, Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    if (!prefsManager.hasApiKey()) {
                        Toast.makeText(context, R.string.api_key_required, Toast.LENGTH_LONG).show()
                        onNavigateToSettings()
                        return@ExtendedFloatingActionButton
                    }
                    isLoading = true
                    error = null
                    onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                },
                icon = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) else Icon(Icons.AutoMirrored.Filled.Send, "Generate") },
                text = { Text(if (isLoading) "Generating..." else "Generate") },
                expanded = !isLoading
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Card
            InputCard(
                inputText = inputText,
                onInputChange = { inputText = it },
                keepStructure = keepStructure,
                onKeepStructureChange = { keepStructure = it }
            )
            
            // Loading State
            AnimatedVisibility(visible = isLoading, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                LoadingCard()
            }
            
            // Error State
            AnimatedVisibility(visible = error != null && !isLoading, enter = fadeIn(), exit = fadeOut()) {
                ErrorCard(
                    error = error ?: "",
                    onRetry = {
                        error = null
                        isLoading = true
                        onGenerate(inputText, prefsManager.isSourceEnabled(), keepStructure)
                    }
                )
            }
            
            // Output Card
            AnimatedVisibility(visible = hasResult && !isLoading, enter = fadeIn() + expandVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutputCard(
                        outputText = outputText,
                        isEditMode = isEditMode,
                        onOutputChange = { if (isEditMode) outputText = it },
                        includeTitle = includeTitle,
                        includeHashtags = includeHashtags,
                        includeSource = includeSource,
                        hasHashtags = prefsManager.getHashtags()?.isNotEmpty() == true,
                        isSourceEnabled = prefsManager.isSourceEnabled(),
                        onIncludeTitleChange = { includeTitle = it },
                        onIncludeHashtagsChange = { includeHashtags = it },
                        onIncludeSourceChange = { includeSource = it },
                        onEditClick = { isEditMode = !isEditMode },
                        onCopyClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Socurate Post", outputText))
                            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                        },
                        onShareClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, outputText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        }
                    )
                    
                    // Refinement Card
                    RefinementCard(
                        selectedOptions = refinementOptions,
                        customPills = customPills,
                        selectedPillIds = selectedPillIds,
                        onToggleOption = { option ->
                            if (refinementOptions.contains(option)) refinementOptions.remove(option)
                            else refinementOptions.add(option)
                        },
                        onTogglePill = { pillId ->
                            if (selectedPillIds.contains(pillId)) selectedPillIds.remove(pillId)
                            else selectedPillIds.add(pillId)
                        },
                        onRegenerate = {
                            val allRefinements = refinementOptions.toList() + 
                                customPills.filter { selectedPillIds.contains(it.id) }.map { it.command }
                            if (allRefinements.isEmpty()) {
                                Toast.makeText(context, "Please select at least one refinement", Toast.LENGTH_SHORT).show()
                                return@RefinementCard
                            }
                            isLoading = true
                            hasResult = false
                            onRefine(outputText, allRefinements, prefsManager.isSourceEnabled())
                        }
                    )
                }
            }
            
            // Empty State
            AnimatedVisibility(visible = !hasResult && !isLoading && error == null, enter = fadeIn(), exit = fadeOut()) {
                EmptyStateCard(
                    onPaste = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        if (clipboard.hasPrimaryClip()) {
                            val text = clipboard.primaryClip?.getItemAt(0)?.text
                            if (text != null) {
                                inputText = text.toString()
                            }
                        }
                    }
                )
            }
            
            Spacer(Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Composable
fun InputCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    keepStructure: Boolean,
    onKeepStructureChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter text or URL") },
                placeholder = { Text("Paste article text or news URL...") },
                minLines = 3,
                maxLines = 8,
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { onInputChange("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Keep original structure", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = keepStructure, onCheckedChange = onKeepStructureChange)
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Generating your post...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ErrorCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Generation Failed", style = MaterialTheme.typography.titleMedium, color = ErrorRed)
            Spacer(Modifier.height(8.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Try Again") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Output toggles
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = includeTitle, onClick = { onIncludeTitleChange(!includeTitle) }, label = { Text("Title") })
                if (hasHashtags) {
                    FilterChip(selected = includeHashtags, onClick = { onIncludeHashtagsChange(!includeHashtags) }, label = { Text("Hashtags") })
                }
                if (isSourceEnabled) {
                    FilterChip(selected = includeSource, onClick = { onIncludeSourceChange(!includeSource) }, label = { Text("Source") })
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Output text
            OutlinedTextField(
                value = outputText,
                onValueChange = onOutputChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode,
                minLines = 5,
                maxLines = 15
            )
            
            // Stats
            val wordCount = if (outputText.isBlank()) 0 else WHITESPACE_PATTERN.split(outputText).size
            val gradeLevel = ReadabilityUtils.calculateFleschKincaidGradeLevel(outputText)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$wordCount words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Grade: ${String.format("%.1f", gradeLevel)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isEditMode) "Save" else "Edit")
                }
                OutlinedButton(onClick = onCopyClick, modifier = Modifier.weight(1f)) { Text("Copy") }
                Button(onClick = onShareClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
            }
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
    onRegenerate: () -> Unit
) {
    val refinementLabels = listOf(
        "rephrase" to "Rephrase",
        "recheck_flow" to "Check Flow",
        "recheck_wording" to "Check Wording",
        "formal" to "More Formal",
        "conversational" to "More Casual",
        "shorten_detailed" to "Shorten"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Refine Output", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                refinementLabels.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedOptions.contains(key),
                        onClick = { onToggleOption(key) },
                        label = { Text(label) }
                    )
                }
                
                // Custom pills
                customPills.forEach { pill ->
                    FilterChip(
                        selected = selectedPillIds.contains(pill.id),
                        onClick = { onTogglePill(pill.id) },
                        label = { Text(pill.name) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onRegenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Regenerate")
            }
        }
    }
}

@Composable
fun EmptyStateCard(onPaste: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_state),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text("Ready to generate", style = MaterialTheme.typography.titleMedium)
            Text("Paste content or enter a URL above", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onPaste) {
                Text("Paste from Clipboard")
            }
        }
    }
}

@Composable
fun RateLimitDialog(
    currentProvider: String,
    retryDelayMs: Long,
    isRefinement: Boolean,
    prefsManager: PreferencesManager,
    onSwitchAndRetry: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fallbackProvider = when (currentProvider) {
        PreferencesManager.PROVIDER_GEMINI -> PreferencesManager.PROVIDER_GROQ
        PreferencesManager.PROVIDER_GROQ -> PreferencesManager.PROVIDER_OPENROUTER
        else -> PreferencesManager.PROVIDER_GEMINI
    }
    
    val fallbackName = CuratorFactory.getProviderDisplayName(fallbackProvider)
    val hasFallbackKey = when (fallbackProvider) {
        PreferencesManager.PROVIDER_GROQ -> prefsManager.getGroqApiKey()?.isNotEmpty() == true
        PreferencesManager.PROVIDER_OPENROUTER -> prefsManager.getOpenRouterApiKey()?.isNotEmpty() == true
        else -> prefsManager.getApiKey()?.isNotEmpty() == true
    }
    
    val waitTime = if (retryDelayMs > 60000) "${retryDelayMs / 60000} minutes" else "${retryDelayMs / 1000} seconds"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Limit Hit") },
        text = {
            Column {
                Text("${CuratorFactory.getProviderDisplayName(currentProvider)} is rate limited.")
                if (retryDelayMs > 0) {
                    Text("Wait time: ~$waitTime")
                }
                Spacer(Modifier.height(8.dp))
                if (hasFallbackKey) {
                    Text("Switch to $fallbackName and retry?")
                } else {
                    Text("Configure $fallbackName API key in Settings to use as fallback.")
                }
            }
        },
        confirmButton = {
            if (hasFallbackKey) {
                TextButton(onClick = { onSwitchAndRetry(fallbackProvider) }) {
                    Text("Switch & Retry")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Wait")
            }
        }
    )
}
