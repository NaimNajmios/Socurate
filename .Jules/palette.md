## 2024-05-23 - Accessibility: Icon-only Buttons
**Learning:** Icon-only buttons (ImageButton) without `contentDescription` are invisible to screen readers (TalkBack), which announces them as "Unlabeled button". This completely blocks navigation for visually impaired users.
**Action:** Always verify `ImageButton` and `ImageView` with interactivity have `android:contentDescription` pointing to a localized string resource.
