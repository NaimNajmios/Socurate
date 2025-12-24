package com.najmi.oreamnos.exceptions

/**
 * Exception thrown when an AI provider returns a rate limit error (HTTP 429).
 * Contains retry delay information and provider identification for fallback handling.
 */
class RateLimitException(
    message: String,
    val retryDelayMs: Long,
    val providerName: String
) : Exception(message) {

    /**
     * Gets a user-friendly wait time message.
     */
    val waitTimeMessage: String
        get() {
            return if (retryDelayMs > 0) {
                val waitSeconds = (retryDelayMs / 1000).toInt()
                if (waitSeconds > 60) {
                    val minutes = waitSeconds / 60
                    "$minutes minute${if (minutes > 1) "s" else ""}"
                } else {
                    "$waitSeconds second${if (waitSeconds > 1) "s" else ""}"
                }
            } else {
                "a minute"
            }
        }
}
