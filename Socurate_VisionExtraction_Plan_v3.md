# Vision Extraction Implementation Plan — Socurate (Updated)
**Feature:** Multi-path on-device vision extraction — Gemini Nano (AICore), PaliGemma, Gemma 3 4B, ML Kit fallback
**Repo:** https://github.com/NaimNajmios/Socurate
**Branch:** Jetpack-Compose-Migration
**Updated:** Reflects current AICore/ML Kit GenAI API capabilities as of March 2026

---

## Assumed Existing State

| File | Status | Role Going Forward |
|------|--------|--------------------|
| `utils/OcrUtils.kt` | Complete | Fallback only — unchanged |
| `utils/FootballOcrParser.kt` | Complete | Fallback only — unchanged |
| `viewmodel/OcrViewModel.kt` | Complete | Extended |
| `ui/components/OcrInputSheet.kt` | Complete | Extended |
| `prompts/PromptManager.kt` | Has `buildPromptFromOcr()` | Extended |
| `curator/IContentCurator.kt` | Complete | Reference pattern |
| `curator/CuratorFactory.kt` | Complete | Reference pattern |
| `MainActivity.kt` | Complete | Unchanged |

---

## Key Facts (Current API State)

Before reading the plan, these facts shape every decision in it:

**Gemini Nano via ML Kit GenAI APIs**
- Runs via AICore — shared model, no per-app download
- Now supports image description as a use case
- Available on Pixel 9 series, Samsung Galaxy S25, Xiaomi 15, Motorola Razr 60 Ultra, and other Snapdragon/Dimensity/Tensor devices
- Accessed via `com.google.mlkit:genai-common` — NOT the same as the old AI Edge SDK experimental path
- No model download needed — model is pre-installed and managed by Google
- Has inference quota per app and must be foreground-only

**PaliGemma 2 3B / Gemma 3 4B via MediaPipe**
- User downloads model file (~1.5–2GB) into app's private storage
- No re-download possible from another app — each app owns its own copy
- No device restrictions — works on Android 9+ with sufficient RAM
- No quota limits — inference runs as much as needed
- Full control over prompt and output format

**ML Kit OCR**
- Always available, no download, no device restriction
- Text extraction only — no understanding
- Existing implementation, zero changes needed

---

## Three-Path Architecture

```
IVisionExtractor
├── GeminiNanoExtractor      ← AICore path — no download, device-gated
├── PaliGemmaExtractor       ← MediaPipe path — user downloads ~1.5GB
├── Gemma3VisionExtractor    ← MediaPipe path — user downloads ~2.0GB
└── MlKitFallbackExtractor   ← Always available, existing OcrUtils wrapped
```

`VisionExtractorFactory` picks the best available path automatically, or respects user's explicit preference from Settings.

---

## Extraction Path Priority (Auto Mode)

When user has not manually selected a model, the factory uses this priority:

```
1. Gemini Nano (AICore)     — if device supports it and AICore is ready
          ↓ not available
2. PaliGemma 2 3B           — if user has downloaded it
          ↓ not downloaded
3. Gemma 3 4B               — if user has downloaded it
          ↓ not downloaded
4. ML Kit OCR               — always available, always last resort
```

User can override this in Settings and pin to any specific path including ML Kit.

---

## Package Structure (New Files)

```
com.najmi.oreamnos/
├── vision/
│   ├── IVisionExtractor.kt
│   ├── VisionExtractorFactory.kt
│   ├── VisionModel.kt
│   ├── GeminiNanoExtractor.kt       ← NEW vs previous plan
│   ├── PaliGemmaExtractor.kt
│   ├── Gemma3VisionExtractor.kt
│   └── MlKitFallbackExtractor.kt
├── utils/
│   └── VisionModelManager.kt        ← MediaPipe models only (Gemini Nano needs no manager)
├── ui/components/
│   └── VisionModelDownloadCard.kt
└── model/
    └── VisionExtractionResult.kt
```

---

## Phase 1 — Gradle Dependencies

**File:** `app/build.gradle`

```groovy
// MediaPipe — for PaliGemma and Gemma 3 4B paths
implementation 'com.google.mediapipe:tasks-vision:0.10.14'
implementation 'com.google.mediapipe:tasks-genai:0.10.14'

// ML Kit GenAI — for Gemini Nano via AICore path
implementation 'com.google.mlkit:genai-common:1.0.0-beta1'
implementation 'com.google.mlkit:genai-tasks-image-description:1.0.0-beta1'

// ML Kit OCR — already present, fallback path
implementation 'com.google.mlkit:text-recognition:16.0.0'
```

---

## Phase 2 — VisionModel Enum

**New file:** `vision/VisionModel.kt`

Defines all supported extraction paths as an enum with metadata. Single source of truth.

Entries:

| Enum Value | Display Name | Requires Download | Device Gate |
|---|---|---|---|
| `GEMINI_NANO` | Gemini Nano (AICore) | No | AICore-compatible devices only |
| `PALIGEMMA_2_3B` | PaliGemma 2 3B | Yes ~1.5GB | Android 9+, 6GB+ RAM |
| `GEMMA_3_4B` | Gemma 3 4B | Yes ~2.0GB | Android 9+, 8GB+ RAM |
| `ML_KIT` | OCR Fallback | No | All devices |

Each entry carries: `id`, `displayName`, `description`, `requiresDownload`, `approximateSizeMb`, `minimumRamGb`, `downloadUrl` (null for Nano and ML Kit).

---

## Phase 3 — IVisionExtractor Interface

**New file:** `vision/IVisionExtractor.kt`

Identical structure to `IContentCurator.kt`:

```kotlin
interface IVisionExtractor {
    val model: VisionModel

    suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult

    fun release()
}
```

All four implementations satisfy this interface. `OcrViewModel` only ever sees `IVisionExtractor` — never a concrete class.

---

## Phase 4 — VisionExtractionResult

**New file:** `model/VisionExtractionResult.kt`

```kotlin
data class VisionExtractionResult(
    val extractedText: String,
    val source: VisionModel,
    val durationMs: Long,
    val error: String? = null
)
```

---

## Phase 5 — GeminiNanoExtractor

**New file:** `vision/GeminiNanoExtractor.kt`

Uses ML Kit GenAI image description API backed by AICore. No model file management needed.

Key implementation details:

- Check device availability first via `ImageDescriber.isSupported(context)` before attempting any inference — if false, `VisionExtractorFactory` should never have created this instance, but guard anyway
- Create `ImageDescriber` client using ML Kit GenAI API
- Convert `Bitmap` to `InputImage`
- Call `imageDescriber.describe(inputImage, options)` — this returns a description of the image
- The raw description is then passed through a focused football-specific follow-up prompt to structure it

**Two-step approach for Gemini Nano** (because the GenAI image description API returns a general description, not structured data):

Step 1 — Get image description via `ImageDescriber`
Step 2 — Pass description text to Gemini Nano's text `Prompt API` with the football structuring prompt

This is necessary because Gemini Nano via AICore does not currently support raw image + custom prompt in one call — the image description API is the only vision entry point.

**Structuring prompt for Step 2:**
```
This is a description of a football screenshot. Extract structured data only.

Description: {imageDescription}

Return in this format (omit fields not present):
Match: [home] vs [away]
Score: [n-n]
Competition: [name]
Round/Date: [value]
Scorers: [player] [minute]
Player: [name] | Goals:[n] Assists:[n] Rating:[n]
Other: [any relevant stats]
```

- Handle `ErrorCode.BUSY` with exponential backoff (AICore enforces per-app quota)
- Handle `ErrorCode.BACKGROUND_USE_BLOCKED` — should not occur since OCR sheet is foreground, but guard anyway
- `release()` closes both `ImageDescriber` and text inference client

**Availability check helper** (used by factory and Settings UI):
```kotlin
fun isAvailable(context: Context): Boolean =
    ImageDescriber.isSupported(context)
```

---

## Phase 6 — PaliGemmaExtractor

**New file:** `vision/PaliGemmaExtractor.kt`

Uses MediaPipe Tasks Vision API. Accepts `Bitmap` directly — single inference step, no two-step workaround needed.

- Lazily create and cache `ImageTextLlmInference` from local model file path
- Use `Mutex` for thread safety
- Downscale bitmap to max 896x896 before inference (PaliGemma native resolution)
- Prefer GPU backend, fall back to CPU automatically
- `release()` closes client and nulls cached instance

**Prompt** (PaliGemma format — no turn markers needed):
```
Extract all football statistics from this image. Return structured labels only:

Match: [home] vs [away]
Score: [n-n]
Competition: [name]
Round/Date: [value]
Scorers: [player] [minute]
Player: [name] | Goals:[n] Assists:[n] Rating:[n]
Other: [any relevant stats]
```

---

## Phase 7 — Gemma3VisionExtractor

**New file:** `vision/Gemma3VisionExtractor.kt`

Uses MediaPipe LLM Inference API with image session. Same structure as PaliGemmaExtractor with differences:

- Max bitmap size: 1024x1024
- Uses Gemma turn-marker prompt format (`<start_of_turn>user` / `<end_of_turn>`)
- `<image>` token embedded in prompt where image is injected
- Higher `maxTokens` (768) — Gemma 3 4B is more verbose

---

## Phase 8 — MlKitFallbackExtractor

**New file:** `vision/MlKitFallbackExtractor.kt`

Wraps existing `OcrUtils` and `FootballOcrParser` behind `IVisionExtractor`. Zero new logic.

- `model` returns `VisionModel.ML_KIT`
- `extractFromImage(bitmap)` calls `OcrUtils.extractTextFromBitmap(bitmap)` then `FootballOcrParser.formatForPrompt(text)`
- `release()` is a no-op

---

## Phase 9 — VisionModelManager

**New file:** `utils/VisionModelManager.kt`

Manages MediaPipe model files only. Gemini Nano is excluded — AICore manages its own model lifecycle.

- Handles `PALIGEMMA_2_3B` and `GEMMA_3_4B` entries only
- `isModelAvailable(model: VisionModel): Boolean`
- `getModelPath(model: VisionModel): String` — absolute path in `context.filesDir/models/`
- `suspend fun downloadModel(model: VisionModel, onProgress: (Float) -> Unit): Result<Unit>` — uses existing OkHttp 4.12.0, streams to disk
- `fun deleteModel(model: VisionModel)`
- `fun getInstalledModels(): List<VisionModel>`
- `fun getStorageUsedMb(model: VisionModel): Long`

---

## Phase 10 — VisionExtractorFactory

**New file:** `vision/VisionExtractorFactory.kt`

Mirrors `CuratorFactory.kt`. Selects the correct implementation.

Logic in `create()`:

```
If user has pinned a specific model in preferences:
    → Use that model's extractor if available, else fall through

Auto-selection priority:
1. GeminiNanoExtractor   — if GeminiNanoExtractor.isAvailable(context)
2. PaliGemmaExtractor    — if VisionModelManager.isModelAvailable(PALIGEMMA_2_3B)
3. Gemma3VisionExtractor — if VisionModelManager.isModelAvailable(GEMMA_3_4B)
4. MlKitFallbackExtractor — always available
```

---

## Phase 11 — Update OcrViewModel

**File:** `viewmodel/OcrViewModel.kt` — extend existing, restructure processing logic

Updated `OcrUiState`:
```kotlin
data class OcrUiState(
    val selectedBitmap: Bitmap? = null,
    val isExtracting: Boolean = false,
    val editableText: String = "",
    val extractionResult: VisionExtractionResult? = null,
    val isModelDownloading: Boolean = false,
    val modelDownloadProgress: Float = 0f,
    val activeExtractorModel: VisionModel = VisionModel.ML_KIT,
    val geminiNanoAvailable: Boolean = false,
    val installedMediaPipeModels: List<VisionModel> = emptyList(),
    val error: String? = null
)
```

Updated `onImageSelected(bitmap: Bitmap)`:
```
1. Set isExtracting = true
2. Build extractor via VisionExtractorFactory
3. Set activeExtractorModel = extractor.model (drives loading label in UI)
4. Call extractor.extractFromImage(bitmap)
5. On success: set editableText, extractionResult
6. On failure: if not already ML Kit, retry silently with MlKitFallbackExtractor
7. Set isExtracting = false
8. Call extractor.release()
```

Add:
- `init {}` — checks `GeminiNanoExtractor.isAvailable()`, loads installed MediaPipe models, loads user preference
- `onModelPinned(model: VisionModel)` — saves preference, updates factory selection
- `onRequestDownload(model: VisionModel)` — delegates to VisionModelManager
- `onDeleteModel(model: VisionModel)` — delegates to VisionModelManager
- Override `onCleared()` — release any held extractor instance

---

## Phase 12 — Update OcrInputSheet

**File:** `ui/components/OcrInputSheet.kt` — targeted additions only

**1. Unified loading label**
`EnhancedLoadingCard` label driven by `activeExtractorModel`:
- `GEMINI_NANO` → "Analysing with Gemini Nano…"
- `PALIGEMMA_2_3B` → "Analysing with PaliGemma…"
- `GEMMA_3_4B` → "Analysing with Gemma 3…"
- `ML_KIT` → "Reading image…"

**2. Extraction source badge**
`NeoChip` below `NeoInput` driven by `extractionResult?.source`:
- `GEMINI_NANO` → "✓ Gemini Nano" (primary accent, no download badge needed)
- `PALIGEMMA_2_3B` → "✓ PaliGemma Vision" (primary accent)
- `GEMMA_3_4B` → "✓ Gemma 3 Vision" (primary accent)
- `ML_KIT` → "OCR Fallback" (muted)

**3. VisionModelDownloadCard**
Shown when no MediaPipe model is downloaded AND Gemini Nano is not available. See Phase 13.

---

## Phase 13 — VisionModelDownloadCard

**New file:** `ui/components/VisionModelDownloadCard.kt`

Shown only when both of the following are true:
- `geminiNanoAvailable == false` (device doesn't support AICore)
- No MediaPipe model is downloaded

Content:
- Headline: "Upgrade to Vision AI"
- Gemini Nano status row: "Gemini Nano · Not supported on this device" (greyed out) — shown so user understands why it's not being offered
- Model selector: two `NeoChip` options
  - "PaliGemma 2 3B · ~1.5GB — Mid-range friendly"
  - "Gemma 3 4B · ~2.0GB — Flagship"
- "Download Selected" `NeoButton`
- "Use OCR Instead" text button — pins preference to `ML_KIT`, card never shown again
- Download progress: linear bar with percentage + MB downloaded
- On complete: card dismisses, extraction source badge updates

If `geminiNanoAvailable == true`, this card is never shown — Gemini Nano activates automatically with no download prompt needed.

---

## Phase 14 — PromptManager Update

**File:** `prompts/PromptManager.kt` — add one function

```kotlin
// Already exists
fun buildPromptFromOcr(rawText: String, tone: String, hashtags: String): String

// New — for all vision model outputs (structured labelled text)
fun buildPromptFromVisionExtraction(structuredText: String, tone: String, hashtags: String): String
```

In `OcrViewModel.onConfirmText()`:
- `VisionModel.ML_KIT` → `buildPromptFromOcr()`
- anything else → `buildPromptFromVisionExtraction()`

---

## Phase 15 — Settings Integration

**File:** `SettingsActivity.kt` — new "Vision AI" section

**Gemini Nano row**
- Status: "Gemini Nano · Ready (no download needed)" or "Gemini Nano · Not supported on this device"
- No download button — managed entirely by Google/AICore
- If supported: shows as the recommended default with a "Recommended" badge

**MediaPipe Models subsection** (shown below Gemini Nano row)
- One row per supported model (PaliGemma 2 3B, Gemma 3 4B)
- Status badge per model: "Not Downloaded" / "Downloading…" / "Ready · Xmb used"
- Download / Delete button per model
- RAM recommendation caption

**Preferred extraction selector**
- Radio-style `NeoChip` group: Auto / Gemini Nano / PaliGemma / Gemma 3 / ML Kit OCR
- "Auto" is default — uses priority order defined in VisionExtractorFactory
- Greyed out options for models not yet downloaded or not supported on device

---

## Files Summary

| Action | File |
|--------|------|
| Modify | `app/build.gradle` |
| Create | `vision/IVisionExtractor.kt` |
| Create | `vision/VisionModel.kt` |
| Create | `vision/VisionExtractorFactory.kt` |
| Create | `vision/GeminiNanoExtractor.kt` |
| Create | `vision/PaliGemmaExtractor.kt` |
| Create | `vision/Gemma3VisionExtractor.kt` |
| Create | `vision/MlKitFallbackExtractor.kt` |
| Create | `model/VisionExtractionResult.kt` |
| Create | `utils/VisionModelManager.kt` |
| Create | `ui/components/VisionModelDownloadCard.kt` |
| Modify | `viewmodel/OcrViewModel.kt` |
| Modify | `ui/components/OcrInputSheet.kt` |
| Modify | `prompts/PromptManager.kt` |
| Modify | `SettingsActivity.kt` |

**Completely untouched:**
- `utils/OcrUtils.kt` — called only inside `MlKitFallbackExtractor`
- `utils/FootballOcrParser.kt` — called only inside `MlKitFallbackExtractor`
- `MainActivity.kt`
- All curator / service / API layers

---

## Graceful Degradation

| Scenario | Behaviour |
|---|---|
| Gemini Nano supported, no MediaPipe downloaded | Auto-selects Gemini Nano, no download prompt shown |
| Gemini Nano not supported, no MediaPipe downloaded | Falls back to ML Kit, VisionModelDownloadCard offered |
| Gemini Nano BUSY error (quota exceeded) | Retry with backoff; if persistent, fall back to next available path |
| Gemini Nano background blocked | Should not occur — guard defensively, fall back silently |
| MediaPipe inference fails | Silent retry with MlKitFallbackExtractor |
| Inference timeout >20s | Abort, fall back to ML Kit, show "Took too long — used OCR instead" |
| Both vision and ML Kit fail | Show error with retry button |
| Model file deleted mid-session | `isModelAvailable()` check in factory catches it, falls back |
| Low storage during MediaPipe download | Catch IOException, show "Not enough storage" in download card |

---

## Notes for Code Assistant

- `GeminiNanoExtractor` is a two-step process (image description → text structuring) because AICore's image API returns a general description, not a custom-prompted output — this is a current API limitation
- Handle `ErrorCode.BUSY` in `GeminiNanoExtractor` with exponential backoff — AICore enforces per-app inference quotas
- `GeminiNanoExtractor.isAvailable(context)` must be called before creating the instance — never instantiate it on unsupported devices
- `VisionModelManager` manages only PaliGemma and Gemma 3 files — never reference Gemini Nano in VisionModelManager
- `MlKitFallbackExtractor` wraps existing `OcrUtils` and `FootballOcrParser` as-is — no logic moves or duplicates
- `VisionModel.ML_KIT` is a valid pinned preference value, not null — treat it as a first-class choice
- Bitmap downscaling happens inside each extractor, not in ViewModel — each model has its own optimal resolution
- Store preferred model as the enum `id` string in PreferencesManager, not ordinal — ordinal breaks if enum order changes
- `VisionModelDownloadCard` is shown only when Gemini Nano is unavailable AND no MediaPipe model exists — if Gemini Nano is available, no download is ever needed and the card must never appear
- Test `GeminiNanoExtractor` on a supported device (Pixel 9, Galaxy S25) and `MlKitFallbackExtractor` on a low-end device — these are the two most common real-world paths for Malaysian users
