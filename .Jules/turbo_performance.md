## 2024-05-21 - [MarkdownUtils Parsing Optimization]

**Context:** `MarkdownUtils.parseInlineFormatting` used for rendering rich text in `SwipeableOutputBox`.
**Symptoms:** Repeated `indexOf` scans for multiple markers (`**`, `*`, `_`) caused O(K*N) complexity.
**Root Cause:** Inefficient loop structure that rescanned the string tail for every formatting type separately.
**Solution:** Implemented a unified single-pass scanner that finds the earliest marker and processes it, reducing complexity to O(N).
**Impact:**
- Time: Theoretical speedup of ~3x for scan operations (reducing 3 scans to 1-2).
- Memory: Reduced temporary string allocations by avoiding overlapping searches.
- CPU: Reduced cycles spent in `indexOf` intrinsic calls.

**Learnings:**
- `String.indexOf` is fast but repeatedly calling it for different needles in a loop multiplies the cost.
- Unifying search for multiple markers using `min(idx1, idx2)` is effective when `indexOfAny` is not intrinsic.
- Parsing logic should always aim for single-pass consumption of the input.
