package com.najmi.oreamnos.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.najmi.oreamnos.R;
import com.najmi.oreamnos.utils.NotificationHelper;
import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Foreground Service for generating AI content in the background.
 * This service continues running even when the app is minimized.
 */
public class ContentGenerationService extends Service {

    private static final String TAG = "ContentGenService";

    // Intent actions
    public static final String ACTION_GENERATE = "com.najmi.oreamnos.ACTION_GENERATE";
    public static final String ACTION_REFINE = "com.najmi.oreamnos.ACTION_REFINE";

    // Broadcast actions for results
    public static final String BROADCAST_RESULT = "com.najmi.oreamnos.BROADCAST_RESULT";

    // Intent extras
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    public static final String EXTRA_ORIGINAL_POST = "extra_original_post";
    public static final String EXTRA_REFINEMENTS = "extra_refinements";
    public static final String EXTRA_INCLUDE_SOURCE = "extra_include_source";

    // Result extras
    public static final String EXTRA_SUCCESS = "extra_success";
    public static final String EXTRA_RESULT = "extra_result";
    public static final String EXTRA_ERROR = "extra_error";
    public static final String EXTRA_IS_REFINEMENT = "extra_is_refinement";

    private ExecutorService executor;
    private NotificationHelper notificationHelper;
    private PreferencesManager prefsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        executor = Executors.newSingleThreadExecutor();
        notificationHelper = new NotificationHelper(this);
        prefsManager = new PreferencesManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started with action: " + (intent != null ? intent.getAction() : "null"));

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Start foreground immediately
        startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                notificationHelper.buildForegroundNotification(
                        getString(R.string.notification_generating_title),
                        getString(R.string.notification_generating_message)));

        switch (action) {
            case ACTION_GENERATE:
                handleGenerate(intent);
                break;
            case ACTION_REFINE:
                handleRefine(intent);
                break;
            default:
                Log.w(TAG, "Unknown action: " + action);
                stopSelf();
        }

        return START_NOT_STICKY;
    }

    /**
     * Handles content generation request.
     */
    private void handleGenerate(Intent intent) {
        String inputText = intent.getStringExtra(EXTRA_INPUT_TEXT);
        boolean includeSource = intent.getBooleanExtra(EXTRA_INCLUDE_SOURCE, false);

        if (inputText == null || inputText.isEmpty()) {
            broadcastError("Input text is required", false);
            stopSelf();
            return;
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "Starting content generation...");
                String content = inputText;

                // Check if input is a URL
                if (WebContentExtractor.isUrl(inputText)) {
                    Log.i(TAG, "Input is URL, extracting content...");
                    WebContentExtractor extractor = new WebContentExtractor();
                    content = extractor.extractContent(inputText);
                }

                // Generate post with Gemini
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                String result = gemini.curatePost(content, includeSource);

                Log.i(TAG, "Content generation successful");
                broadcastSuccess(result, false);

            } catch (Exception e) {
                Log.e(TAG, "Content generation failed: " + e.getMessage(), e);
                broadcastError(e.getMessage(), false);
            } finally {
                // Show completion notification and stop service
                notificationHelper.showCompletedNotification(
                        getString(R.string.notification_complete_title),
                        getString(R.string.notification_complete_message));
                stopSelf();
            }
        });
    }

    /**
     * Handles content refinement request.
     */
    private void handleRefine(Intent intent) {
        String originalPost = intent.getStringExtra(EXTRA_ORIGINAL_POST);
        ArrayList<String> refinements = intent.getStringArrayListExtra(EXTRA_REFINEMENTS);
        boolean includeSource = intent.getBooleanExtra(EXTRA_INCLUDE_SOURCE, false);

        if (originalPost == null || originalPost.isEmpty()) {
            broadcastError("Original post is required", true);
            stopSelf();
            return;
        }

        if (refinements == null || refinements.isEmpty()) {
            broadcastError("At least one refinement option is required", true);
            stopSelf();
            return;
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "Starting content refinement with options: " + refinements);

                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                String result = gemini.refinePost(originalPost, refinements, includeSource);

                Log.i(TAG, "Content refinement successful");
                broadcastSuccess(result, true);

            } catch (Exception e) {
                Log.e(TAG, "Content refinement failed: " + e.getMessage(), e);
                broadcastError(e.getMessage(), true);
            } finally {
                // Show completion notification and stop service
                notificationHelper.showCompletedNotification(
                        getString(R.string.notification_complete_title),
                        getString(R.string.notification_complete_message));
                stopSelf();
            }
        });
    }

    /**
     * Broadcasts successful result to MainActivity.
     */
    private void broadcastSuccess(String result, boolean isRefinement) {
        Intent broadcast = new Intent(BROADCAST_RESULT);
        broadcast.putExtra(EXTRA_SUCCESS, true);
        broadcast.putExtra(EXTRA_RESULT, result);
        broadcast.putExtra(EXTRA_IS_REFINEMENT, isRefinement);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    /**
     * Broadcasts error to MainActivity.
     */
    private void broadcastError(String error, boolean isRefinement) {
        Intent broadcast = new Intent(BROADCAST_RESULT);
        broadcast.putExtra(EXTRA_SUCCESS, false);
        broadcast.putExtra(EXTRA_ERROR, error != null ? error : "Unknown error");
        broadcast.putExtra(EXTRA_IS_REFINEMENT, isRefinement);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        // Show error notification
        notificationHelper.showErrorNotification(
                getString(R.string.notification_error_title),
                error != null ? error : "Unknown error");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't support binding
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        if (executor != null) {
            executor.shutdown();
        }
        super.onDestroy();
    }
}
