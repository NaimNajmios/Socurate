package com.najmi.oreamnos.curator;

import android.util.Log;

import com.najmi.oreamnos.prompts.PromptManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.najmi.oreamnos.exceptions.RateLimitException;

/**
 * OpenAI-compatible API curator that works with both Groq and OpenRouter.
 * These providers use the same OpenAI chat completions format.
 * 
 * Supported providers:
 * - Groq: Fast inference with Llama 3.3
 * - OpenRouter: Access to multiple free models
 */
public class OpenAICompatibleCurator implements IContentCurator {

    private static final String TAG = "OpenAICompatibleCurator";
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;

    private final String apiKey;
    private final String baseUrl;
    private final String modelId;
    private final String tone;
    private final boolean isOpenRouter;
    private final PromptManager promptManager;

    // Token counts from last API call
    private int lastPromptTokens = 0;
    private int lastCandidateTokens = 0;
    private int lastTotalTokens = 0;

    /**
     * Creates an OpenAI-compatible curator.
     *
     * @param apiKey       API key for the provider
     * @param baseUrl      Base URL for the API (e.g.,
     *                     "https://api.groq.com/openai/v1/chat/completions")
     * @param modelId      Model ID to use (e.g., "llama-3.3-70b-versatile")
     * @param tone         Post tone preference ("formal" or "casual")
     * @param isOpenRouter Whether this is OpenRouter (requires special headers)
     */
    public OpenAICompatibleCurator(String apiKey, String baseUrl, String modelId,
            String tone, boolean isOpenRouter) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelId = modelId;
        this.tone = tone;
        this.isOpenRouter = isOpenRouter;
        this.promptManager = new PromptManager();
    }

    @Override
    public String curatePost(String inputText, boolean includeSource, boolean keepStructure) throws Exception {
        String systemPrompt = "You are a professional social media content writer for a Malaysian football club. " +
                "Write in Malaysian Malay (Bahasa Malaysia) only. Do not include hashtags.";
        String userPrompt = promptManager.buildInitialPrompt(tone, inputText, includeSource, keepStructure);

        return callApi(systemPrompt, userPrompt);
    }

    @Override
    public String refinePost(String originalPost, List<String> refinements, boolean includeSource) throws Exception {
        String systemPrompt = "You are refining a Malaysian Malay social media post about football. " +
                "Apply improvements while maintaining Bahasa Malaysia. Do not include hashtags.";
        String userPrompt = promptManager.buildRefinementPrompt(originalPost, refinements, includeSource);

        return callApi(systemPrompt, userPrompt);
    }

    /**
     * Makes the API call with retry logic.
     */
    private String callApi(String systemPrompt, String userPrompt) throws Exception {
        int retryCount = 0;
        int delayMs = INITIAL_RETRY_DELAY_MS;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                return executeRequest(systemPrompt, userPrompt);
            } catch (RateLimitException rle) {
                // Rate limit exceptions should be thrown immediately for fallback handling
                throw rle;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                Log.w(TAG, "API call failed (attempt " + retryCount + "/" + MAX_RETRIES + "): " + e.getMessage());

                if (retryCount >= MAX_RETRIES) {
                    throw e;
                }

                // Exponential backoff
                Thread.sleep(delayMs);
                delayMs *= 2;
            }
        }

        throw lastException != null ? lastException : new Exception("Max retries exceeded");
    }

    /**
     * Executes the HTTP request to the OpenAI-compatible API.
     */
    private String executeRequest(String systemPrompt, String userPrompt) throws Exception {
        URL url = new URL(baseUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            // OpenRouter requires additional headers
            if (isOpenRouter) {
                conn.setRequestProperty("HTTP-Referer", "https://github.com/socurate-app");
                conn.setRequestProperty("X-Title", "Socurate Football Content Curator");
            }

            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            // Build request body in OpenAI format
            JSONObject requestBody = buildRequestBody(systemPrompt, userPrompt);

            Log.d(TAG, "Sending request to: " + baseUrl);
            Log.d(TAG, "Model: " + modelId);

            // Write request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                return parseResponse(response.toString());
            } else {
                // Read error response
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }

                Log.e(TAG, "API Error: " + errorResponse);

                // Check for rate limit (429)
                if (responseCode == 429) {
                    String providerName = isOpenRouter ? "openrouter" : "groq";
                    throw new RateLimitException(
                            "Rate limit exceeded for " + providerName,
                            0, // OpenAI-compatible APIs don't always provide retry delay
                            providerName);
                }

                throw new Exception("API error (" + responseCode + "): " + parseErrorMessage(errorResponse.toString()));
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Builds the request body in OpenAI chat completions format.
     */
    private JSONObject buildRequestBody(String systemPrompt, String userPrompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", modelId);

        JSONArray messages = new JSONArray();

        // System message
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.put(systemMessage);

        // User message
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.put(userMessage);

        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);

        return body;
    }

    /**
     * Parses the OpenAI-format response.
     * Format: {"choices": [{"message": {"content": "..."}}], "usage": {...}}
     */
    private String parseResponse(String responseJson) throws Exception {
        JSONObject response = new JSONObject(responseJson);

        // Extract token usage
        if (response.has("usage")) {
            JSONObject usage = response.getJSONObject("usage");
            lastPromptTokens = usage.optInt("prompt_tokens", 0);
            lastCandidateTokens = usage.optInt("completion_tokens", 0);
            lastTotalTokens = usage.optInt("total_tokens", 0);
            Log.d(TAG, "Token usage - Prompt: " + lastPromptTokens +
                    ", Completion: " + lastCandidateTokens + ", Total: " + lastTotalTokens);
        }

        // Extract content
        JSONArray choices = response.getJSONArray("choices");
        if (choices.length() == 0) {
            throw new Exception("No choices in response");
        }

        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String content = message.getString("content");

        return content.trim();
    }

    /**
     * Parses error message from API error response.
     */
    private String parseErrorMessage(String errorJson) {
        try {
            JSONObject error = new JSONObject(errorJson);
            if (error.has("error")) {
                JSONObject errorObj = error.getJSONObject("error");
                return errorObj.optString("message", "Unknown error");
            }
            return errorJson;
        } catch (Exception e) {
            return errorJson;
        }
    }

    @Override
    public int getLastPromptTokens() {
        return lastPromptTokens;
    }

    @Override
    public int getLastCandidateTokens() {
        return lastCandidateTokens;
    }

    @Override
    public int getLastTotalTokens() {
        return lastTotalTokens;
    }
}
