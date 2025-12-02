package com.mycompany.oreamnos.services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Handles communication with the Google Gemini API for content curation.
 * Ported from the original web application with Android-specific optimizations.
 * Features retry mechanism with exponential backoff and response cleaning.
 */
public class GeminiService {

    private static final String TAG = "GeminiService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Retry configuration
    private static final int MAX_RETRIES = 4;
    private static final long BASE_DELAY_MS = 500L;
    private static final long MAX_DELAY_MS = 60000L; // Increased to 60 seconds for rate limits
    private static final long RATE_LIMIT_FALLBACK_DELAY_MS = 30000L; // 30 seconds if can't parse

    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String endpoint;
    private final String tone; // "formal" or "casual"

    /**
     * Creates a new GeminiService instance.
     * 
     * @param apiKey   Gemini API key
     * @param endpoint API endpoint URL
     * @param tone     Post tone ("formal" or "casual")
     */
    public GeminiService(String apiKey, String endpoint, String tone) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.tone = tone != null ? tone : "formal";
        this.gson = new Gson();

        // Configure OkHttp client with timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Curates the input text into a football social media post in Malaysian Malay.
     * Implements retry logic with exponential backoff for transient errors.
     * 
     * @param inputText The text to curate
     * @return The curated post
     * @throws Exception if API call fails after retries
     */
    public String curatePost(String inputText) throws Exception {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        Log.i(TAG, "=== GEMINI API CALL START [" + requestId + "] ===");
        Log.i(TAG, "[" + requestId + "] Input text length: " + (inputText != null ? inputText.length() : 0));

        // Validate inputs
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("Invalid or missing Gemini API key");
        }
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new Exception("Invalid Gemini API endpoint");
        }
        if (inputText == null || inputText.trim().isEmpty()) {
            throw new Exception("Input text is required");
        }

        // Build the prompt based on tone
        String prompt = buildPrompt(tone);
        String sanitizedInput = inputText.replace("\"", "'");

        // Build request JSON
        JsonObject requestJson = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt + "\n\n" + sanitizedInput);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestJson.add("contents", contents);

        String requestBodyString = gson.toJson(requestJson);
        Log.d(TAG, "[" + requestId + "] Request body length: " + requestBodyString.length());

        // Retry loop
        String rawResult = null;
        Exception lastException = null;
        Random rnd = new Random();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Log.i(TAG, "[" + requestId + "] Gemini attempt " + attempt + "/" + MAX_RETRIES);

                // Build request with API key
                String urlWithKey = endpoint + "?key=" + apiKey;
                RequestBody body = RequestBody.create(requestBodyString, JSON);
                Request request = new Request.Builder()
                        .url(urlWithKey)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                long connectionStart = System.currentTimeMillis();
                Response response = client.newCall(request).execute();
                long connectionEnd = System.currentTimeMillis();

                int code = response.code();
                Log.i(TAG, "[" + requestId + "] Response code: " + code +
                        " (time: " + (connectionEnd - connectionStart) + "ms) on attempt " + attempt);

                if (code >= 400) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    response.close();

                    // Check if transient error (retry)
                    if (code == 503 || code == 429 || (code >= 500 && code < 600)) {
                        String errorType = code == 429 ? "Rate limit (quota)" : "Server error";
                        Log.w(TAG, "[" + requestId + "] " + errorType + " " + code +
                                " - will retry (attempt " + attempt + ")");

                        // For 429, parse retry delay from API response
                        long apiSuggestedDelay = 0;
                        if (code == 429) {
                            apiSuggestedDelay = parseRetryDelay(errorBody, requestId);
                            if (apiSuggestedDelay > 0) {
                                Log.i(TAG, "[" + requestId + "] API requests wait of " + apiSuggestedDelay + "ms");
                            } else {
                                Log.w(TAG, "[" + requestId + "] Could not parse retry delay, using default backoff");
                            }
                        }

                        lastException = new RateLimitException(
                                "Gemini " + errorType.toLowerCase() + ": " + code + ". " + errorBody,
                                apiSuggestedDelay);
                        } else {
                        // Permanent error
                        Log.e(TAG, "[" + requestId + "] Permanent error: " + code + " - " + errorBody);
                        throw new Exception("Gemini API error: " + code + ". " + errorBody);
                    }
                } else {
                    // Success
                    rawResult = response.body().string();
                    response.close();
                    Log.d(TAG, "[" + requestId + "] Raw response length: " + rawResult.length());
                    lastException = null;
                    break;
                }
            } catch (IOException ioe) {
                Log.w(TAG, "[" + requestId + "] Network error on attempt " + attempt + ": " + ioe.getMessage());
                lastException = ioe;
            } catch (Exception e) {
                Log.e(TAG, "[" + requestId + "] Error on attempt " + attempt + ": " + e.getMessage());
                lastException = e;
                // If it's not a transient error, don't retry
                if (!(e instanceof IOException)) {
                    throw e;
                }
            }

            // Exponential backoff if not last attempt
            if (attempt < MAX_RETRIES) {
                long delay;

                // If we have API-suggested delay from rate limit, use it
                if (lastException instanceof RateLimitException) {
                    RateLimitException rle = (RateLimitException) lastException;
                    long apiDelay = rle.getRetryDelayMs();

                    if (apiDelay > 0) {
                        // Respect API's requested delay
                        delay = Math.min(MAX_DELAY_MS, apiDelay);
                        Log.i(TAG, "[" + requestId + "] Using API-suggested delay: " + delay + "ms");
                    } else {
                        // Fallback for rate limits
                        delay = RATE_LIMIT_FALLBACK_DELAY_MS;
                        Log.i(TAG, "[" + requestId + "] Using fallback delay for rate limit: " + delay + "ms");
                    }
                } else {
                    // Standard exponential backoff for other errors
                    delay = Math.min(MAX_DELAY_MS, BASE_DELAY_MS * (1L << (attempt - 1)));
                    long jitter = (long) (rnd.nextDouble() * 500L);
                    delay += jitter;
                }

                Log.i(TAG, "[" + requestId + "] Sleeping " + delay + "ms before retry");
                Thread.sleep(delay);
            }
        }

        // Check if we got a result
        if (rawResult == null) {
            long totalTime = System.currentTimeMillis() - startTime;
            Log.i(TAG, "[" + requestId + "] API exhausted retries after " + totalTime + "ms");

            if (lastException != null) {
                // Provide user-friendly message for rate limits
                if (lastException instanceof RateLimitException) {
                    RateLimitException rle = (RateLimitException) lastException;
                    String userMsg = "Rate limit exceeded. ";

                    if (rle.getRetryDelayMs() > 0) {
                        int waitSeconds = (int) (rle.getRetryDelayMs() / 1000);
                        userMsg += "Please wait " + waitSeconds + " seconds and try again. ";
                    } else {
                        userMsg += "Please wait a minute and try again. ";
                    }

                    userMsg += "Tip: Use 'gemini-1.5-flash' model for better quotas.";
                    throw new Exception(userMsg, lastException);
                }

                throw new Exception("Gemini API failed after retries: " + lastException.getMessage(), lastException);
            } else {
                throw new Exception("Gemini API returned no result after retries");
            }
        }

        // Parse response
        String curatedText;
        try {
            JsonObject root = gson.fromJson(rawResult, JsonObject.class);
            curatedText = extractTextFromJson(root);

            if (curatedText == null || curatedText.trim().isEmpty()) {
                Log.w(TAG, "[" + requestId + "] Extracted text is empty");
                curatedText = "Gagal mendapatkan hasil dari Gemini.";
            } else {
                curatedText = cleanUpResponse(curatedText);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            Log.i(TAG, "[" + requestId + "] Success! Output: " + curatedText.length() +
                    " chars (total time: " + totalTime + "ms)");
            Log.i(TAG, "=== GEMINI API CALL END [" + requestId + "] ===");

            return curatedText;
        } catch (Exception e) {
            Log.e(TAG, "[" + requestId + "] Error parsing response", e);
            return "Gagal mendapatkan hasil dari Gemini.";
        }
    }

    /**
     * Builds the prompt based on the selected tone.
     */
    private String buildPrompt(String tone) {
        String baseTone = "formal".equals(tone)
                ? "Use a polished and respectful tone appropriate for official club communications or high-quality social media content."
                : "Use an engaging, conversational tone suitable for fan communities and social media interaction while remaining respectful.";

        return "Rewrite the following text into a " +
                ("formal".equals(tone) ? "formal, professional" : "engaging, conversational") +
                " social media post written in Malaysian Malay (Bahasa Malaysia). " + baseTone +
                " Be length-aware: preserve the key information and nuance from the source and produce a substantive post — "
                +
                "target approximately 40–60% of the original article's character length while ensuring a minimum of 200 characters "
                +
                "and a maximum of 800 characters. If the input article is shorter than 200 characters, produce a concise post "
                +
                "that fully conveys the content. Keep the message clear, well-structured, and engaging; include suitable football "
                +
                "terminology where relevant (e.g., 'hat-trick', 'clean sheet' → 'rekod bersih'). " +
                "Do not add explanations, notes, headings, metadata, or any commentary. Do not include personal phrases such as "
                +
                "'Saya cuba' or 'Saya juga'. Do not use the em dash character (—). Provide only the final ready-to-post content.";
    }

    /**
     * Extracts the curated text from Gemini's JSON response.
     * Tries multiple common locations in the JSON structure.
     */
    private String extractTextFromJson(JsonObject root) {
        if (root == null)
            return null;

        try {
            // Try: candidates[0].content.parts[0].text
            if (root.has("candidates")) {
                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    if (firstCandidate.has("content")) {
                        JsonObject content = firstCandidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject firstPart = parts.get(0).getAsJsonObject();
                                if (firstPart.has("text")) {
                                    String text = firstPart.get("text").getAsString();
                                    if (text != null && !text.trim().isEmpty()) {
                                        return text;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fallback: try to find any "text" field
            return findFirstTextField(root);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting text from JSON", e);
            return null;
        }
    }

    /**
     * Recursively searches for the first "text" field in JSON.
     */
    private String findFirstTextField(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (String key : obj.keySet()) {
                if ("text".equalsIgnoreCase(key) && obj.get(key).isJsonPrimitive()) {
                    String text = obj.get(key).getAsString();
                    if (text != null && !text.trim().isEmpty()) {
                        return text;
                    }
                }
                String deeper = findFirstTextField(obj.get(key));
                if (deeper != null && !deeper.trim().isEmpty()) {
                    return deeper;
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            for (JsonElement item : arr) {
                String deeper = findFirstTextField(item);
                if (deeper != null && !deeper.trim().isEmpty()) {
                    return deeper;
                }
            }
        }

        return null;
    }

    /**
     * Cleans up the generated response by removing unwanted phrases.
     */
    private String cleanUpResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }

        String cleaned = response.trim();

        // Remove unwanted explanatory phrases
        String[] unwantedPhrases = {
                "Okay, ini percubaan untuk mengubah teks tersebut",
                "terjemahkan ke Bahasa Melayu (Malaysia)",
                "suntikkan sedikit gaya yang kurang formal",
                "istilah bola sepak Inggeris yang biasa",
                "Saya cuba gunakan perkataan yang lebih santai",
                "Saya juga masukkan istilah bola sepak",
                "Struktur diubah dengan menggabungkan",
                "Em dash (—) dibuang seperti yang diminta",
                "Tukar perkataan dari bahasa inggeris",
                "Semoga ini membantu",
                "Saya cuba",
                "Saya juga",
                "Struktur diubah",
                "Em dash",
                "Tukar perkataan",
                "Semoga ini"
        };

        for (String phrase : unwantedPhrases) {
            cleaned = cleaned.replace(phrase, "");
        }

        // Remove text between asterisks (explanatory notes)
        cleaned = cleaned.replaceAll("\\*.*?\\*", "");

        // Clean up spacing
        cleaned = cleaned.replaceAll("\\n\\s*\\n\\s*\\n+", "\n\n");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.replaceAll("\\n\\s+", "\n");
        cleaned = cleaned.trim();

        // If too short after cleaning, return original
        if (cleaned.length() < 50) {
            Log.w(TAG, "Response too short after cleaning, returning original");
            return response;
        }

        return cleaned;
    }

    /**
     * Parses the retryDelay from Gemini API 429 error response.
     * Looks for "retryDelay" field in the details section.
     * 
     * @param errorBody JSON error response from API
     * @param requestId Request ID for logging
     * @return Delay in milliseconds, or 0 if not found
     */
    private long parseRetryDelay(String errorBody, String requestId) {
        try {
            if (errorBody == null || errorBody.trim().isEmpty()) {
                return 0;
            }

            JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
            if (errorJson == null || !errorJson.has("error")) {
                return 0;
            }

            JsonObject error = errorJson.getAsJsonObject("error");
            if (!error.has("details")) {
                return 0;
            }

            JsonArray details = error.getAsJsonArray("details");
            for (JsonElement detail : details) {
                JsonObject detailObj = detail.getAsJsonObject();

                // Look for RetryInfo type
                if (detailObj.has("@type") &&
                        detailObj.get("@type").getAsString().contains("RetryInfo")) {

                    if (detailObj.has("retryDelay")) {
                        String retryDelayStr = detailObj.get("retryDelay").getAsString();
                        // Parse format like "46s" or "46.799s"
                        return parseRetryDelayString(retryDelayStr);
                    }
                }
            }

            return 0;
        } catch (Exception e) {
            Log.w(TAG, "[" + requestId + "] Error parsing retry delay: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Parses retry delay string like "46s" or "46.799675533s" to milliseconds.
     */
    private long parseRetryDelayString(String delayStr) {
        if (delayStr == null || delayStr.isEmpty()) {
            return 0;
        }

        try {
            // Remove 's' suffix and parse as decimal seconds
            String secondsStr = delayStr.replaceAll("[^0-9.]", "");
            double seconds = Double.parseDouble(secondsStr);
            return (long) (seconds * 1000);
        } catch (Exception e) {
            Log.w(TAG, "Could not parse delay string: " + delayStr);
            return 0;
        }
    }

    /**
     * Custom exception for rate limit errors that includes the retry delay.
     */
    private static class RateLimitException extends Exception {
        private final long retryDelayMs;

        public RateLimitException(String message, long retryDelayMs) {
            super(message);
            this.retryDelayMs = retryDelayMs;
        }

        public long getRetryDelayMs() {
            return retryDelayMs;
        }
    }
}
