## 2024-05-24 - Service Lifecycle Race Condition

**Context:** `ContentGenerationService` handles asynchronous AI generation tasks using a `SingleThreadExecutor`.
**Symptoms:** If a user triggers a second request that fails validation (e.g., empty input) immediately after a valid request, the service stops, potentially killing the valid request's execution environment.
**Root Cause:**
The `onStartCommand` method calls `handleGenerate` (or `handleRefine`) on the main thread.
Inside `handleGenerate`, input validation happens *synchronously*.
If validation fails, `stopSelf(startId)` is called immediately.
If a previous valid request is running in the background thread (executor), the `stopSelf(startId)` call (where `startId` corresponds to the *failed* request, which is the most recent one) tells the system "I'm done with the latest work".
The system then destroys the service (`onDestroy`), shutting down the executor and potentially killing the process, terminating the running valid request.

**Code Example (Before):**
```kotlin
    private fun handleGenerate(intent: Intent, startId: Int) {
        val inputText = intent.getStringExtra(EXTRA_INPUT_TEXT)
        // Validation on Main Thread
        if (inputText.isNullOrEmpty()) {
            broadcastError("Input text is required", false)
            stopSelf(startId) // BUG: Stops service even if older tasks are running
            return
        }

        executor.execute {
            // ... long running task ...
            stopSelf(startId)
        }
    }
```

**Fix Applied:**
Move the validation logic *inside* the `executor.execute` block. This ensures that the validation (and subsequent `stopSelf` call) is processed sequentially in the queue, only after the previous task has completed.

**Code Example (After):**
```kotlin
    private fun handleGenerate(intent: Intent, startId: Int) {
        val inputText = intent.getStringExtra(EXTRA_INPUT_TEXT)

        executor.execute {
             // Validation inside background thread (sequential)
            if (inputText.isNullOrEmpty()) {
                broadcastError("Input text is required", false)
                stopSelf(startId) // SAFE: Only called when this task's turn arrives
                return@execute
            }

            // ... long running task ...
            stopSelf(startId)
        }
    }
```
