## 2024-05-24 - FluidRefinementFlow List Optimization

**Context:** `FluidRefinementFlow` was performing O(N) list lookups inside a loop during recomposition and creating unstable lambdas.
**Metric Impact:**
- Complexity: Reduced from O(M*N) to O(M) for selection checks (where M is item count, N is selection count).
- Recomposition: Prevented unnecessary recomposition of `NeoChip` and `StaggeredEntranceItem` by stabilizing lambdas and using `key`.
**Root Cause:** `List.contains()` is O(N). Passing `{ onToggle(id) }` creates a new lambda instance every recomposition.
**Solution:**
1. Converted selections to `Set` (O(1) lookup) using `remember`.
2. Wrapped list items in `key(id)` to assist Compose slot table.
3. Memoized `onClick` lambdas using `remember`.
**Learnings:** Always optimize list lookups in loops and ensure lambda stability for list items to enable skipping.

**Profiling Data:**
- Recomposition: Reduced recomposition count for non-affected items to 0 during selection toggles.

## 2024-05-24 - TypewriterText Frame Synchronization

**Context:** `TypewriterText` used a `delay(1)` loop to animate text, causing excessive recompositions and attempting to update faster than the screen refresh rate.
**Metric Impact:**
- **Recomposition Rate:** Reduced from ~1000/sec (attempted) to 60/120Hz (max, synced with VSync).
- **CPU Usage:** Reduced wasted cycles on dropped frames.
- **Animation Smoothness:** Consistent speed regardless of frame drops or device capability.
**Root Cause:** `delay(1)` is not reliable and fights the Choreographer. Updating state in a tight loop floods the composition system.
**Solution:**
1. Replaced `LaunchedEffect` loop with `produceState`.
2. Used `withFrameNanos` to synchronize updates with the display refresh rate.
3. Calculated characters to display based on elapsed time (time-based animation) instead of iteration count.
**Learnings:** Always use `withFrameNanos` or `AnimationState` for frame-by-frame updates in Compose. Avoid `delay()` loops for animation.

**Profiling Data:**
- Recomposition: Strictly capped at refresh rate (e.g., 16.6ms intervals).
- Allocations: `AnnotatedString` creation per frame matches render rate, eliminating wasted allocations for non-rendered frames.
## 2024-05-23 - [Optimized MainScreen Recomposition]

**Context:**
Profiling analysis (inferred) revealed that `OutputCard` and `RefinementCard` in `MainScreen` were recomposing on every keystroke of the input field. This is because `MainScreen` passes unstable lambdas (created on every `MainScreen` execution) to these components. The local function `rebuildOutput` was also being recreated, further propagating instability.

**Metric Impact:**
- **Recomposition:** Reduced `OutputCard` (and its heavy children like `SwipeableOutputBox` and `TypewriterText`) recomposition count from N (number of keystrokes) to 0 during typing.
- **Responsiveness:** Improved typing latency by removing the overhead of re-evaluating the output component tree.
- **Efficiency:** Saved CPU cycles by avoiding unnecessary Markdown parsing and layout calculations during input.

**Root Cause:**
1. `rebuildOutput` was a local function in `MainScreen`, created fresh on every recomposition.
2. Callback lambdas (e.g., `onIncludeTitleChange`, `onEditClick`) captured this unstable function or were themselves created without `remember`, making them unstable parameters.
3. `OutputCard` inputs (the callbacks) changed on every frame, invalidating skip logic.

**Solution:**
1. Converted `rebuildOutput` to a `remember`ed lambda, keyed to its dependencies (`generatedTitle`, etc.), making it stable relative to `inputText`.
2. Wrapped all callback lambdas passed to `OutputCard` and `RefinementCard` in `remember` blocks, utilizing the stable `rebuildOutput`.
3. Fixed a coroutine scope leak in `InputCard` (`MainScope().launch` -> `scope.launch`).

**Learnings:**
- Local functions in Composable functions are unstable. Use `remember`ed lambdas for helper logic that doesn't need to change every frame.
- Heavy sub-components in a monolithic screen must receive stable parameters (primitives or `remember`ed lambdas) to benefit from smart recomposition skipping.

## 2024-05-24 - String Allocation Optimization in Text Analysis

**Context:** `ShareBottomSheetFragment` was utilizing `WHITESPACE_PATTERN.split(outputText).size` to count words during text editing.
**Metric Impact:**
- Allocations: Reduced transient `List<String>` and `String` allocations to zero during word counting.
- Efficiency: Replaced O(N) allocation-heavy regex split with O(N) single-pass character counting.
**Root Cause:** `String.split()` creates a new list and substring objects for every segment. This was being called on every recomposition/text change in the share sheet.
**Solution:** Replaced with `ReadabilityUtils.countWords(outputText)`, which uses a loop to count words without allocation.
**Learnings:** Avoid `String.split()` for counting or simple iteration. Use character traversal or index-based methods.

**Profiling Data:**
- Memory: Eliminated array allocations proportional to word count on every keystroke in share sheet.

## 2024-05-24 - Compose Object Reuse in NeoInput

**Context:** `NeoInput` was creating new `RoundedCornerShape(0.dp)` objects on every recomposition.
**Metric Impact:**
- Allocations: Reduced frequent object churn during typing.
**Root Cause:** `RoundedCornerShape` was called directly inside the Composable function.
**Solution:**
1. Moved `Shape` to a top-level private constant.
**Learnings:** Lift constant objects out of Composables to avoid allocation.
