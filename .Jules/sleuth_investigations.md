## 2024-05-23 - Concurrency Bug in Service Lifecycle

**Context:** `ContentGenerationService.kt`, which uses a `SingleThreadExecutor` to queue and process generation requests sequentially.
**Symptoms:** If a user triggers a second generation request (e.g., "Refine") while the first one is still running, the second request is often silently dropped or never completes.
**Root Cause:** The service was calling `stopSelf()` unconditionally in the `finally` block of the first task. This instructs the Android system to destroy the Service immediately (`onDestroy` shuts down the executor), even if there are other tasks queued in the executor or subsequent `onStartCommand` calls have occurred.
**Fix Applied:** Changed `stopSelf()` to `stopSelf(startId)`. This conditional stop only terminates the service if the `startId` matches the most recent start request received by the service.
**Prevention:** Always use `stopSelf(startId)` in Services that handle multiple asynchronous requests to ensure proper lifecycle management. Avoid unconditional `stopSelf()` unless you are certain no other work is pending.

**Code Example (Before):**
```kotlin
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ...
        handleGenerate(intent) // startId ignored
        return START_NOT_STICKY
    }

    private fun handleGenerate(intent: Intent) {
        executor.execute {
            try {
                // process...
            } finally {
                stopSelf() // BUG: Stops service even if new requests came in
            }
        }
    }
```

**Code Example (After):**
```kotlin
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ...
        handleGenerate(intent, startId) // Pass startId
        return START_NOT_STICKY
    }

    private fun handleGenerate(intent: Intent, startId: Int) {
        executor.execute {
            try {
                // process...
            } finally {
                stopSelf(startId) // FIX: Only stops if this was the last request
            }
        }
    }
```
