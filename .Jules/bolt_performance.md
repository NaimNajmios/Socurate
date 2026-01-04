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
