package com.najmi.oreamnos.curator

data class ConnectionResult(
    val success: Boolean,
    val message: String,
    val error: Throwable? = null
)

interface IConnectionTester {
    suspend fun testConnection(
        provider: String,
        apiKey: String,
        modelId: String,
        testMessage: String = "Test connection: Manchester United won 3-0."
    ): ConnectionResult
}
