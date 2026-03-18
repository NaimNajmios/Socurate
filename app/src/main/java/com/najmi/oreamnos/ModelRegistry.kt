package com.najmi.oreamnos

import com.najmi.oreamnos.utils.PreferencesManager

private data class ModelConfig(val displayName: String, val id: String)

private object GeminiModels {
    val MODELS = listOf(
        ModelConfig("Gemini 3.1 Pro", "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro:generateContent"),
        ModelConfig("Gemini 3 Flash", "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash:generateContent"),
        ModelConfig("Gemini 3.1 Flash Lite", "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent"),
        ModelConfig("Gemini 2.5 Pro", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent"),
        ModelConfig("Gemini 2.5 Flash", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"),
        ModelConfig("Gemini 2.5 Flash Lite", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent")
    )
    val modelNames = MODELS.map { it.displayName }
    val modelIds = MODELS.map { it.id }
}

private object GroqModels {
    val MODELS = listOf(
        ModelConfig("DeepSeek R1 Distill Llama 70B", "deepseek-r1-distill-llama-70b"),
        ModelConfig("Llama 3.3 70B Versatile", "llama-3.3-70b-versatile"),
        ModelConfig("Llama 3.1 8B Instant", "llama-3.1-8b-instant"),
        ModelConfig("Qwen QwQ 32B", "qwen-qwq-32b"),
        ModelConfig("GPT OSS 120B", "openai/gpt-oss-120b")
    )
    val modelNames = MODELS.map { it.displayName }
    val modelIds = MODELS.map { it.id }
}

private object OpenRouterModels {
    val MODELS = listOf(
        ModelConfig("DeepSeek R1 Zero", "deepseek/deepseek-r1-zero:free"),
        ModelConfig("DeepSeek V3 Base", "deepseek/deepseek-v3-base:free"),
        ModelConfig("Llama 4 Maverick", "meta-llama/llama-4-maverick:free"),
        ModelConfig("Gemini 2.5 Pro Exp", "google/gemini-2.5-pro-exp-03-25:free"),
        ModelConfig("GPT OSS 120B", "openai/gpt-oss-120b:free"),
        ModelConfig("Llama 3.3 70B Instruct", "meta-llama/llama-3.3-70b-instruct:free")
    )
    val modelNames = MODELS.map { it.displayName }
    val modelIds = MODELS.map { it.id }
}

private object CerebrasModels {
    val MODELS = listOf(
        ModelConfig("GPT-5.3 Codex Spark", "gpt-5.3-codex-spark"),
        ModelConfig("Llama 3.3 70B", "llama-3.3-70b"),
        ModelConfig("Llama 3.1 8B", "llama3.1-8b"),
        ModelConfig("Z.ai GLM 4.7", "zai-glm-4.7")
    )
    val modelNames = MODELS.map { it.displayName }
    val modelIds = MODELS.map { it.id }
}

object ModelRegistry {
    fun getModelNamesForProvider(provider: String): List<String> = when (provider) {
        PreferencesManager.PROVIDER_GROQ -> GroqModels.modelNames
        PreferencesManager.PROVIDER_OPENROUTER -> OpenRouterModels.modelNames
        PreferencesManager.PROVIDER_CEREBRAS -> CerebrasModels.modelNames
        else -> GeminiModels.modelNames
    }
    
    fun getModelIdsForProvider(provider: String): List<String> = when (provider) {
        PreferencesManager.PROVIDER_GROQ -> GroqModels.modelIds
        PreferencesManager.PROVIDER_OPENROUTER -> OpenRouterModels.modelIds
        PreferencesManager.PROVIDER_CEREBRAS -> CerebrasModels.modelIds
        else -> GeminiModels.modelIds
    }
    
    val PROVIDER_NAMES = listOf("Gemini", "Groq", "OpenRouter", "Cerebras")
    val PROVIDER_VALUES = listOf("gemini", "groq", "openrouter", "cerebras")
}
