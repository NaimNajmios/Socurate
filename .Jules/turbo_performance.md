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

## 2026-01-19 - Optimized Markdown Parsing Logic in MarkdownUtils

**Context:** `MarkdownUtils.parseInlineFormatting`
**Symptoms:** Suboptimal performance scaling on long texts due to repeated full-string scans for multiple markers.
**Root Cause:**
- The function scanned the remaining string for `**`, `*`, and `_` separately at every iteration.
- This resulted in `O(K * N)` complexity where K is the number of markers/segments, as `indexOf` scans to the end (or next marker).
- For text with M segments, it effectively scanned the string 3 * M times.

**Solution:**
- Replaced the `while` loop containing multiple `indexOf` calls with a single-pass character scan.
- The new loop iterates through characters `O(N)` and only triggers `indexOf` when a marker is found (to find its closer).
- Double underscore `__` skipping logic was preserved.

**Impact:**
- Time (Benchmark on 700KB text): 413ms → 288ms (~1.44x speedup).
- CPU: Reduced redundant string scanning operations.
- Complexity: Reduced from pseudo-quadratic to linear `O(N)`.

**Learnings:**
- Avoid calling `indexOf` repeatedly inside a loop if you can perform a single-pass scan.
- When parsing multiple delimiters, a state-machine or single-pass loop is often more efficient than searching for "next occurrence of A, B, or C".
