## 2024-05-23 - PromptManager Singleton & Gson Sharing

**Context:** `GeminiService.kt` and `PromptManager.kt`
**Symptoms:** Excessive object allocation during API requests (new `PromptManager` and `Gson` instances per request).
**Root Cause:** `GeminiService` is instantiated per request, leading to repeated initialization of heavy objects.
**Solution:** Converted `PromptManager` to `object` (singleton) and moved `Gson` to `companion object` in `GeminiService`.
**Impact:**
- Memory: Eliminated 2 objects per request (PromptManager + Gson reflection setup).
- CPU: Reduced initialization overhead significantly (Gson setup is expensive).

**Learnings:** In high-frequency or per-request service classes, heavy stateless dependencies like Gson or helper utilities should be singletons or static fields.
