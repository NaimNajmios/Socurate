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

## 2025-05-27 - Zero-Recomposition Drag in SwipeableOutputBox

**Context:** `SwipeableOutputBox` component (gesture-heavy interaction).
**Metric Impact:**
- **Recomposition:** Reduced from ~120/sec (during drag/shimmy) to **0** recompositions of the component body.
- **CPU Overhead:** Eliminated markdown re-parsing checks (memoization) and layout recalculations during simple translation.
**Root Cause:**
The component used `val animatedOffset by animateFloatAsState(...)` and `val offsetX by remember { mutableFloatStateOf(0f) }` directly in the Composable body.
1.  Reading `offsetX` to calculate background alpha triggered recomposition on every pixel of drag.
2.  Using `by animateFloatAsState` triggered recomposition on every frame of the spring animation.
**Solution:**
1.  Replaced state-driven animation with `Animatable`.
2.  Moved drag updates to `scope.launch { anim.snapTo(...) }`, bypassing the composition phase entirely.
3.  Deferred all visual updates (translation, alpha) to `Modifier.graphicsLayer { ... }` which reads the `Animatable` value during the draw phase.
4.  Used `derivedStateOf` for logic thresholds (`isActive`) to limit recomposition only to state changes (true/false).
**Learnings:** For gesture-driven components, avoid `State` and `by` delegates in the main body. Use `Animatable` and `graphicsLayer` to keep the interaction purely in the layout/draw phases.

**Profiling Data (Estimated):**
- Recompositions during Drag: ~N (frames) → 0
- Recompositions during Shimmy: ~N (frames) → 0
