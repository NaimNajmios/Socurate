package com.najmi.oreamnos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.UsageStats

import com.najmi.oreamnos.ui.theme.ErrorRed
import com.najmi.oreamnos.ui.theme.SocurateTheme
import com.najmi.oreamnos.ui.theme.SuccessGreen
import com.najmi.oreamnos.utils.PreferencesManager
import java.util.Locale

/**
 * Activity for displaying comprehensive API usage statistics.
 * Converted to Jetpack Compose.
 */
class UsageActivity : ComponentActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        setContent {
            var stats by remember { mutableStateOf(prefsManager.getUsageStats()) }
            val currentTheme = prefsManager.getTheme()
            
            SocurateTheme(themeMode = currentTheme) {
                UsageScreen(
                    stats = stats,
                    onNavigateBack = { finish() },
                    onResetStats = {
                        prefsManager.resetUsageStats()
                        stats = prefsManager.getUsageStats()
                    },
                    onClearLogs = {
                        prefsManager.clearLogs()
                        stats = prefsManager.getUsageStats()
                    },
                    onViewAllSessions = {
                        startActivity(Intent(this, SessionListActivity::class.java))
                    },
                    onViewAllLogs = {
                        startActivity(Intent(this, LogListActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(
    stats: UsageStats,
    onNavigateBack: () -> Unit,
    onResetStats: () -> Unit,
    onClearLogs: () -> Unit,
    onViewAllSessions: () -> Unit,
    onViewAllLogs: () -> Unit
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<UsageStats.LogEntry?>(null) }
    
    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to reset all usage statistics? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onResetStats()
                    showResetDialog = false
                    Toast.makeText(context, "Statistics reset", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Reset", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    // Clear logs dialog
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = { Text("Clear Logs") },
            text = { Text("Are you sure you want to clear all logs?") },
            confirmButton = {
                TextButton(onClick = {
                    onClearLogs()
                    showClearLogsDialog = false
                    Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    // Log details dialog
    selectedLog?.let { log ->
        LogDetailsDialog(
            log = log,
            onDismiss = { selectedLog = null },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = buildLogText(log)
                clipboard.setPrimaryClip(ClipData.newPlainText("Log Details", text))
                Toast.makeText(context, "Log details copied", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            // Overview Card
            item { OverviewCard(stats) }
            
            // Time-based Stats
            item { TimeBasedStatsCard(stats) }
            
            // Token Breakdown
            item { TokenBreakdownCard(stats) }
            
            // Provider Stats
            item { ProviderStatsCard(stats) }
            
            // Request Stats
            item { RequestStatsCard(stats) }
            
            // Recent Sessions
            item {
                RecentSessionsCard(
                    sessions = stats.getRecentSessions().take(5),
                    totalCount = stats.getRecentSessions().size,
                    onViewAll = onViewAllSessions
                )
            }
            
            // Recent Logs
            item {
                RecentLogsCard(
                    logs = stats.getLogs().take(5),
                    totalCount = stats.getLogs().size,
                    onViewAll = onViewAllLogs,
                    onLogClick = { selectedLog = it },
                    onClearLogs = { showClearLogsDialog = true }
                )
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun OverviewCard(stats: UsageStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = formatNumber(stats.totalTokens),
                label = "Total Tokens"
            )
            StatItem(
                value = String.format(Locale.US, "%.0f%%", stats.successRate),
                label = "Success Rate"
            )
            StatItem(
                value = String.format(Locale.US, "%d", stats.totalRequests),
                label = "Requests"
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimeBasedStatsCard(stats: UsageStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Time-Based Usage",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeStatItem("Today", formatNumber(stats.getTodayTokens()), "${stats.getTodayRequests()} req")
                TimeStatItem("This Week", formatNumber(stats.getWeekTokens()), "${stats.getWeekRequests()} req")
                TimeStatItem("This Month", formatNumber(stats.getMonthTokens()), "${stats.getMonthRequests()} req")
            }
        }
    }
}

@Composable
fun TimeStatItem(title: String, tokens: String, requests: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(tokens, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(requests, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TokenBreakdownCard(stats: UsageStats) {
    val promptTokens = stats.totalPromptTokens
    val responseTokens = stats.totalCandidateTokens
    val total = promptTokens + responseTokens
    val promptPercent = if (total > 0) promptTokens.toFloat() / total else 0.5f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Token Breakdown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Prompt: ${formatNumber(promptTokens)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Response: ${formatNumber(responseTokens)}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
            }
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier.weight(promptPercent).fillMaxSize().background(MaterialTheme.colorScheme.primary))
                Box(modifier = Modifier.weight(1f - promptPercent).fillMaxSize().background(SuccessGreen))
            }
        }
    }
}

@Composable
fun ProviderStatsCard(stats: UsageStats) {
    val providerStats = stats.getProviderStats()
    val totalTokens = stats.totalTokens
    
    val geminiTokens = providerStats["gemini"]?.totalTokens ?: 0L
    val groqTokens = providerStats["groq"]?.totalTokens ?: 0L
    val orTokens = providerStats["openrouter"]?.totalTokens ?: 0L
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Provider Usage", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            
            ProviderRow("Gemini", geminiTokens, totalTokens, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            ProviderRow("Groq", groqTokens, totalTokens, Color(0xFFFF6B6B))
            Spacer(Modifier.height(8.dp))
            ProviderRow("OpenRouter", orTokens, totalTokens, Color(0xFF4ECDC4))
        }
    }
}

@Composable
fun ProviderRow(name: String, tokens: Long, total: Long, color: Color) {
    val progress = if (total > 0) tokens.toFloat() / total else 0f
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(formatNumber(tokens), style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun RequestStatsCard(stats: UsageStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${stats.successfulRequests}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text("Successful", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${stats.failedRequests}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
                Text("Failed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RecentSessionsCard(
    sessions: List<UsageStats.SessionEntry>,
    totalCount: Int,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Sessions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (totalCount > 5) {
                    TextButton(onClick = onViewAll) {
                        Text("View All ($totalCount)")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            if (sessions.isEmpty()) {
                Text("No sessions yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                sessions.forEach { session ->
                    SessionItem(session)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecentLogsCard(
    logs: List<UsageStats.LogEntry>,
    totalCount: Int,
    onViewAll: () -> Unit,
    onLogClick: (UsageStats.LogEntry) -> Unit,
    onClearLogs: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Logs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("$totalCount", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Row {
                    if (totalCount > 5) {
                        TextButton(onClick = onViewAll) { Text("View All") }
                    }
                    IconButton(onClick = onClearLogs) {
                        Icon(Icons.Default.Delete, "Clear", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            if (logs.isEmpty()) {
                Text("No logs yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                logs.forEach { log ->
                    LogItem(log) { onLogClick(log) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> String.format(Locale.US, "%,d", number)
    }
}

private fun buildLogText(log: UsageStats.LogEntry): String {
    return buildString {
        append("Level: ${log.level}\n")
        append("Time: ${log.formattedDate}\n")
        append("Tag: ${log.tag}\n")
        append("Message: ${log.message}\n")
        if (!log.details.isNullOrEmpty()) {
            append("Details:\n${log.details}")
        }
    }
}
