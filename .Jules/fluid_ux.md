## 2024-05-23 - Animated Expansion for Contextual Actions

**Context:** The `InputCard` displays contextual actions (Link Preview) based on user input (URL detection).
**Observation:** Previously, the Link Preview would appear instantly, causing a jarring layout shift and pushing content down without warning.
**Solution:** Integrated `AnimatedVisibility` with `expandVertically` + `fadeIn` for the content, and applied `animateContentSize` with a `spring` spec to the parent container. This ensures the card expands fluidly to accommodate the new content.
**Impact:** The UI feels more organic and responsive. The user is guided to the new options rather than being surprised by them.

**Code Pattern:**
```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
) {
    // Permanent Content
    NeoInput(...)

    // Contextual Content
    AnimatedVisibility(
        visible = showContextualContent,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ContextualContent(...)
    }
}
```

Animation Spec:
```kotlin
spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)
```
