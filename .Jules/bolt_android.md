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

### Derived State for Animation Triggers
**Observation:** Calculating boolean flags from animated float values directly in a Composable function causes recomposition on every animation frame.
**Impact:** `val isVisible = animatedOffset > 10f` inside a Composable causes the function to re-execute every time `animatedOffset` changes (e.g. 60-120 times/sec).
**Solution:** Wrap the calculation in `derivedStateOf`.
```kotlin
val isVisible by remember { derivedStateOf { animatedOffsetState.value > 10f } }
```
This ensures recomposition only occurs when the result changes (true <-> false).

### Defer Reading Animation State
**Observation:** Using `by animateFloatAsState` reads the value during composition, forcing recomposition of the parent scope on every frame.
**Impact:** Excessive recomposition of complex UI layouts during simple animations.
**Solution:** Use `val state = animateFloatAsState` (no `by`) and read `state.value` only inside `Modifier.graphicsLayer { ... }`.
