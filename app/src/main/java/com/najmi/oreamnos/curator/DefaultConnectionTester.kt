package com.najmi.oreamnos.curator

import com.najmi.oreamnos.services.GeminiService
import com.najmi.oreamnos.utils.PreferencesManager

class DefaultConnectionTester : IConnectionTester {
    override suspend fun testConnection(
        provider: String,
        apiKey: String,
        modelId: String,
        testMessage: String
    ): ConnectionResult {
        return try {
            when (provider) {
                PreferencesManager.PROVIDER_GROQ -> {
                    val curator = OpenAICompatibleCurator(
                        apiKey,
                        "https://api.groq.com/openai/v1/chat/completions",
                        modelId,
                        false
                    )
                    curator.curatePost(testMessage, true, false)
                }
                PreferencesManager.PROVIDER_OPENROUTER -> {
                    val curator = OpenAICompatibleCurator(
                        apiKey,
                        "https://openrouter.ai/api/v1/chat/completions",
                        modelId,
                        true
                    )
                    curator.curatePost(testMessage, true, false)
                }
                PreferencesManager.PROVIDER_CEREBRAS -> {
                    val curator = OpenAICompatibleCurator(
                        apiKey,
                        "https://api.cerebras.ai/v1/chat/completions",
                        modelId,
                        false
                    )
                    curator.curatePost(testMessage, true, false)
                }
                else -> {
                    val gemini = GeminiService(apiKey, modelId)
                    gemini.curatePost(testMessage, true, false)
                }
            }
            ConnectionResult(success = true, message = "Connection successful")
        } catch (e: Exception) {
            ConnectionResult(success = false, message = e.message ?: "Unknown error", error = e)
        }
    }
}
