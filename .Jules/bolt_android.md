# Bolt's Journal - Android Performance Patterns

## ⚡ Performance Observations

### Regex Compilation in Hot Paths
**Observation:** `Pattern.compile()` is expensive and should not be called inside methods that run frequently (like loops or text change listeners).
**Impact:** Compiling regex takes 100x-1000x more time than matching.
**Solution:** Move patterns to `private static final` fields.

### String Manipulation
**Observation:** `String.split()` internally compiles regex.
**Impact:** Using `str.split("\\s+")` inside `onTextChanged` causes regex compilation on every keystroke.
**Solution:** Use a pre-compiled `Pattern` and `pattern.split(str)`.
