package com.oreamnos.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final int MAX_RETRIES = 5;
    private static final int INITIAL_BACKOFF_MS = 1000;
    private static final int MAX_BACKOFF_MS = 32000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    private final String apiKey;
    private final Gson gson;

    public GeminiService() {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        this.gson = new Gson();
    }

    public String transformContent(String originalText) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set");
        }

        String prompt = buildPrompt(originalText);
        return executeWithRetry(prompt);
    }

    private String buildPrompt(String originalText) {
        int originalLength = originalText.length();
        int targetMinLength = (int) (originalLength * 0.4);
        int targetMaxLength = (int) (originalLength * 0.6);

        return String.format("""
            You are a professional social media content writer for a Malaysian football club. Your task is to transform the following English football news article into a formal, professional social media post written in Malaysian Malay (Bahasa Malaysia).

            STRICT REQUIREMENTS:
            1. Write ONLY in Bahasa Malaysia (Malaysian Malay) - do not include any English text in your output
            2. Maintain a formal, professional tone suitable for official club communication
            3. The output must be approximately 40-60%% of the original content length (target: %d-%d characters)
            4. FORBIDDEN: Do not use personal commentary phrases like "Saya cuba", "Saya rasa", "Pada pendapat saya"
            5. FORBIDDEN: Do not use em-dashes (—) anywhere in the output
            6. FORBIDDEN: Do not include hashtags unless specifically relevant to the content
            7. Format the post for social media readability (short paragraphs, clear structure)
            8. Preserve key facts, names, dates, and statistics from the original
            9. Make the content engaging but maintain journalistic objectivity
            10. The tone should be that of an official club announcement or news update

            ORIGINAL ENGLISH TEXT:
            ---
            %s
            ---

            Provide ONLY the Bahasa Malaysia social media post. Do not include any explanations, notes, or English text.
            """, targetMinLength, targetMaxLength, originalText);
    }

    private String executeWithRetry(String prompt) throws Exception {
        int retryCount = 0;
        int backoffMs = INITIAL_BACKOFF_MS;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                return callGeminiApi(prompt);
            } catch (IOException e) {
                lastException = e;
                String errorMessage = e.getMessage().toLowerCase();
                
                if (errorMessage.contains("401") || errorMessage.contains("403") || 
                    errorMessage.contains("invalid") || errorMessage.contains("unauthorized")) {
                    throw e;
                }

                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    System.out.printf("Gemini API call failed (attempt %d/%d), retrying in %dms: %s%n", 
                                     retryCount, MAX_RETRIES, backoffMs, e.getMessage());
                    Thread.sleep(backoffMs);
                    backoffMs = (int) Math.min(backoffMs * BACKOFF_MULTIPLIER, MAX_BACKOFF_MS);
                }
            }
        }

        throw new IOException("Failed after " + MAX_RETRIES + " retries: " + 
                             (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private String callGeminiApi(String prompt) throws IOException {
        URL url = new URL(GEMINI_API_URL + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("maxOutputTokens", 2048);
            requestBody.add("generationConfig", generationConfig);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                String errorBody = readStream(conn.getErrorStream());
                throw new IOException("API error " + responseCode + ": " + errorBody);
            }

            String responseBody = readStream(conn.getInputStream());
            return parseResponse(responseBody);
            
        } finally {
            conn.disconnect();
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            
            if (response.has("candidates")) {
                JsonArray candidates = response.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                return parts.get(0).getAsJsonObject().get("text").getAsString().trim();
                            }
                        }
                    }
                }
            }
            
            throw new IOException("Unexpected response format: " + responseBody);
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini response: " + e.getMessage());
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
