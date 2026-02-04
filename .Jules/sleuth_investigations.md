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

## 2024-05-24 - Regex Logic Error Destroying Structure

**Context:** `WebContentExtractor.cleanContent` normalizes text by collapsing whitespace and normalizing newlines.
**Symptoms:** Extracted text from URLs loses all paragraph structure, becoming a single massive block of text.
**Root Cause:**
The `cleanContent` method used `WHITESPACE_PATTERN` defined as `\s+` to collapse whitespace.
In Java regex, `\s` includes `\n` (newlines).
The code ran `WHITESPACE_PATTERN` *before* `NEWLINES_PATTERN`.
This caused all newlines to be replaced by a single space, destroying paragraph breaks. The subsequent `NEWLINES_PATTERN` (looking for `\n`) found nothing to match.

**Code Example (Before):**
```kotlin
private val WHITESPACE_PATTERN: Pattern = Pattern.compile("\\s+") // Matches \n too!

// ...
cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ")
cleaned = NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n") // Useless, newlines gone
```

**Fix Applied:**
Changed the regex to `[ \t]+` (spaces and tabs only) and renamed it to `HORIZONTAL_WHITESPACE_PATTERN` to clearly communicate intent. This preserves newlines so the subsequent newline normalization logic works correctly.

**Code Example (After):**
```kotlin
private val HORIZONTAL_WHITESPACE_PATTERN: Pattern = Pattern.compile("[ \\t]+")

// ...
cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ") // Preserves \n
cleaned = NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n") // Works correctly
```

## 2024-05-21 - Null Safety Violation in MainActivity

**Context:** `MainActivity.kt` uses `!!` operator on mutable state variables `rateLimitInfo` and `pillToEdit` inside Composable functions.

**Symptoms:** Potential `NullPointerException` if `rateLimitInfo` or `pillToEdit` are set to null (e.g., by another event or concurrent modification) between the time they are checked and the time they are accessed inside a lambda (e.g., `onSwitchAndRetry` or `onSave`).

**Root Cause:**
Usage of `!!` on mutable properties.
```kotlin
    if (showRateLimitDialog && rateLimitInfo != null) {
        RateLimitDialog(
            // ...
            onSwitchAndRetry = { newProvider ->
                // ...
                if (rateLimitInfo!!.isRefinement) { // Unsafe! rateLimitInfo could be null here if dialog was concurrently dismissed or state changed.
```

**Fix Applied:**
Capture the non-null value using `let` or a local variable before passing it to the dialog or usage.

**Prevention:**
Always use `let`, `run`, or local variable capture when working with nullable mutable state properties in Compose, especially when passing them to callbacks or lambdas that might execute later.

**Code Example (Before):**
```kotlin
    if (showRateLimitDialog && rateLimitInfo != null) {
        RateLimitDialog(
             retryDelayMs = rateLimitInfo!!.retryDelayMs,
             // ...
```

**Code Example (After):**
```kotlin
    if (showRateLimitDialog) {
        rateLimitInfo?.let { info ->
            RateLimitDialog(
                 retryDelayMs = info.retryDelayMs,
                 // ...
```

## 2024-05-24 - Race Condition in Concurrent Service Requests

**Context:** `ContentGenerationService` handles concurrent AI generation tasks using Kotlin Coroutines (`serviceScope.launch`).
**Symptoms:** If multiple requests are sent to the service rapidly, a shorter/faster request (e.g., validation failure or short generation) finishing *before* a longer request can cause the service to stop prematurely, killing the longer-running job.
**Root Cause:**
The service relied on `stopSelf(startId)` called at the end of each coroutine.
If Request 2 (startId=2) finishes before Request 1 (startId=1), it calls `stopSelf(2)`.
Since `2` is the latest startId, the system stops the service.
This kills the `CoroutineScope` and cancels Request 1.
The standard `stopSelf(startId)` mechanism works for sequential processing but fails for parallel processing where job completion order is non-deterministic.

**Fix Applied:**
Introduced `ServiceConcurrencyTracker` (thread-safe counter) to track active jobs.
Increment job count in `onStartCommand`.
Decrement job count in `finally` block of every job.
Only call `stopSelf(latestStartId)` when the active job count reaches zero.

**Code Example (Before):**
```kotlin
    // Inside handleGenerate coroutine
    try {
        // ... work ...
    } finally {
        stopSelf(startId) // DANGEROUS: Might stop service while other jobs run
    }
```

**Code Example (After):**
```kotlin
    // Inside handleGenerate coroutine
    try {
        // ... work ...
    } finally {
        val stopId = concurrencyTracker.onJobFinished()
        if (stopId != null) {
            stopSelf(stopId) // SAFE: Only stops when ALL jobs are done
        }
    }
```
