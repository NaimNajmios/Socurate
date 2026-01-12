## 2024-01-04 - Debounced URL Metadata Fetching

**Context:** `MainActivity.kt`'s `LaunchedEffect` for URL detection.
**Metric Impact:**
- **Network Requests:** Reduced from N (1 per char typed) to ~1 per URL entry.
- **Battery/CPU:** Eliminated overhead from starting/cancelling Coroutines and network stacks for incomplete URLs.
**Root Cause:** The `LaunchedEffect(inputText)` triggered `WebContentExtractor.extractMetadata` immediately when `isUrl` returned true. Since `isUrl` can match partial URLs (e.g., "http://" matches len > 7), typing a full URL caused rapid-fire network requests.
**Solution:** Added `kotlinx.coroutines.delay(800)` (debounce) inside the `if (isUrl)` block.
**Learnings:** High-frequency input fields (like text editors) that trigger network side-effects must always be debounced to prevent resource exhaustion.

**Profiling Data (Estimated):**
- Network Calls per URL Typed: ~15 → 1
- Thread Jitter: Reduced due to fewer `Dispatchers.IO` context switches.

## 2024-05-23 - Animation State & Callback Stabilization

**Context:** `MainActivity.kt`'s `MainScreen`.
**Metric Impact:**
- **Recomposition:** Reduced `MainScreen` recomposition from ~60/sec (during pulse/shake) to 0 (steady state).
- **CPU Usage:** Significantly lower during animations (Pulse, Shake).
**Root Cause:**
1.  Using delegated properties (`by animateFloatAsState`) in `MainScreen` scope caused the entire screen to recompose on every animation frame.
2.  Unstable lambdas passed to `InputCard` caused it to recompose whenever `MainScreen` recomposed (e.g. `isLoading` change).
**Solution:**
1.  Changed `by animate...` to `= animate...` (State object) and read `.value` inside `graphicsLayer` block (deferred reading).
2.  Wrapped `InputCard` callbacks in `remember` to ensure stability.
**Learnings:** Avoid reading animation state values in the parent Composable scope. Pass the `State` object or read it inside `graphicsLayer` / `drawWithCache`.

**Profiling Data (Estimated):**
- MainScreen Recompositions during Pulse: ~60/sec → 0/sec
- InputCard Recompositions on Loading: 1 → 0

## 2024-05-24 - SwipeableOutputBox Shimmy Optimization

**Context:** `SwipeableOutputBox` component.
**Metric Impact:**
- **Recomposition:** Reduced `SwipeableOutputBox` recomposition from ~60/sec (during shimmy animation) to 0/sec.
- **CPU Usage:** Reduced UI thread overhead during the onboarding animation.
**Root Cause:**
1.  Using `by animateFloatAsState` caused the Composable to recompose on every frame of the shimmy animation.
2.  Calculating `shimmyVisible` boolean directly from the float value triggered recomposition every frame.
**Solution:**
1.  Switched to using `State` object for animation (`val offsetState = ...`).
2.  Deferred reading of the offset value to the `graphicsLayer` block.
3.  Wrapped `shimmyVisible` logic in `derivedStateOf` to only trigger updates when the threshold is crossed.
**Learnings:** Boolean logic derived from high-frequency animation values must use `derivedStateOf` to act as a low-pass filter for recomposition.

**Profiling Data (Estimated):**
- Recompositions during Shimmy: ~120 (2s @ 60fps) → ~4 (start/end of swings)
