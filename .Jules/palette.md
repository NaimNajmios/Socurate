## 2024-05-23 - Replacing Legacy Toasts with NeoSnackbars

**Context:** `MainActivity.kt` used `Toast.makeText` for feedback like "Content pasted" or "Clipboard empty". This felt disjointed from the Neo design system.

**Learning:** `Toast` messages are system-level and cannot be styled to match the app's "Neo-brutalism" aesthetic (sharp corners, high contrast). Using `SnackbarHost` with a custom component allows full control over the visual presentation while maintaining accessibility and standard behaviors (auto-dismiss).

**Application:** When feedback is needed in Compose:
1.  Hoist `SnackbarHostState`.
2.  Pass it to `Scaffold`'s `snackbarHost` parameter.
3.  Use a custom composable (like `NeoSnackbar`) inside the `SnackbarHost` lambda.
4.  Launch coroutines to show messages.

**Example:**
```kotlin
Scaffold(
    snackbarHost = {
        SnackbarHost(hostState) { data ->
            NeoSnackbar(data) // Custom styled component
        }
    }
) {
    // ...
    scope.launch { hostState.showSnackbar("Polished feedback!") }
}
```
