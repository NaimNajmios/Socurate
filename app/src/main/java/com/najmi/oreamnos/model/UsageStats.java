package com.najmi.oreamnos.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

/**
 * Comprehensive model class for storing API usage statistics.
 * Tracks token counts, request statistics, per-provider/model breakdown,
 * and time-based aggregates.
 */
public class UsageStats {

    // ==================== AGGREGATE STATS ====================

    // Cumulative token counts (all-time)
    private long totalPromptTokens;
    private long totalCandidateTokens;
    private long totalTokens;

    // Request counts (all-time)
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;

    // Last request stats
    private int lastPromptTokens;
    private int lastCandidateTokens;
    private int lastTotalTokens;
    private long lastRequestTimestamp;

    // ==================== TIME-BASED STATS ====================

    private long todayTokens;
    private int todayRequests;
    private long weekTokens;
    private int weekRequests;
    private long monthTokens;
    private int monthRequests;
    private String lastResetDate; // "2025-12-13" format
    private int lastResetWeek; // Week of year
    private int lastResetMonth; // Month (1-12)

    // ==================== PER-PROVIDER STATS ====================

    private Map<String, ProviderStats> providerStats;

    // ==================== PER-MODEL STATS ====================

    private Map<String, ModelStats> modelStats;

    // ==================== SESSION HISTORY ====================

    private List<SessionEntry> recentSessions;
    private static final int MAX_SESSIONS = 20;

    // ==================== NESTED CLASSES ====================

    /**
     * Statistics for a specific AI provider.
     */
    public static class ProviderStats {
        private long totalTokens;
        private long promptTokens;
        private long responseTokens;
        private int successfulRequests;
        private int failedRequests;

        public ProviderStats() {
            this.totalTokens = 0;
            this.promptTokens = 0;
            this.responseTokens = 0;
            this.successfulRequests = 0;
            this.failedRequests = 0;
        }

        public void recordSuccess(int prompt, int response, int total) {
            this.promptTokens += prompt;
            this.responseTokens += response;
            this.totalTokens += total;
            this.successfulRequests++;
        }

        public void recordFailure() {
            this.failedRequests++;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public long getPromptTokens() {
            return promptTokens;
        }

        public long getResponseTokens() {
            return responseTokens;
        }

        public int getSuccessfulRequests() {
            return successfulRequests;
        }

        public int getFailedRequests() {
            return failedRequests;
        }

        public int getTotalRequests() {
            return successfulRequests + failedRequests;
        }
    }

    /**
     * Statistics for a specific AI model.
     */
    public static class ModelStats {
        private String modelName; // Human-readable name
        private String provider; // gemini, groq, openrouter
        private long totalTokens;
        private int requests;

        public ModelStats() {
            this.totalTokens = 0;
            this.requests = 0;
        }

        public ModelStats(String modelName, String provider) {
            this();
            this.modelName = modelName;
            this.provider = provider;
        }

        public void recordUsage(int tokens) {
            this.totalTokens += tokens;
            this.requests++;
        }

        public String getModelName() {
            return modelName;
        }

        public String getProvider() {
            return provider;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public int getRequests() {
            return requests;
        }
    }

    /**
     * A single session/request entry for history tracking.
     */
    public static class SessionEntry {
        private long timestamp;
        private String provider;
        private String modelId;
        private String modelName;
        private int promptTokens;
        private int responseTokens;
        private int totalTokens;
        private boolean success;
        private String errorMessage;

        public SessionEntry() {
        }

        public SessionEntry(String provider, String modelId, String modelName,
                int prompt, int response, int total, boolean success) {
            this.timestamp = System.currentTimeMillis();
            this.provider = provider;
            this.modelId = modelId;
            this.modelName = modelName;
            this.promptTokens = prompt;
            this.responseTokens = response;
            this.totalTokens = total;
            this.success = success;
        }

        public static SessionEntry failure(String provider, String modelId, String modelName, String error) {
            SessionEntry entry = new SessionEntry();
            entry.timestamp = System.currentTimeMillis();
            entry.provider = provider;
            entry.modelId = modelId;
            entry.modelName = modelName;
            entry.success = false;
            entry.errorMessage = error;
            return entry;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getProvider() {
            return provider;
        }

        public String getModelId() {
            return modelId;
        }

        public String getModelName() {
            return modelName;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getResponseTokens() {
            return responseTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    /**
     * A log entry for tracking app/API events and errors.
     */
    public static class LogEntry {
        public static final String LEVEL_INFO = "INFO";
        public static final String LEVEL_WARNING = "WARN";
        public static final String LEVEL_ERROR = "ERROR";
        public static final String LEVEL_DEBUG = "DEBUG";

        private long timestamp;
        private String level;
        private String tag;
        private String message;
        private String details;

        public LogEntry() {
        }

        public LogEntry(String level, String tag, String message, String details) {
            this.timestamp = System.currentTimeMillis();
            this.level = level;
            this.tag = tag;
            this.message = message;
            this.details = details;
        }

        public static LogEntry info(String tag, String message) {
            return new LogEntry(LEVEL_INFO, tag, message, null);
        }

        public static LogEntry warning(String tag, String message, String details) {
            return new LogEntry(LEVEL_WARNING, tag, message, details);
        }

        public static LogEntry error(String tag, String message, String details) {
            return new LogEntry(LEVEL_ERROR, tag, message, details);
        }

        public static LogEntry debug(String tag, String message) {
            return new LogEntry(LEVEL_DEBUG, tag, message, null);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getLevel() {
            return level;
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }

        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    // ==================== LOG ENTRIES ====================

    private List<LogEntry> logs;
    private static final int MAX_LOGS = 100;

    // ==================== CONSTRUCTOR ====================

    public UsageStats() {
        // Initialize with zeros
        this.totalPromptTokens = 0;
        this.totalCandidateTokens = 0;
        this.totalTokens = 0;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
        this.lastPromptTokens = 0;
        this.lastCandidateTokens = 0;
        this.lastTotalTokens = 0;
        this.lastRequestTimestamp = 0;

        // Time-based
        this.todayTokens = 0;
        this.todayRequests = 0;
        this.weekTokens = 0;
        this.weekRequests = 0;
        this.monthTokens = 0;
        this.monthRequests = 0;
        this.lastResetDate = getCurrentDateString();
        this.lastResetWeek = getCurrentWeek();
        this.lastResetMonth = getCurrentMonth();

        // Per-provider/model
        this.providerStats = new HashMap<>();
        this.modelStats = new HashMap<>();
        this.recentSessions = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    // ==================== TIME HELPERS ====================

    private static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    private static int getCurrentWeek() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    private static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1; // 1-indexed
    }

    /**
     * Checks and resets time-based counters if needed.
     */
    public void checkAndResetTimePeriods() {
        String today = getCurrentDateString();
        int currentWeek = getCurrentWeek();
        int currentMonth = getCurrentMonth();

        // Reset daily stats if day changed
        if (!today.equals(lastResetDate)) {
            todayTokens = 0;
            todayRequests = 0;
            lastResetDate = today;
        }

        // Reset weekly stats if week changed
        if (currentWeek != lastResetWeek) {
            weekTokens = 0;
            weekRequests = 0;
            lastResetWeek = currentWeek;
        }

        // Reset monthly stats if month changed
        if (currentMonth != lastResetMonth) {
            monthTokens = 0;
            monthRequests = 0;
            lastResetMonth = currentMonth;
        }
    }

    // ==================== RECORDING METHODS ====================

    /**
     * Records a successful API call with comprehensive tracking.
     * 
     * @param promptTokens    Tokens used in prompt
     * @param candidateTokens Tokens in response
     * @param totalTokensUsed Total tokens used
     * @param provider        AI provider (gemini, groq, openrouter)
     * @param modelId         Model identifier
     * @param modelName       Human-readable model name
     */
    public void recordSuccess(int promptTokens, int candidateTokens, int totalTokensUsed,
            String provider, String modelId, String modelName) {
        // Check time period resets
        checkAndResetTimePeriods();

        // Update last request
        this.lastPromptTokens = promptTokens;
        this.lastCandidateTokens = candidateTokens;
        this.lastTotalTokens = totalTokensUsed;
        this.lastRequestTimestamp = System.currentTimeMillis();

        // Update all-time totals
        this.totalPromptTokens += promptTokens;
        this.totalCandidateTokens += candidateTokens;
        this.totalTokens += totalTokensUsed;
        this.totalRequests++;
        this.successfulRequests++;

        // Update time-based stats
        this.todayTokens += totalTokensUsed;
        this.todayRequests++;
        this.weekTokens += totalTokensUsed;
        this.weekRequests++;
        this.monthTokens += totalTokensUsed;
        this.monthRequests++;

        // Update provider stats
        if (provider != null) {
            ProviderStats ps = providerStats.get(provider);
            if (ps == null) {
                ps = new ProviderStats();
                providerStats.put(provider, ps);
            }
            ps.recordSuccess(promptTokens, candidateTokens, totalTokensUsed);
        }

        // Update model stats
        if (modelId != null) {
            ModelStats ms = modelStats.get(modelId);
            if (ms == null) {
                ms = new ModelStats(modelName, provider);
                modelStats.put(modelId, ms);
            }
            ms.recordUsage(totalTokensUsed);
        }

        // Add session entry
        SessionEntry session = new SessionEntry(provider, modelId, modelName,
                promptTokens, candidateTokens, totalTokensUsed, true);
        addSession(session);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public void recordSuccess(int promptTokens, int candidateTokens, int totalTokensUsed) {
        recordSuccess(promptTokens, candidateTokens, totalTokensUsed, null, null, null);
    }

    /**
     * Records a failed API call with comprehensive tracking.
     */
    public void recordFailure(String provider, String modelId, String modelName, String error) {
        checkAndResetTimePeriods();

        this.totalRequests++;
        this.failedRequests++;
        this.lastRequestTimestamp = System.currentTimeMillis();
        this.todayRequests++;
        this.weekRequests++;
        this.monthRequests++;

        // Update provider stats
        if (provider != null) {
            ProviderStats ps = providerStats.get(provider);
            if (ps == null) {
                ps = new ProviderStats();
                providerStats.put(provider, ps);
            }
            ps.recordFailure();
        }

        // Add session entry
        SessionEntry session = SessionEntry.failure(provider, modelId, modelName, error);
        addSession(session);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public void recordFailure() {
        recordFailure(null, null, null, null);
    }

    private void addSession(SessionEntry session) {
        if (recentSessions == null) {
            recentSessions = new ArrayList<>();
        }
        recentSessions.add(0, session); // Add at beginning
        // Keep only last MAX_SESSIONS
        while (recentSessions.size() > MAX_SESSIONS) {
            recentSessions.remove(recentSessions.size() - 1);
        }
    }

    /**
     * Resets all statistics.
     */
    public void reset() {
        this.totalPromptTokens = 0;
        this.totalCandidateTokens = 0;
        this.totalTokens = 0;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
        this.lastPromptTokens = 0;
        this.lastCandidateTokens = 0;
        this.lastTotalTokens = 0;
        this.lastRequestTimestamp = 0;

        this.todayTokens = 0;
        this.todayRequests = 0;
        this.weekTokens = 0;
        this.weekRequests = 0;
        this.monthTokens = 0;
        this.monthRequests = 0;
        this.lastResetDate = getCurrentDateString();
        this.lastResetWeek = getCurrentWeek();
        this.lastResetMonth = getCurrentMonth();

        this.providerStats = new HashMap<>();
        this.modelStats = new HashMap<>();
        this.recentSessions = new ArrayList<>();
        // Note: logs are NOT reset when resetting stats - use clearLogs() separately
    }

    // ==================== LOG METHODS ====================

    /**
     * Adds a log entry.
     */
    public void addLog(LogEntry entry) {
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(0, entry); // Add at beginning (newest first)
        // Keep only last MAX_LOGS
        while (logs.size() > MAX_LOGS) {
            logs.remove(logs.size() - 1);
        }
    }

    /**
     * Convenience method to add an info log.
     */
    public void logInfo(String tag, String message) {
        addLog(LogEntry.info(tag, message));
    }

    /**
     * Convenience method to add a warning log.
     */
    public void logWarning(String tag, String message, String details) {
        addLog(LogEntry.warning(tag, message, details));
    }

    /**
     * Convenience method to add an error log.
     */
    public void logError(String tag, String message, String details) {
        addLog(LogEntry.error(tag, message, details));
    }

    /**
     * Gets all log entries.
     */
    public List<LogEntry> getLogs() {
        return logs != null ? logs : new ArrayList<>();
    }

    /**
     * Clears all log entries.
     */
    public void clearLogs() {
        if (logs != null) {
            logs.clear();
        }
    }

    /**
     * Gets the number of error logs.
     */
    public int getErrorCount() {
        if (logs == null)
            return 0;
        int count = 0;
        for (LogEntry log : logs) {
            if (LogEntry.LEVEL_ERROR.equals(log.getLevel())) {
                count++;
            }
        }
        return count;
    }

    // ==================== GETTERS ====================

    // All-time stats
    public long getTotalPromptTokens() {
        return totalPromptTokens;
    }

    public long getTotalCandidateTokens() {
        return totalCandidateTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    // Last request stats
    public int getLastPromptTokens() {
        return lastPromptTokens;
    }

    public int getLastCandidateTokens() {
        return lastCandidateTokens;
    }

    public int getLastTotalTokens() {
        return lastTotalTokens;
    }

    public long getLastRequestTimestamp() {
        return lastRequestTimestamp;
    }

    // Time-based stats
    public long getTodayTokens() {
        checkAndResetTimePeriods();
        return todayTokens;
    }

    public int getTodayRequests() {
        checkAndResetTimePeriods();
        return todayRequests;
    }

    public long getWeekTokens() {
        checkAndResetTimePeriods();
        return weekTokens;
    }

    public int getWeekRequests() {
        checkAndResetTimePeriods();
        return weekRequests;
    }

    public long getMonthTokens() {
        checkAndResetTimePeriods();
        return monthTokens;
    }

    public int getMonthRequests() {
        checkAndResetTimePeriods();
        return monthRequests;
    }

    // Per-provider stats
    public Map<String, ProviderStats> getProviderStats() {
        return providerStats != null ? providerStats : new HashMap<>();
    }

    public ProviderStats getProviderStats(String provider) {
        if (providerStats == null)
            return null;
        return providerStats.get(provider);
    }

    // Per-model stats
    public Map<String, ModelStats> getModelStats() {
        return modelStats != null ? modelStats : new HashMap<>();
    }

    // Recent sessions
    public List<SessionEntry> getRecentSessions() {
        return recentSessions != null ? recentSessions : new ArrayList<>();
    }

    // Success rate
    public float getSuccessRate() {
        if (totalRequests == 0)
            return 100.0f;
        return (float) successfulRequests / totalRequests * 100;
    }

    // ==================== SERIALIZATION ====================

    /**
     * Converts to JSON for storage.
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Creates from JSON.
     */
    public static UsageStats fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new UsageStats();
        }
        try {
            UsageStats stats = new Gson().fromJson(json, UsageStats.class);
            // Initialize null collections
            if (stats.providerStats == null)
                stats.providerStats = new HashMap<>();
            if (stats.modelStats == null)
                stats.modelStats = new HashMap<>();
            if (stats.recentSessions == null)
                stats.recentSessions = new ArrayList<>();
            return stats;
        } catch (Exception e) {
            return new UsageStats();
        }
    }

    /**
     * Returns formatted summary string.
     */
    public String getFormattedSummary() {
        return String.format(Locale.US,
                "Total: %,d tokens (%,d prompt + %,d response)\n" +
                        "Requests: %d successful, %d failed (%.1f%% success)",
                totalTokens, totalPromptTokens, totalCandidateTokens,
                successfulRequests, failedRequests, getSuccessRate());
    }
}
