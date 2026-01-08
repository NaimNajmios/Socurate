## 2024-05-24 - ReadabilityUtils Syllable Counting Optimization

**Context:** `ReadabilityUtils.countSyllables` is a hot path used by `calculateFleschKincaidGradeLevel`, which iterates over every word in the text.
**Symptoms:** The original implementation iterated over each word string twice (once for effective length, once for syllable counting) and used inefficient character checks.
**Root Cause:** Two-pass algorithm and potentially boxed character operations or string lookups for vowels.
**Solution:**
1. Implemented a single-pass algorithm that calculates effective length and syllable count simultaneously.
2. Replaced string lookup (`lowerC in "aeiouy"`) with direct character comparison.
3. Used bitwise optimization for ASCII case conversion.
**Impact:**
- **Execution Time:** ~2.37x speedup (Benchmark: 3209ms -> 1352ms for 2M iterations of sample words).
- **Allocations:** Zero allocations (maintained from previous version, but reduced CPU cycles).
- **CPU:** Reduced instruction count by avoiding double iteration and optimizing checks.

**Learnings:** For text processing hot paths, always strive for single-pass algorithms (`O(N)`). Even simple loops add up when executed millions of times.

## 2024-05-27 - StringUtils.stripLeadingEmojis Regex Optimization

**Context:** `StringUtils.stripLeadingEmojis` is used in UI rendering (ShareBottomSheet, MainActivity) to clean up generated text.
**Symptoms:** The function unconditionally compiled a `Matcher` and ran `replaceAll` even for text containing no emojis (the common case).
**Root Cause:** The Regex engine has initialization overhead. `Pattern.matcher(text)` allocates objects even if no match is found.
**Solution:**
1. Added a fast-path check `containsPotentialEmoji(text)` (already used in `stripAllEmojis`) before invoking the regex engine.
2. If no potential emoji characters are present, the function returns immediately.
**Impact:**
- **Execution Time (Clean Text):** ~12.5x speedup (Benchmark: 1171ms -> 94ms for 1M iterations).
- **Allocations:** Zero allocations for clean text (avoiding `Matcher`, `String` result from replaceAll).
- **CPU:** Significant reduction in CPU usage for non-emoji text.

**Learnings:** Regex is expensive. For patterns that match a specific subset of characters (like emojis), a linear scan bloom filter (`O(N)`) is significantly faster than regex engine initialization for the negative case.

## 2024-06-03 - PromptManager.containsBulletPoints Optimization

**Context:** `PromptManager.containsBulletPoints` is used to detect list structures in input text to determine prompt instructions.
**Symptoms:** The original implementation split the text into a List<String> (`lines()`), trimmed every line, and compiled a Regex for every line until a match was found.
**Root Cause:** Excessive temporary object creation (Lists, Strings) and repeated Regex compilation in a loop.
**Solution:**
1. Replaced `lines()` and `any {}` with a single-pass character scan.
2. Replaced `trim()` with manual whitespace skipping indices.
3. Replaced Regex matching with direct character checks (state machine).
**Impact:**
- **Execution Time:** ~153x speedup (Benchmark: 18540ms -> 121ms for 2000 iterations on large text).
- **Allocations:** Reduced from O(N) String allocations (one per line) to Zero allocations.
- **CPU:** Eliminated Regex compilation overhead entirely.

**Learnings:** Avoid `lines()` and `trim()` in hot paths or large text processing. Manual character traversal is significantly more performant and memory-friendly in Kotlin/Java.
