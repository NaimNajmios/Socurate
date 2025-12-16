## 2024-05-24 - [Smart Input Field]
**Insight:** Consolidated "Paste" and "Clear" actions into the `TextInputLayout` End Icon.
**Pattern:**
- If input is empty -> Show Paste Icon.
- If input has text -> Show Clear Icon.
- This saves vertical space and keeps context-aware actions close to the input.
- Added Haptic Feedback to these actions for a tactile feel.
- Replaced custom character counter TextView with `TextInputLayout`'s native `app:counterEnabled="true"`.
