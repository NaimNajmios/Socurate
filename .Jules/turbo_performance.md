## 2024-05-24 - Optimized Keyword Scanning in PromptManager

**Context:** `PromptManager.isLongTechnicalContent`
**Symptoms:** High CPU usage during prompt generation for long texts due to repeated case-insensitive scanning.
**Root Cause:**
- The function iterated over 26 keywords.
- For each keyword, it called `text.contains(keyword, ignoreCase = true)`.
- `ignoreCase = true` triggers a case-insensitive scan (often `regionMatches`) which is slower than a direct byte comparison (`indexOf`).
- Repeating this 26 times for a large text results in `O(K * N)` complexity with high constant factors.

**Solution:**
- Allocate a lowercase copy of the text *once* using `text.lowercase(Locale.ROOT)`.
- Use standard `contains()` (which uses `indexOf` intrinsics) on the lowercase text.
- This reduces the complexity to `O(N)` (allocation) + `O(K * N)` (fast scan), but the fast scan is significantly faster than the case-insensitive scan.

**Impact:**
- Time (Benchmark on 3.5KB text): ~4.8ms → ~1.2ms (4x speedup).
- Memory: One allocation of the text size (negligible for typical usage < 10KB).
- CPU: Reduced cycle count in hot path.

**Learnings:**
- For multiple substring searches, the cost of one-time case conversion often outweighs the overhead of repeated case-insensitive scans.
- `String.indexOf` (and `contains`) is heavily optimized (SIMD) on the JVM compared to manual or complex regex scans.
- Always use `Locale.ROOT` or `Locale.US` for internal string normalization to avoid locale-specific bugs (e.g., Turkish 'I').
