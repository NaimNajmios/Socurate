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
