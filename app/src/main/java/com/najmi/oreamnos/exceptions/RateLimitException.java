package com.najmi.oreamnos.exceptions;

/**
 * Exception thrown when an AI provider returns a rate limit error (HTTP 429).
 * Contains retry delay information and provider identification for fallback
 * handling.
 */
public class RateLimitException extends Exception {

    private final long retryDelayMs;
    private final String providerName;

    /**
     * Creates a new RateLimitException.
     *
     * @param message      Error message
     * @param retryDelayMs Suggested retry delay in milliseconds (0 if unknown)
     * @param providerName Name of the provider that hit the rate limit
     */
    public RateLimitException(String message, long retryDelayMs, String providerName) {
        super(message);
        this.retryDelayMs = retryDelayMs;
        this.providerName = providerName;
    }

    /**
     * Gets the suggested retry delay in milliseconds.
     * 
     * @return Delay in milliseconds, or 0 if unknown
     */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Gets the name of the provider that hit the rate limit.
     * 
     * @return Provider name (e.g., "gemini", "groq", "openrouter")
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Gets a user-friendly wait time message.
     * 
     * @return Formatted wait time string
     */
    public String getWaitTimeMessage() {
        if (retryDelayMs > 0) {
            int waitSeconds = (int) (retryDelayMs / 1000);
            if (waitSeconds > 60) {
                int minutes = waitSeconds / 60;
                return minutes + " minute" + (minutes > 1 ? "s" : "");
            }
            return waitSeconds + " second" + (waitSeconds > 1 ? "s" : "");
        }
        return "a minute";
    }
}
