
## 2025-05-15 - Animated Button State

**Context:** The "GENERATE" button in `MainActivity` triggers a long-running operation but lacked integrated feedback, relying on an external loading card. The button also felt static when pressed.

**Learning:** Integrating `isLoading` state directly into the button component with an `AnimatedContent` transition (Text <-> CircularProgressIndicator) provides immediate, localized feedback. Adding a scale animation (`0.95f`) on press adds tactile delight and responsiveness, critical for the "Neo" design language which favors bold, chunky interactions.

**Application:** Use `animateFloatAsState` driven by `MutableInteractionSource` for press states on all custom buttons. Use `AnimatedContent` for smooth transitions between "Idle" and "Loading" content within the same container.

**Example:**
```kotlin
val interactionSource = remember { MutableInteractionSource() }
val isPressed by interactionSource.collectIsPressedAsState()
val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

Button(
    onClick = onClick,
    modifier = modifier.scale(scale),
    interactionSource = interactionSource
) {
    AnimatedContent(targetState = isLoading) { loading ->
        if (loading) CircularProgressIndicator() else Text("ACTION")
    }
}
```
