package com.najmi.oreamnos.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Model class for storing Gemini API usage statistics.
 * Tracks token counts and request statistics.
 */
public class UsageStats {

    // Cumulative token counts
    private long totalPromptTokens;
    private long totalCandidateTokens;
    private long totalTokens;

    // Request counts
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;

    // Last request stats
    private int lastPromptTokens;
    private int lastCandidateTokens;
    private int lastTotalTokens;
    private long lastRequestTimestamp;

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
    }

    /**
     * Records a successful API call with token usage.
     */
    public void recordSuccess(int promptTokens, int candidateTokens, int totalTokensUsed) {
        this.lastPromptTokens = promptTokens;
        this.lastCandidateTokens = candidateTokens;
        this.lastTotalTokens = totalTokensUsed;
        this.lastRequestTimestamp = System.currentTimeMillis();

        this.totalPromptTokens += promptTokens;
        this.totalCandidateTokens += candidateTokens;
        this.totalTokens += totalTokensUsed;
        this.totalRequests++;
        this.successfulRequests++;
    }

    /**
     * Records a failed API call.
     */
    public void recordFailure() {
        this.totalRequests++;
        this.failedRequests++;
        this.lastRequestTimestamp = System.currentTimeMillis();
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
    }

    // Getters
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
            return new Gson().fromJson(json, UsageStats.class);
        } catch (Exception e) {
            return new UsageStats();
        }
    }

    /**
     * Returns formatted summary string.
     */
    public String getFormattedSummary() {
        return String.format(
                "Total: %,d tokens (%,d prompt + %,d response)\n" +
                        "Requests: %d successful, %d failed",
                totalTokens, totalPromptTokens, totalCandidateTokens,
                successfulRequests, failedRequests);
    }
}
