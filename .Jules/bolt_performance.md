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
