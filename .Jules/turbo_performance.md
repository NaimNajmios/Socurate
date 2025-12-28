## 2024-05-23 - Markdown Parsing Optimization

**Context:** `MarkdownUtils.parseMarkdownToAnnotatedString`
**Symptoms:** High memory allocation detected when parsing long AI-generated responses (creating `List<String>` for every line).
**Root Cause:** `String.split("\n")` creates a new String object for every line in the text, plus an ArrayList to hold them. For a 50-line response, this is 51 unnecessary object allocations.
**Solution:** Replaced `split` with an index-based `while` loop scanning for `\n` and `startsWith` checks using offsets.
**Impact:**
- Time: O(N) -> O(N) (but faster constant factor due to reduced GC pressure)
- Memory: Reduced from O(L) allocations (where L is lines) to O(1) auxiliary allocations (only the builder).
- CPU: Reduced overhead from Regex compilation inside `split()`.

**Learnings:** For simple line-by-line text processing, index-based scanning is significantly more memory-efficient than `split()`, especially in hot paths like UI rendering loops.
