package com.najmi.oreamnos.utils

import com.najmi.oreamnos.model.UsageStats

interface IPreferencesManager {
    fun getTheme(): String
    fun saveTheme(theme: String)
    fun getProvider(): String
    fun saveProvider(provider: String)
    fun getApiKey(): String?
    fun saveApiKey(apiKey: String): Boolean
    fun getGroqApiKey(): String?
    fun saveGroqApiKey(apiKey: String): Boolean
    fun getOpenRouterApiKey(): String?
    fun saveOpenRouterApiKey(apiKey: String): Boolean
    fun getCerebrasApiKey(): String?
    fun saveCerebrasApiKey(apiKey: String): Boolean
    fun getHfToken(): String?
    fun saveHfToken(token: String): Boolean
    fun getModelForProvider(provider: String): String
    fun saveModelForProvider(provider: String, modelId: String)
    fun areHashtagsEnabled(): Boolean
    fun setHashtagsEnabled(enabled: Boolean)
    fun isSourceEnabled(): Boolean
    fun saveSourceEnabled(enabled: Boolean)
    fun getTextSize(): Int
    fun saveTextSize(size: Int)
    fun getUsageStats(): UsageStats
    fun resetUsageStats()
    fun clearLogs()
}
