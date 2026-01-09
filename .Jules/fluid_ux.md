
## 2025-05-24 - Success Snap Interaction

**Context:** Swipeable Output Card (Copy Action)
**Observation:** Users swiping to copy received haptic feedback but missed a clear visual confirmation, leading to uncertainty if the action succeeded. The card simply snapped back immediately.
**Solution:** Implemented a "Success Snap" pattern where the card locks in the open position for 1000ms upon successful copy, morphing the icon into a checkmark before resetting. This provides a focused moment of confirmation.
**Impact:** Eliminates ambiguity about the copy action's success and adds a delightful, polished feel to the gesture.

**Code Pattern:**
```kotlin
// Success Snap Logic
onDragEnd = {
    if (isSwipeToCopy) {
        onCopy()
        isSuccess = true
        offsetX = -snapOffset // Lock open

        scope.launch {
            delay(1000)
            isSuccess = false
            offsetX = 0f // Reset
        }
    }
}
```
