## 2024-05-22 - Markdown Parsing Optimization

**Context:** `MarkdownUtils.parseInlineFormatting`
**Symptoms:** Iterative `indexOf` calls inside a loop caused repeated string scanning (O(N*M)).
**Root Cause:** The parser scanned from `currentIndex` to `end` for *each* marker type (`**`, `*`, `_`) independently, resulting in redundant traversals.
**Solution:** Implemented a single-pass character scanner to identify the next marker index. Once a marker start is found, `indexOf` is used efficiently only for the closing marker.
**Impact:**
- Time: ~1.9s → ~1.4s (for 50k iterations on complex text)
- Speedup: ~1.34x
- Complexity: Reduced from O(N*M) to O(N) for scanning.

**Learnings:**
- Replacing multiple `indexOf` calls with a single manual loop scan is highly effective when searching for multiple potential delimiters.
- `AnnotatedString.Builder` operations can be decoupled from parsing logic for testing.
