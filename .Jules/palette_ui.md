## 2024-05-23 - Action Button Morph
**Insight:** A "Morphing Action Button" provides superior feedback compared to Toasts for primary actions.
Instead of showing a disconnected floating message, the button itself transforms to indicate success (e.g., "Copy" -> "Copied!").
This requires:
1.  **Scale Animation:** `scaleX`/`scaleY` to 0.9 then back to 1.0 (with `OvershootInterpolator` for pop) to mask the icon/text swap.
2.  **State Management:** Changing icon, text, and background tint programmatically.
3.  **Automatic Revert:** Using a `Handler` to restore the original state after a delay (e.g., 2000ms), ensuring the button is ready for reuse.
4.  **Lifecycle Safety:** Checking `isDestroyed() || isFinishing()` inside the Handler to prevent leaks.
5.  **Robust Restoration:** Always capture the *current* state (icon, text, colors) into final variables before animating, and use these for restoration. Do not rely on hardcoded resource IDs for the "original" state, as XML layouts may change.
