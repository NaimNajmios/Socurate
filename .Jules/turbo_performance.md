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
