package com.najmi.oreamnos.model

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive model class for storing API usage statistics.
 * Tracks token counts, request statistics, per-provider/model breakdown,
 * and time-based aggregates.
 */
class UsageStats {

    // ==================== AGGREGATE STATS ====================

    // Cumulative token counts (all-time)
    var totalPromptTokens: Long = 0
        private set
    var totalCandidateTokens: Long = 0
        private set
    var totalTokens: Long = 0
        private set

    // Request counts (all-time)
    var totalRequests: Int = 0
        private set
    var successfulRequests: Int = 0
        private set
    var failedRequests: Int = 0
        private set

    // Last request stats
    var lastPromptTokens: Int = 0
        private set
    var lastCandidateTokens: Int = 0
        private set
    var lastTotalTokens: Int = 0
        private set
    var lastRequestTimestamp: Long = 0
        private set

    // ==================== TIME-BASED STATS ====================

    private var todayTokens: Long = 0
    private var todayRequests: Int = 0
    private var weekTokens: Long = 0
    private var weekRequests: Int = 0
    private var monthTokens: Long = 0
    private var monthRequests: Int = 0
    private var lastResetDate: String = currentDateString
    private var lastResetWeek: Int = currentWeek
    private var lastResetMonth: Int = currentMonth

    // ==================== PER-PROVIDER STATS ====================

    private var providerStats: MutableMap<String, ProviderStats> = mutableMapOf()

    // ==================== PER-MODEL STATS ====================

    private var modelStats: MutableMap<String, ModelStats> = mutableMapOf()

    // ==================== SESSION HISTORY ====================

    private var recentSessions: MutableList<SessionEntry> = mutableListOf()

    // ==================== LOG ENTRIES ====================

    private var logs: MutableList<LogEntry> = mutableListOf()

    // ==================== NESTED CLASSES ====================

    /**
     * Statistics for a specific AI provider.
     */
    class ProviderStats {
        var totalTokens: Long = 0
            private set
        var promptTokens: Long = 0
            private set
        var responseTokens: Long = 0
            private set
        var successfulRequests: Int = 0
            private set
        var failedRequests: Int = 0
            private set

        val totalRequests: Int
            get() = successfulRequests + failedRequests

        fun recordSuccess(prompt: Int, response: Int, total: Int) {
            promptTokens += prompt
            responseTokens += response
            totalTokens += total
            successfulRequests++
        }

        fun recordFailure() {
            failedRequests++
        }
    }

    /**
     * Statistics for a specific AI model.
     */
    class ModelStats(
        val modelName: String? = null,
        val provider: String? = null
    ) {
        var totalTokens: Long = 0
            private set
        var requests: Int = 0
            private set

        fun recordUsage(tokens: Int) {
            totalTokens += tokens
            requests++
        }
    }

    /**
     * A single session/request entry for history tracking.
     */
    class SessionEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val provider: String? = null,
        val modelId: String? = null,
        val modelName: String? = null,
        val promptTokens: Int = 0,
        val responseTokens: Int = 0,
        val totalTokens: Int = 0,
        val success: Boolean = true,
        val errorMessage: String? = null,
        val durationMs: Long = 0 // Response time in milliseconds
    ) {
        val formattedTime: String
            get() {
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }

        // Java compatibility method for boolean property
        fun isSuccess(): Boolean = success

        companion object {
            @JvmStatic
            fun failure(provider: String?, modelId: String?, modelName: String?, error: String?, durationMs: Long = 0): SessionEntry {
                return SessionEntry(
                    provider = provider,
                    modelId = modelId,
                    modelName = modelName,
                    success = false,
                    errorMessage = error,
                    durationMs = durationMs
                )
            }
        }
    }

    /**
     * A log entry for tracking app/API events and errors.
     */
    class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val level: String = LEVEL_INFO,
        val tag: String? = null,
        val message: String? = null,
        val details: String? = null
    ) {
        val formattedTime: String
            get() {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }

        val formattedDate: String
            get() {
                val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }

        companion object {
            const val LEVEL_INFO = "INFO"
            const val LEVEL_WARNING = "WARN"
            const val LEVEL_ERROR = "ERROR"
            const val LEVEL_DEBUG = "DEBUG"

            @JvmStatic
            fun info(tag: String, message: String): LogEntry =
                LogEntry(level = LEVEL_INFO, tag = tag, message = message)

            @JvmStatic
            fun warning(tag: String, message: String, details: String?): LogEntry =
                LogEntry(level = LEVEL_WARNING, tag = tag, message = message, details = details)

            @JvmStatic
            fun error(tag: String, message: String, details: String?): LogEntry =
                LogEntry(level = LEVEL_ERROR, tag = tag, message = message, details = details)

            @JvmStatic
            fun debug(tag: String, message: String): LogEntry =
                LogEntry(level = LEVEL_DEBUG, tag = tag, message = message)
        }
    }

    // ==================== TIME HELPERS ====================

    /**
     * Checks and resets time-based counters if needed.
     */
    fun checkAndResetTimePeriods() {
        val today = currentDateString
        val currentWeekVal = currentWeek
        val currentMonthVal = currentMonth

        // Reset daily stats if day changed
        if (today != lastResetDate) {
            todayTokens = 0
            todayRequests = 0
            lastResetDate = today
        }

        // Reset weekly stats if week changed
        if (currentWeekVal != lastResetWeek) {
            weekTokens = 0
            weekRequests = 0
            lastResetWeek = currentWeekVal
        }

        // Reset monthly stats if month changed
        if (currentMonthVal != lastResetMonth) {
            monthTokens = 0
            monthRequests = 0
            lastResetMonth = currentMonthVal
        }
    }

    // ==================== RECORDING METHODS ====================

    /**
     * Records a successful API call with comprehensive tracking.
     */
    @JvmOverloads
    fun recordSuccess(
        promptTokens: Int,
        candidateTokens: Int,
        totalTokensUsed: Int,
        provider: String? = null,
        modelId: String? = null,
        modelName: String? = null,
        durationMs: Long = 0
    ) {
        checkAndResetTimePeriods()

        // Update last request
        lastPromptTokens = promptTokens
        lastCandidateTokens = candidateTokens
        lastTotalTokens = totalTokensUsed
        lastRequestTimestamp = System.currentTimeMillis()

        // Update all-time totals
        totalPromptTokens += promptTokens
        totalCandidateTokens += candidateTokens
        totalTokens += totalTokensUsed
        totalRequests++
        successfulRequests++

        // Update time-based stats
        todayTokens += totalTokensUsed
        todayRequests++
        weekTokens += totalTokensUsed
        weekRequests++
        monthTokens += totalTokensUsed
        monthRequests++

        // Update provider stats
        provider?.let { prov ->
            val ps = providerStats.getOrPut(prov) { ProviderStats() }
            ps.recordSuccess(promptTokens, candidateTokens, totalTokensUsed)
        }

        // Update model stats
        modelId?.let { id ->
            val ms = modelStats.getOrPut(id) { ModelStats(modelName, provider) }
            ms.recordUsage(totalTokensUsed)
        }

        // Add session entry
        val session = SessionEntry(
            provider = provider,
            modelId = modelId,
            modelName = modelName,
            promptTokens = promptTokens,
            responseTokens = candidateTokens,
            totalTokens = totalTokensUsed,
            success = true,
            durationMs = durationMs
        )
        addSession(session)
    }

    /**
     * Records a failed API call with comprehensive tracking.
     */
    @JvmOverloads
    fun recordFailure(
        provider: String? = null,
        modelId: String? = null,
        modelName: String? = null,
        error: String? = null,
        durationMs: Long = 0
    ) {
        checkAndResetTimePeriods()

        totalRequests++
        failedRequests++
        lastRequestTimestamp = System.currentTimeMillis()
        todayRequests++
        weekRequests++
        monthRequests++

        // Update provider stats
        provider?.let { prov ->
            val ps = providerStats.getOrPut(prov) { ProviderStats() }
            ps.recordFailure()
        }

        // Add session entry
        val session = SessionEntry.failure(provider, modelId, modelName, error, durationMs)
        addSession(session)
    }

    private fun addSession(session: SessionEntry) {
        recentSessions.add(0, session) // Add at beginning
        // Keep only last MAX_SESSIONS
        while (recentSessions.size > MAX_SESSIONS) {
            recentSessions.removeAt(recentSessions.size - 1)
        }
    }

    /**
     * Resets all statistics.
     */
    fun reset() {
        totalPromptTokens = 0
        totalCandidateTokens = 0
        totalTokens = 0
        totalRequests = 0
        successfulRequests = 0
        failedRequests = 0
        lastPromptTokens = 0
        lastCandidateTokens = 0
        lastTotalTokens = 0
        lastRequestTimestamp = 0

        todayTokens = 0
        todayRequests = 0
        weekTokens = 0
        weekRequests = 0
        monthTokens = 0
        monthRequests = 0
        lastResetDate = currentDateString
        lastResetWeek = currentWeek
        lastResetMonth = currentMonth

        providerStats = mutableMapOf()
        modelStats = mutableMapOf()
        recentSessions = mutableListOf()
        // Note: logs are NOT reset when resetting stats - use clearLogs() separately
    }

    // ==================== LOG METHODS ====================

    fun addLog(entry: LogEntry) {
        logs.add(0, entry) // Add at beginning (newest first)
        // Keep only last MAX_LOGS
        while (logs.size > MAX_LOGS) {
            logs.removeAt(logs.size - 1)
        }
    }

    fun logInfo(tag: String, message: String) = addLog(LogEntry.info(tag, message))

    fun logWarning(tag: String, message: String, details: String?) =
        addLog(LogEntry.warning(tag, message, details))

    fun logError(tag: String, message: String, details: String?) =
        addLog(LogEntry.error(tag, message, details))

    fun getLogs(): List<LogEntry> = logs.toList()

    fun clearLogs() = logs.clear()

    val errorCount: Int
        get() = logs.count { it.level == LogEntry.LEVEL_ERROR }

    // ==================== GETTERS ====================

    fun getTodayTokens(): Long {
        checkAndResetTimePeriods()
        return todayTokens
    }

    fun getTodayRequests(): Int {
        checkAndResetTimePeriods()
        return todayRequests
    }

    fun getWeekTokens(): Long {
        checkAndResetTimePeriods()
        return weekTokens
    }

    fun getWeekRequests(): Int {
        checkAndResetTimePeriods()
        return weekRequests
    }

    fun getMonthTokens(): Long {
        checkAndResetTimePeriods()
        return monthTokens
    }

    fun getMonthRequests(): Int {
        checkAndResetTimePeriods()
        return monthRequests
    }

    fun getProviderStats(): Map<String, ProviderStats> = providerStats.toMap()

    fun getProviderStats(provider: String): ProviderStats? = providerStats[provider]

    fun getModelStats(): Map<String, ModelStats> = modelStats.toMap()

    fun getRecentSessions(): List<SessionEntry> = recentSessions.toList()

    val successRate: Float
        get() = if (totalRequests == 0) 100.0f else successfulRequests.toFloat() / totalRequests * 100

    // ==================== CHART DATA METHODS ====================

    /**
     * Get sessions from the last N days for chart data.
     */
    fun getSessionsLastNDays(days: Int): List<SessionEntry> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return recentSessions.filter { it.timestamp >= cutoff }
    }

    /**
     * Get daily token aggregation for line charts.
     * Returns a map of date string to token count.
     */
    fun getTokensPerDay(days: Int): Map<String, Long> {
        val sdf = SimpleDateFormat("MM/dd", Locale.US)
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val filtered = recentSessions.filter { it.timestamp >= cutoff && it.success }
        
        return filtered.groupBy { sdf.format(Date(it.timestamp)) }
            .mapValues { (_, sessions) -> sessions.sumOf { it.totalTokens.toLong() } }
    }

    /**
     * Get daily prompt vs response token split for charts.
     */
    data class DailyTokenSplit(val promptTokens: Long, val responseTokens: Long)
    
    fun getTokenSplitPerDay(days: Int): Map<String, DailyTokenSplit> {
        val sdf = SimpleDateFormat("MM/dd", Locale.US)
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val filtered = recentSessions.filter { it.timestamp >= cutoff && it.success }
        
        return filtered.groupBy { sdf.format(Date(it.timestamp)) }
            .mapValues { (_, sessions) -> 
                DailyTokenSplit(
                    promptTokens = sessions.sumOf { it.promptTokens.toLong() },
                    responseTokens = sessions.sumOf { it.responseTokens.toLong() }
                )
            }
    }

    /**
     * Get success rate breakdown by provider for pie chart.
     */
    data class ProviderSuccessRate(
        val provider: String,
        val successful: Int,
        val failed: Int,
        val rate: Float
    )
    
    fun getSuccessRateByProvider(): List<ProviderSuccessRate> {
        return providerStats.map { (provider, stats) ->
            val total = stats.successfulRequests + stats.failedRequests
            val rate = if (total > 0) stats.successfulRequests.toFloat() / total * 100 else 0f
            ProviderSuccessRate(provider, stats.successfulRequests, stats.failedRequests, rate)
        }
    }

    /**
     * Get average response time per provider for bar chart.
     */
    data class ProviderResponseTime(
        val provider: String,
        val averageMs: Long,
        val minMs: Long,
        val maxMs: Long,
        val requestCount: Int
    )
    
    fun getAverageResponseTimeByProvider(): List<ProviderResponseTime> {
        val grouped = recentSessions
            .filter { it.success && it.durationMs > 0 && it.provider != null }
            .groupBy { it.provider ?: "Unknown" }
        
        return grouped.map { (provider, sessions) ->
            val durations = sessions.map { it.durationMs }
            ProviderResponseTime(
                provider = provider,
                averageMs = if (durations.isNotEmpty()) durations.average().toLong() else 0L,
                minMs = durations.minOrNull() ?: 0L,
                maxMs = durations.maxOrNull() ?: 0L,
                requestCount = sessions.size
            )
        }
    }

    /**
     * Get fastest and slowest generation for highlights.
     */
    fun getFastestGeneration(): SessionEntry? = 
        recentSessions.filter { it.success && it.durationMs > 0 }.minByOrNull { it.durationMs }
    
    fun getSlowestGeneration(): SessionEntry? = 
        recentSessions.filter { it.success && it.durationMs > 0 }.maxByOrNull { it.durationMs }

    /**
     * Get error timeline for chart.
     */
    fun getErrorTimeline(days: Int): List<SessionEntry> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return recentSessions.filter { !it.success && it.timestamp >= cutoff }
    }

    // ==================== SERIALIZATION ====================

    fun toJson(): String = Gson().toJson(this)

    val formattedSummary: String
        get() = String.format(
            Locale.US,
            "Total: %,d tokens (%,d prompt + %,d response)\n" +
                    "Requests: %d successful, %d failed (%.1f%% success)",
            totalTokens, totalPromptTokens, totalCandidateTokens,
            successfulRequests, failedRequests, successRate
        )

    companion object {
        private const val MAX_SESSIONS = 20
        private const val MAX_LOGS = 100

        private val currentDateString: String
            get() {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                return sdf.format(Date())
            }

        private val currentWeek: Int
            get() = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

        private val currentMonth: Int
            get() = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed

        @JvmStatic
        fun fromJson(json: String?): UsageStats {
            if (json.isNullOrEmpty()) return UsageStats()
            return try {
                val stats = Gson().fromJson(json, UsageStats::class.java)
                // Initialize null collections
                if (stats.providerStats == null) stats.providerStats = mutableMapOf()
                if (stats.modelStats == null) stats.modelStats = mutableMapOf()
                if (stats.recentSessions == null) stats.recentSessions = mutableListOf()
                if (stats.logs == null) stats.logs = mutableListOf()
                stats
            } catch (e: Exception) {
                UsageStats()
            }
        }
    }
}
