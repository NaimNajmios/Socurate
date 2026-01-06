## 2024-05-23 - SwipeableOutputBox Recomposition Optimization

**Context:** The `SwipeableOutputBox` component was recomposing on every frame during swipe gestures.
**Metric Impact:**
- Recomposition count during drag: ~1 per frame (60-120/sec) → 0 per frame (layout/draw only).
- Significant reduction in main thread work during gestures.

**Root Cause:**
The `offsetX` state variable (updated every drag event) was being read in the main composition scope (e.g., inside `Box` modifiers like `.alpha(...)` and for `isActive` boolean checks). This invalidated the scope of `SwipeableOutputBox` continuously.

**Solution:**
1.  **Defer State Reads:** Moved visual transformations (translation, alpha) into `Modifier.graphicsLayer { ... }`. This allows the state to be read during the draw phase, bypassing composition.
2.  **Isolate Logic:** Used `derivedStateOf` for the `isActive` (threshold) logic. This ensures that the component only recomposes when the threshold is actually crossed (a discrete event), rather than on every pixel of movement.
3.  **Animatable vs State:** Switched from `mutableFloatStateOf` to `Animatable`. This allows imperative updates via `snapTo` inside a coroutine, which integrates cleanly with the gesture detector without triggering composition-side state reads in the gesture callback itself (though effectively similar, `Animatable` is the standard for gesture-driven animations).

**Learnings:**
- **Pattern:** Always use `graphicsLayer` for values that change rapidly (animations, gestures).
- **Anti-Pattern:** Reading a `MutableState` directly in a Composable's body that updates every frame (e.g., scroll offset, drag offset).
