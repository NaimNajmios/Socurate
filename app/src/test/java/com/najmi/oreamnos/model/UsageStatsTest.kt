package com.najmi.oreamnos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UsageStatsTest {

    @Test
    fun `getAverageResponseTimeByProvider handles sessions with null provider`() {
        val stats = UsageStats()

        // Add successful session with valid provider
        stats.recordSuccess(
            promptTokens = 10,
            candidateTokens = 10,
            totalTokensUsed = 20,
            provider = "Gemini",
            durationMs = 1000
        )

        // Add successful session with NULL provider
        // Note: recordSuccess accepts nullable provider
        stats.recordSuccess(
            promptTokens = 10,
            candidateTokens = 10,
            totalTokensUsed = 20,
            provider = null,
            durationMs = 2000
        )

        // Add failed session
        stats.recordFailure(
            provider = "Gemini",
            error = "Error",
            durationMs = 500
        )

        // The method under test filters out null providers internally (currently).
        // If we didn't filter, the `!!` would crash.
        // We want to ensure it doesn't crash now.
        val avgTimes = stats.getAverageResponseTimeByProvider()

        assertNotNull(avgTimes)
        assertEquals(1, avgTimes.size)
        assertEquals("Gemini", avgTimes[0].provider)
        // Only one successful session for Gemini (1000ms).
        // The failed one is filtered by success && duration > 0 check?
        // UsageStats.kt: .filter { it.success && it.durationMs > 0 && it.provider != null }
        // So failed request (success=false) is excluded.
        // Null provider session (success=true) is excluded.

        assertEquals(1000L, avgTimes[0].averageMs)
    }

    @Test
    fun `verify defensive coding allows null provider in recordSuccess`() {
        val stats = UsageStats()
        // Should not throw exception
        stats.recordSuccess(10, 10, 20, null, null, null, 100)

        val sessions = stats.getRecentSessions()
        assertEquals(1, sessions.size)
        assertEquals(null, sessions[0].provider)
    }
}
