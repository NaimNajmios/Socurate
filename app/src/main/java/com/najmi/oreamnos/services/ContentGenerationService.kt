package com.najmi.oreamnos.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.najmi.oreamnos.R
import com.najmi.oreamnos.curator.CuratorFactory
import com.najmi.oreamnos.exceptions.RateLimitException
import com.najmi.oreamnos.utils.NotificationHelper
import com.najmi.oreamnos.utils.PreferencesManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Foreground Service for generating AI content in the background.
 * This service continues running even when the app is minimized.
 */
class ContentGenerationService : Service() {

    private lateinit var executor: ExecutorService
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        executor = Executors.newSingleThreadExecutor()
        notificationHelper = NotificationHelper(this)
        prefsManager = PreferencesManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started with action: ${intent?.action ?: "null"}")

        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action
        if (action == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start foreground immediately
        startForeground(
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            notificationHelper.buildForegroundNotification(
                getString(R.string.notification_generating_title),
                getString(R.string.notification_generating_message)
            )
        )

        when (action) {
            ACTION_GENERATE -> handleGenerate(intent, startId)
            ACTION_REFINE -> handleRefine(intent, startId)
            else -> {
                Log.w(TAG, "Unknown action: $action")
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Handles content generation request.
     */
    private fun handleGenerate(intent: Intent, startId: Int) {
        val inputText = intent.getStringExtra(EXTRA_INPUT_TEXT)
        val includeSource = intent.getBooleanExtra(EXTRA_INCLUDE_SOURCE, false)
        val keepStructure = intent.getBooleanExtra(EXTRA_KEEP_STRUCTURE, false)

        executor.execute {
            // Validation moved inside executor to prevent race conditions with stopSelf
            // This ensures validation failures don't kill the service while previous tasks are running
            if (inputText.isNullOrEmpty()) {
                broadcastError("Input text is required", false)
                stopSelf(startId)
                return@execute
            }

            val startTime = System.currentTimeMillis()
            try {
                Log.i(TAG, "Starting content generation...")
                var content = inputText

                // Check if input is a URL
                if (WebContentExtractor.isUrl(inputText)) {
                    Log.i(TAG, "Input is URL, extracting content...")
                    val extractor = WebContentExtractor()
                    content = extractor.extractContent(inputText)
                }

                // Get provider name for logging
                val provider = prefsManager.getProvider()
                val providerDisplay = CuratorFactory.getProviderDisplayName(provider)

                // Log the API request start
                prefsManager.logInfo("API", "Request started via $providerDisplay")

                // Generate post using curator abstraction
                val curator = CuratorFactory.create(this@ContentGenerationService)
                val result = curator.curatePost(content, includeSource, keepStructure)

                // Calculate duration
                val durationMs = System.currentTimeMillis() - startTime

                // Record token usage with duration
                val promptTokens = curator.lastPromptTokens
                val candidateTokens = curator.lastCandidateTokens
                val totalTokens = curator.lastTotalTokens
                prefsManager.recordApiSuccess(promptTokens, candidateTokens, totalTokens, durationMs)

                // Log success
                prefsManager.logInfo("API", "Request successful via $providerDisplay ($totalTokens tokens, ${durationMs}ms)")

                Log.i(TAG, "Content generation successful")
                broadcastSuccess(result, false)

            } catch (rle: RateLimitException) {
                Log.w(TAG, "Rate limit hit: ${rle.message}")
                val durationMs = System.currentTimeMillis() - startTime
                prefsManager.recordApiFailure(durationMs)

                // Log rate limit error
                val providerName = rle.providerName ?: "Unknown"
                val delayMs = rle.retryDelayMs
                val delayInfo = if (delayMs > 0) " (retry in ${delayMs / 1000}s)" else ""
                prefsManager.logWarning("API", "Rate limit hit on $providerName$delayInfo", rle.message)

                broadcastRateLimit(rle, false)
            } catch (e: Exception) {
                Log.e(TAG, "Content generation failed: ${e.message}", e)
                val durationMs = System.currentTimeMillis() - startTime
                prefsManager.recordApiFailure(durationMs)

                // Log error
                val errorMsg = e.message ?: "Unknown error"
                prefsManager.logError("API", "Request failed", errorMsg)

                broadcastError(e.message, false)
            } finally {
                // Show completion notification and stop service
                notificationHelper.showCompletedNotification(
                    getString(R.string.notification_complete_title),
                    getString(R.string.notification_complete_message)
                )
                stopSelf(startId)
            }
        }
    }

    /**
     * Handles content refinement request.
     */
    private fun handleRefine(intent: Intent, startId: Int) {
        val originalPost = intent.getStringExtra(EXTRA_ORIGINAL_POST)
        val refinements = intent.getStringArrayListExtra(EXTRA_REFINEMENTS)
        val includeSource = intent.getBooleanExtra(EXTRA_INCLUDE_SOURCE, false)

        executor.execute {
            // Validation moved inside executor to prevent race conditions with stopSelf
            if (originalPost.isNullOrEmpty()) {
                broadcastError("Original post is required", true)
                stopSelf(startId)
                return@execute
            }

            if (refinements.isNullOrEmpty()) {
                broadcastError("At least one refinement option is required", true)
                stopSelf(startId)
                return@execute
            }

            val startTime = System.currentTimeMillis()
            try {
                Log.i(TAG, "Starting content refinement with options: $refinements")

                // Get provider name for logging
                val provider = prefsManager.getProvider()
                val providerDisplay = CuratorFactory.getProviderDisplayName(provider)

                // Log the refinement request start
                prefsManager.logInfo("API", "Refinement started via $providerDisplay ($refinements)")

                // Refine post using curator abstraction
                val curator = CuratorFactory.create(this@ContentGenerationService)
                val result = curator.refinePost(originalPost, refinements, includeSource)

                // Calculate duration
                val durationMs = System.currentTimeMillis() - startTime

                // Record token usage with duration
                val promptTokens = curator.lastPromptTokens
                val candidateTokens = curator.lastCandidateTokens
                val totalTokens = curator.lastTotalTokens
                prefsManager.recordApiSuccess(promptTokens, candidateTokens, totalTokens, durationMs)

                // Log success
                prefsManager.logInfo("API", "Refinement successful via $providerDisplay ($totalTokens tokens, ${durationMs}ms)")

                Log.i(TAG, "Content refinement successful")
                broadcastSuccess(result, true)

            } catch (rle: RateLimitException) {
                Log.w(TAG, "Rate limit hit during refinement: ${rle.message}")
                val durationMs = System.currentTimeMillis() - startTime
                prefsManager.recordApiFailure(durationMs)

                // Log rate limit error
                val providerName = rle.providerName ?: "Unknown"
                val delayMs = rle.retryDelayMs
                val delayInfo = if (delayMs > 0) " (retry in ${delayMs / 1000}s)" else ""
                prefsManager.logWarning("API", "Rate limit during refinement on $providerName$delayInfo", rle.message)

                broadcastRateLimit(rle, true)
            } catch (e: Exception) {
                Log.e(TAG, "Content refinement failed: ${e.message}", e)
                val durationMs = System.currentTimeMillis() - startTime
                prefsManager.recordApiFailure(durationMs)

                // Log error
                val errorMsg = e.message ?: "Unknown error"
                prefsManager.logError("API", "Refinement failed", errorMsg)

                broadcastError(e.message, true)
            } finally {
                // Show completion notification and stop service
                notificationHelper.showCompletedNotification(
                    getString(R.string.notification_complete_title),
                    getString(R.string.notification_complete_message)
                )
                stopSelf(startId)
            }
        }
    }

    /**
     * Broadcasts successful result to MainActivity.
     */
    private fun broadcastSuccess(result: String, isRefinement: Boolean) {
        val broadcast = Intent(BROADCAST_RESULT).apply {
            putExtra(EXTRA_SUCCESS, true)
            putExtra(EXTRA_RESULT, result)
            putExtra(EXTRA_IS_REFINEMENT, isRefinement)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    /**
     * Broadcasts error to MainActivity.
     */
    private fun broadcastError(error: String?, isRefinement: Boolean) {
        val errorMessage = error ?: "Unknown error"
        val broadcast = Intent(BROADCAST_RESULT).apply {
            putExtra(EXTRA_SUCCESS, false)
            putExtra(EXTRA_ERROR, errorMessage)
            putExtra(EXTRA_IS_REFINEMENT, isRefinement)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)

        // Show error notification
        notificationHelper.showErrorNotification(
            getString(R.string.notification_error_title),
            errorMessage
        )
    }

    /**
     * Broadcasts rate limit error to MainActivity for fallback handling.
     */
    private fun broadcastRateLimit(rle: RateLimitException, isRefinement: Boolean) {
        val broadcast = Intent(BROADCAST_RESULT).apply {
            putExtra(EXTRA_SUCCESS, false)
            putExtra(EXTRA_IS_RATE_LIMIT, true)
            putExtra(EXTRA_RATE_LIMIT_PROVIDER, rle.providerName)
            putExtra(EXTRA_RETRY_DELAY_MS, rle.retryDelayMs)
            putExtra(EXTRA_ERROR, rle.message)
            putExtra(EXTRA_IS_REFINEMENT, isRefinement)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)

        Log.i(TAG, "Rate limit broadcast sent for provider: ${rle.providerName}")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")
        if (::executor.isInitialized) {
            executor.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ContentGenService"

        // Intent actions
        const val ACTION_GENERATE = "com.najmi.oreamnos.ACTION_GENERATE"
        const val ACTION_REFINE = "com.najmi.oreamnos.ACTION_REFINE"

        // Broadcast actions for results
        const val BROADCAST_RESULT = "com.najmi.oreamnos.BROADCAST_RESULT"

        // Intent extras
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_ORIGINAL_POST = "extra_original_post"
        const val EXTRA_REFINEMENTS = "extra_refinements"
        const val EXTRA_INCLUDE_SOURCE = "extra_include_source"
        const val EXTRA_KEEP_STRUCTURE = "extra_keep_structure"

        // Result extras
        const val EXTRA_SUCCESS = "extra_success"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_ERROR = "extra_error"
        const val EXTRA_IS_REFINEMENT = "extra_is_refinement"
        const val EXTRA_IS_RATE_LIMIT = "extra_is_rate_limit"
        const val EXTRA_RATE_LIMIT_PROVIDER = "extra_rate_limit_provider"
        const val EXTRA_RETRY_DELAY_MS = "extra_retry_delay_ms"
    }
}
