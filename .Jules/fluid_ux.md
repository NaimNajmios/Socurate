## 2024-05-24 - Shimmy Hint for Discoverability

**Context:** The `SwipeableOutputBox` contains hidden actions (Copy/Share) accessible via swipe gestures.
**Observation:** Swipe gestures on mobile have low discoverability. Users might not realize the box is interactive, especially since it contains scrollable text.
**Solution:** Implemented a "Shimmy" entrance animation using `LaunchedEffect`. Upon appearance, the box briefly slides right (revealing Share hint) then left (revealing Copy hint) before settling. Added fading chevron arrows as an explicit visual cue.
**Impact:** Explicitly teaches the user the interaction model without requiring a tutorial overlay. The animation is subtle but noticeable enough to prompt exploration.

**Code Pattern:**
```kotlin
// UX: "Shimmy" animation to hint at swipeability
LaunchedEffect(Unit) {
    delay(600) // Wait for entrance animation
    // Shimmy Right (Hint Share)
    offsetX = 40f
    delay(300)
    // Shimmy Left (Hint Copy)
    offsetX = -40f
    delay(300)
    // Return to center
    offsetX = 0f
    delay(1000)
    // Fade out visual hints
    showHints = false
}
```

Animation Spec:
```kotlin
spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)
```
