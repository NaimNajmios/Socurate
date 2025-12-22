## 2024-05-22 - String Split Optimization
**Learning:** Redundant String.split() calls on the same text can be a significant bottleneck in text processing utilities.
**Action:** Share the result of the split operation between methods when analyzing the same text.

## 2024-05-23 - Multiline Regex vs Split Loops
**Learning:** For line-based replacements (like stripping prefixes), `Pattern.MULTILINE` with `replaceAll` is significantly faster (>3x) and memory-efficient than `split()` -> `loop` -> `StringBuilder` patterns, as it avoids array and substring allocations.
**Action:** Use `Pattern.compile("...", Pattern.MULTILINE)` for line-based text transformations. Ensure `\s` is replaced with `[ \t]` if newlines should be preserved.
