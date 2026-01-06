## 2024-05-23 - Shimmy Entrance Animation

**Context:** Swipeable Output Card (Swipe to Copy/Share)
**Observation:** Users often missed the swipe functionality on the output card because the actions (Copy/Share) were hidden behind the content layer with no affordance.
**Solution:** Implemented a "Shimmy" entrance animation that automatically slides the content layer back and forth (Right then Left) to peek the hidden actions. Added fading chevron hints during the animation to reinforce directionality.
**Impact:** Drastically improves discoverability of the swipe gestures without permanent visual clutter.

**Code Pattern:**
```kotlin
// Track interaction to cancel animation
var isInteracted by remember { mutableStateOf(false) }

// Shimmy Sequence
LaunchedEffect(Unit) {
    delay(600) // Wait for entrance
    if (!isInteracted) offsetX = 50f // Reveal Left Action
    delay(500)
    if (!isInteracted) offsetX = 0f
    delay(200)
    if (!isInteracted) offsetX = -50f // Reveal Right Action
    delay(500)
    if (!isInteracted) offsetX = 0f
}

// Hint Visibility
val shimmyVisible = !isInteracted && abs(animatedOffset) > 10f
val hintAlpha by animateFloatAsState(if (shimmyVisible) 0.6f else 0f)
```
