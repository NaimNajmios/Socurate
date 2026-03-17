package com.najmi.oreamnos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.curator.OpenAICompatibleCurator
import com.najmi.oreamnos.services.GeminiService
import com.najmi.oreamnos.ui.theme.SocurateTheme
import com.najmi.oreamnos.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.najmi.oreamnos.vision.VisionModel
import com.najmi.oreamnos.utils.VisionModelManager
import kotlinx.coroutines.flow.collect

/**
 * Settings activity for configuring API key, tone, and model selection.
 * Converted to Jetpack Compose.
 */
class SettingsActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "SettingsActivity"
        
        // Gemini models
        val GEMINI_MODEL_NAMES = arrayOf(
            "Gemini 3.1 Pro", "Gemini 3 Flash", "Gemini 3.1 Flash Lite",
            "Gemini 2.5 Pro", "Gemini 2.5 Flash", "Gemini 2.5 Flash Lite"
        )
        val GEMINI_MODEL_ENDPOINTS = arrayOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"
        )
        
        // Groq models
        val GROQ_MODEL_NAMES = arrayOf(
            "DeepSeek R1 Distill Llama 70B", "Llama 3.3 70B Versatile", "Llama 3.1 8B Instant",
            "Qwen QwQ 32B", "GPT OSS 120B"
        )
        val GROQ_MODEL_IDS = arrayOf(
            "deepseek-r1-distill-llama-70b", "llama-3.3-70b-versatile", "llama-3.1-8b-instant",
            "qwen-qwq-32b", "openai/gpt-oss-120b"
        )
        
        // OpenRouter models
        val OPENROUTER_MODEL_NAMES = arrayOf(
            "DeepSeek R1 Zero", "DeepSeek V3 Base", "Llama 4 Maverick",
            "Gemini 2.5 Pro Exp", "GPT OSS 120B", "Llama 3.3 70B Instruct"
        )
        val OPENROUTER_MODEL_IDS = arrayOf(
            "deepseek/deepseek-r1-zero:free", "deepseek/deepseek-v3-base:free", "meta-llama/llama-4-maverick:free",
            "google/gemini-2.5-pro-exp-03-25:free", "openai/gpt-oss-120b:free", "meta-llama/llama-3.3-70b-instruct:free"
        )
        
        // Cerebras models
        val CEREBRAS_MODEL_NAMES = arrayOf(
            "GPT-5.3 Codex Spark", "Llama 3.3 70B", "Llama 3.1 8B", "Z.ai GLM 4.7"
        )
        val CEREBRAS_MODEL_IDS = arrayOf(
            "gpt-5.3-codex-spark", "llama-3.3-70b", "llama3.1-8b", "zai-glm-4.7"
        )
        
        val PROVIDER_NAMES = arrayOf("Gemini", "Groq", "OpenRouter", "Cerebras")
        val PROVIDER_VALUES = arrayOf("gemini", "groq", "openrouter", "cerebras")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "=== SettingsActivity onCreate ===")
        
        val prefsManager = PreferencesManager(this)
        applyTheme(prefsManager.getTheme())
        
        setContent {
            val currentTheme = remember { mutableStateOf(prefsManager.getTheme()) }

            SocurateTheme(themeMode = currentTheme.value) {
                SettingsScreen(
                    prefsManager = prefsManager,
                    onNavigateBack = { finish() },
                    onNavigateToHashtags = { startActivity(Intent(this, HashtagManagerActivity::class.java)) },
                    onNavigateToUsage = { startActivity(Intent(this, UsageActivity::class.java)) },
                    onThemeChanged = { theme -> 
                        currentTheme.value = theme
                        applyTheme(theme) 
                    }
                )
            }
        }
    }
    
    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            PreferencesManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    prefsManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onNavigateToHashtags: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var provider by remember { mutableStateOf(prefsManager.getProvider()) }
    var geminiKey by remember { mutableStateOf(prefsManager.getApiKey() ?: "") }
    var groqKey by remember { mutableStateOf(prefsManager.getGroqApiKey() ?: "") }
    var openRouterKey by remember { mutableStateOf(prefsManager.getOpenRouterApiKey() ?: "") }
    var cerebrasKey by remember { mutableStateOf(prefsManager.getCerebrasApiKey() ?: "") }
    var selectedModelIndex by remember { mutableIntStateOf(0) }
    var theme by remember { mutableStateOf(prefsManager.getTheme()) }
    var hashtagsEnabled by remember { mutableStateOf(prefsManager.areHashtagsEnabled()) }
    var sourceEnabled by remember { mutableStateOf(prefsManager.isSourceEnabled()) }
    var textSize by remember { mutableIntStateOf(prefsManager.getTextSize()) }
    var isTesting by remember { mutableStateOf(false) }
    
    // Get current model arrays based on provider
    val (modelNames, modelIds) = remember(provider) {
        when (provider) {
            PreferencesManager.PROVIDER_GROQ -> SettingsActivity.GROQ_MODEL_NAMES to SettingsActivity.GROQ_MODEL_IDS
            PreferencesManager.PROVIDER_OPENROUTER -> SettingsActivity.OPENROUTER_MODEL_NAMES to SettingsActivity.OPENROUTER_MODEL_IDS
            PreferencesManager.PROVIDER_CEREBRAS -> SettingsActivity.CEREBRAS_MODEL_NAMES to SettingsActivity.CEREBRAS_MODEL_IDS
            else -> SettingsActivity.GEMINI_MODEL_NAMES to SettingsActivity.GEMINI_MODEL_ENDPOINTS
        }
    }
    
    // Load saved model index for current provider
    remember(provider) {
        val savedModel = prefsManager.getModelForProvider(provider)
        selectedModelIndex = modelIds.indexOfFirst { it == savedModel }.coerceAtLeast(0)
    }
    
    fun showSaved() = Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            // AI Provider Section
            SettingsCard(title = "AI Provider") {
                DropdownSelector(
                    label = "Provider",
                    options = SettingsActivity.PROVIDER_NAMES.toList(),
                    selectedIndex = SettingsActivity.PROVIDER_VALUES.indexOf(provider).coerceAtLeast(0),
                    onSelect = { index: Int ->
                        provider = SettingsActivity.PROVIDER_VALUES[index]
                        prefsManager.saveProvider(provider)
                        showSaved()
                    }
                )
                
                Spacer(Modifier.height(12.dp))
                
                // API Key input based on provider
                when (provider) {
                    PreferencesManager.PROVIDER_GROQ -> ApiKeyInput(
                        label = "Groq API Key",
                        value = groqKey,
                        onValueChange = { groqKey = it },
                        onSave = { prefsManager.saveGroqApiKey(groqKey); showSaved() }
                    )
                    PreferencesManager.PROVIDER_OPENROUTER -> ApiKeyInput(
                        label = "OpenRouter API Key",
                        value = openRouterKey,
                        onValueChange = { openRouterKey = it },
                        onSave = { prefsManager.saveOpenRouterApiKey(openRouterKey); showSaved() }
                    )
                    PreferencesManager.PROVIDER_CEREBRAS -> ApiKeyInput(
                        label = "Cerebras API Key",
                        value = cerebrasKey,
                        onValueChange = { cerebrasKey = it },
                        onSave = { prefsManager.saveCerebrasApiKey(cerebrasKey); showSaved() }
                    )
                    else -> ApiKeyInput(
                        label = "Gemini API Key",
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
                        onSave = { prefsManager.saveApiKey(geminiKey); showSaved() }
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                DropdownSelector(
                    label = "Model",
                    options = modelNames.toList(),
                    selectedIndex = selectedModelIndex,
                    onSelect = { index: Int ->
                        selectedModelIndex = index
                        prefsManager.saveModelForProvider(provider, modelIds[index])
                        showSaved()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Test Connection Button
                Button(
                    onClick = {
                        val apiKey = when (provider) {
                            PreferencesManager.PROVIDER_GROQ -> groqKey
                            PreferencesManager.PROVIDER_OPENROUTER -> openRouterKey
                            PreferencesManager.PROVIDER_CEREBRAS -> cerebrasKey
                            else -> geminiKey
                        }
                        if (apiKey.isEmpty()) {
                            Toast.makeText(context, "Please enter an API key first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isTesting = true
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    testConnection(provider, apiKey, modelIds[selectedModelIndex])
                                }
                                Toast.makeText(context, "Connection successful! API key saved.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Test failed: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isTesting = false
                            }
                        }
                    },
                    enabled = !isTesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isTesting) "Testing..." else "Test Connection")
                }
            }
            
            // Vision AI Section
            SettingsCard(title = "Vision AI") {
                val visionModelManager = remember(context) { VisionModelManager(context) }
                var installedModels by remember { mutableStateOf(visionModelManager.getInstalledModels()) }
                var downloadingModel by remember { mutableStateOf<VisionModel?>(null) }
                var downloadProgress by remember { mutableStateOf(0f) }
                var hfToken by remember { mutableStateOf(prefsManager.getHfToken() ?: "") }
                
                // HF Token Input
                ApiKeyInput(
                    label = "HuggingFace Read Token (for gated models)",
                    value = hfToken,
                    onValueChange = { hfToken = it },
                    onSave = { 
                        prefsManager.saveHfToken(hfToken)
                        Toast.makeText(context, "HF Token saved", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Text(
                    text = "Gemma 3 and PaliGemma models are gated. Please accept terms on HuggingFace and provide a Read Token.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // On-Device OCR Status (Always Ready)
                VisionModelRow(
                    name = "On-Device OCR",
                    description = "Ready - ML Kit with AI structuring",
                    status = "READY",
                    isReady = true
                )
                
                Spacer(Modifier.height(16.dp))
                
                // PaliGemma Status
                val paligemma = VisionModel.PALIGEMMA_3B
                VisionModelRow(
                    name = "PaliGemma 2 3B",
                    description = "~3GB · Mid-range friendly",
                    status = if (installedModels.contains(paligemma)) "READY" else "NOT DOWNLOADED",
                    isReady = installedModels.contains(paligemma),
                    isDownloading = downloadingModel == paligemma,
                    progress = downloadProgress,
                    onDownload = {
                        downloadingModel = paligemma
                        scope.launch {
                            visionModelManager.downloadModelFlow(paligemma, hfToken = hfToken).collect { progress ->
                                when (progress) {
                                    is VisionModelManager.DownloadProgress.InProgress -> {
                                        downloadProgress = progress.progress
                                    }
                                    is VisionModelManager.DownloadProgress.Completed -> {
                                        downloadingModel = null
                                        installedModels = visionModelManager.getInstalledModels()
                                        Toast.makeText(context, "PaliGemma downloaded!", Toast.LENGTH_SHORT).show()
                                    }
                                    is VisionModelManager.DownloadProgress.Failed -> {
                                        downloadingModel = null
                                        Toast.makeText(context, "Download failed: ${progress.error}", Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    },
                    onDelete = {
                        visionModelManager.deleteModel(paligemma)
                        installedModels = visionModelManager.getInstalledModels()
                        Toast.makeText(context, "Model deleted", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Gemma 3n Status
                val gemma3n = VisionModel.GEMMA_3N_E2B
                VisionModelRow(
                    name = "Gemma 3n E2B",
                    description = "~2.9GB · Multimodal (vision + text)",
                    status = if (installedModels.contains(gemma3n)) "READY" else "NOT DOWNLOADED",
                    isReady = installedModels.contains(gemma3n),
                    isDownloading = downloadingModel == gemma3n,
                    progress = downloadProgress,
                    onDownload = {
                        downloadingModel = gemma3n
                        scope.launch {
                            visionModelManager.downloadModelFlow(gemma3n, hfToken = hfToken).collect { progress ->
                                when (progress) {
                                    is VisionModelManager.DownloadProgress.InProgress -> {
                                        downloadProgress = progress.progress
                                    }
                                    is VisionModelManager.DownloadProgress.Completed -> {
                                        downloadingModel = null
                                        installedModels = visionModelManager.getInstalledModels()
                                        Toast.makeText(context, "Gemma 3n downloaded!", Toast.LENGTH_SHORT).show()
                                    }
                                    is VisionModelManager.DownloadProgress.Failed -> {
                                        downloadingModel = null
                                        Toast.makeText(context, "Download failed: ${progress.error}", Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    },
                    onDelete = {
                        visionModelManager.deleteModel(gemma3n)
                        installedModels = visionModelManager.getInstalledModels()
                        Toast.makeText(context, "Model deleted", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Gemma 3 1B Status
                val gemma3_1b = VisionModel.GEMMA_3_1B
                VisionModelRow(
                    name = "Gemma 3 1B",
                    description = "~557MB · Fast text structuring",
                    status = if (installedModels.contains(gemma3_1b)) "READY" else "NOT DOWNLOADED",
                    isReady = installedModels.contains(gemma3_1b),
                    isDownloading = downloadingModel == gemma3_1b,
                    progress = downloadProgress,
                    onDownload = {
                        downloadingModel = gemma3_1b
                        scope.launch {
                            visionModelManager.downloadModelFlow(gemma3_1b, hfToken = hfToken).collect { progress ->
                                when (progress) {
                                    is VisionModelManager.DownloadProgress.InProgress -> {
                                        downloadProgress = progress.progress
                                    }
                                    is VisionModelManager.DownloadProgress.Completed -> {
                                        downloadingModel = null
                                        installedModels = visionModelManager.getInstalledModels()
                                        Toast.makeText(context, "Gemma 3 1B downloaded!", Toast.LENGTH_SHORT).show()
                                    }
                                    is VisionModelManager.DownloadProgress.Failed -> {
                                        downloadingModel = null
                                        Toast.makeText(context, "Download failed: ${progress.error}", Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    },
                    onDelete = {
                        visionModelManager.deleteModel(gemma3_1b)
                        installedModels = visionModelManager.getInstalledModels()
                        Toast.makeText(context, "Model deleted", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Storage Used
                val totalStorage = visionModelManager.getTotalStorageUsedMb()
                Text(
                    text = "Storage used: ${totalStorage}MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Vision models enable advanced screenshot extraction. Gemma 3n is recommended for the best visual understanding.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Output Options Section
            SettingsCard(title = "Output Options") {
                // Text Size Selector
                DropdownSelector(
                    label = "Text Size",
                    options = listOf("Small (11sp)", "Medium (13sp)", "Large (15sp)", "Extra Large (17sp)"),
                    selectedIndex = when (textSize) {
                        PreferencesManager.TEXT_SIZE_SMALL -> 0
                        PreferencesManager.TEXT_SIZE_MEDIUM -> 1
                        PreferencesManager.TEXT_SIZE_LARGE -> 2
                        PreferencesManager.TEXT_SIZE_EXTRA_LARGE -> 3
                        else -> 1
                    },
                    onSelect = { index ->
                        textSize = when (index) {
                            0 -> PreferencesManager.TEXT_SIZE_SMALL
                            1 -> PreferencesManager.TEXT_SIZE_MEDIUM
                            2 -> PreferencesManager.TEXT_SIZE_LARGE
                            3 -> PreferencesManager.TEXT_SIZE_EXTRA_LARGE
                            else -> PreferencesManager.TEXT_SIZE_MEDIUM
                        }
                        prefsManager.saveTextSize(textSize)
                        showSaved()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                SettingsToggle(
                    title = "Include Source Citation",
                    subtitle = "Add URL source at the end of posts",
                    checked = sourceEnabled,
                    onCheckedChange = { sourceEnabled = it; prefsManager.saveSourceEnabled(it); showSaved() }
                )
                Spacer(Modifier.height(16.dp))
                SettingsToggle(
                    title = "Auto-append Hashtags",
                    subtitle = "Add hashtags to generated posts",
                    checked = hashtagsEnabled,
                    onCheckedChange = { hashtagsEnabled = it; prefsManager.setHashtagsEnabled(it); showSaved() }
                )
                Spacer(Modifier.height(8.dp))
                NavigationRow(
                    title = "Manage Hashtags",
                    onClick = onNavigateToHashtags
                )
            }
            
            // Theme Section
            SettingsCard(title = "Appearance") {
                Text("Theme", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        "System" to PreferencesManager.THEME_SYSTEM,
                        "Light" to PreferencesManager.THEME_LIGHT,
                        "Dark" to PreferencesManager.THEME_DARK,
                        "Deep Blue" to PreferencesManager.THEME_DEEP_BLUE,
                        "Midnight" to PreferencesManager.THEME_MIDNIGHT,
                        "Solarized" to PreferencesManager.THEME_SOLARIZED,
                        "Cyberpunk" to PreferencesManager.THEME_CYBERPUNK,
                        "Matchday" to PreferencesManager.THEME_MATCHDAY
                    )
                    themeOptions.forEach { (label, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = theme == value,
                                onClick = { theme = value; prefsManager.saveTheme(theme); onThemeChanged(theme); showSaved() }
                            )
                            Text(label, modifier = Modifier.clickable { theme = value; prefsManager.saveTheme(theme); onThemeChanged(theme); showSaved() })
                        }
                    }
                }
            }
            
            // Usage Stats Navigation
            SettingsCard(title = "Statistics") {
                NavigationRow(
                    title = "Usage Statistics",
                    subtitle = "View API usage and logs",
                    onClick = onNavigateToUsage
                )
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    com.najmi.oreamnos.ui.components.NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ApiKeyInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    com.najmi.oreamnos.ui.components.NeoInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSave() }),
        trailingIcon = {
            androidx.compose.material3.TextButton(onClick = onSave) {
                Text("Save", style = MaterialTheme.typography.labelMedium)
            }
        }
    )
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun NavigationRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VisionModelRow(
    name: String,
    description: String,
    status: String,
    isReady: Boolean,
    isDownloading: Boolean = false,
    progress: Float = 0f,
    onDownload: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                
                Text(
                    text = if (isDownloading) "${(progress * 100).toInt()}%" else status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (status != "READY" || name != "On-Device OCR") {
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = if (isReady) onDelete else onDownload,
                        modifier = Modifier.size(24.dp),
                        enabled = !isDownloading
                    ) {
                        Icon(
                            imageVector = if (isReady) Icons.Default.Delete 
                                         else Icons.Default.Download,
                            contentDescription = if (isReady) "Delete" else "Download",
                            modifier = Modifier.size(16.dp),
                            tint = if (isReady) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        if (isDownloading) {
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        }
    }
}

private suspend fun testConnection(provider: String, apiKey: String, modelId: String): String {
    return when (provider) {
        PreferencesManager.PROVIDER_GROQ -> {
            val curator = OpenAICompatibleCurator(apiKey, "https://api.groq.com/openai/v1/chat/completions", modelId, false)
            curator.curatePost("Test connection: Manchester United won 3-0.", true, false)
        }
        PreferencesManager.PROVIDER_OPENROUTER -> {
            val curator = OpenAICompatibleCurator(apiKey, "https://openrouter.ai/api/v1/chat/completions", modelId, true)
            curator.curatePost("Test connection: Manchester United won 3-0.", true, false)
        }
        PreferencesManager.PROVIDER_CEREBRAS -> {
            val curator = OpenAICompatibleCurator(apiKey, "https://api.cerebras.ai/v1/chat/completions", modelId, false)
            curator.curatePost("Test connection: Manchester United won 3-0.", true, false)
        }
        else -> {
            val gemini = GeminiService(apiKey, modelId)
            gemini.curatePost("Test connection: Manchester United won 3-0.", true, false)
        }
    }
}
