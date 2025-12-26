## 2024-12-26 - Markdown Stripping Logic Error

**Context:** `GeminiService.cleanUpResponse` logic
**Symptoms:** Bold (`**text**`) and Italic (`*text*`) formatting is stripped from generated content, despite `MainActivity` having support for rendering it.
**Root Cause:** `ASTERISK_TEXT_PATTERN` regex (`\*+(.*?)\*+` -> `$1`) aggressively removes all asterisks, flattening the text to plain text. This was likely intended to clean up unwanted artifacts but breaks the rich text feature.
**Fix Applied:** Removed the asterisk stripping logic from `cleanUpResponse`.
**Prevention:** Ensure data cleaning layers do not strip formatting required by the presentation layer.
**Tests Added:** Updated `GeminiServiceCleanupTest` to verify asterisks are PRESERVED.
