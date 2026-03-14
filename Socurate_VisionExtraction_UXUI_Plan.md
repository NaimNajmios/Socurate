# UX/UI Implementation Plan — Vision Extraction Feature
## Socurate · Jetpack-Compose-Migration Branch

---

## Design Foundation

All new UI strictly follows the existing Neo-Editorial design system. No new design language is introduced. Every component, spacing decision, colour choice, and motion behaviour references what already exists in the app.

### Neo-Editorial Reference

| Token | Value | Usage |
|-------|-------|-------|
| Primary accent (Light/Dark) | `#FF4500` International Orange | Active states, primary buttons, selected chips |
| Primary accent (Deep Blue) | `#3B82F6` Electric Blue | Same role in Deep Blue theme |
| Background (Dark) | `#000000` | Sheet backgrounds |
| Surface (Dark) | `#121212` | Card surfaces |
| Border | 1dp, subtle grey | NeoCard borders |
| Corner radius | 0dp (sharp) | All cards and chips — Neo-Editorial has zero rounding |
| Typography | Monospace for input, sans-serif for labels | Matches existing NeoInput and body text |
| Haptic | Light tap on selection, medium on confirm | Matches HapticHelper usage throughout app |

### Motion Reference (Existing System)

| Animation | Spec | When to reuse |
|-----------|------|---------------|
| Spring entrance | `spring(dampingRatio = MediumBouncy)` | Cards entering view |
| Stagger | 30ms delay per item | Chip rows appearing |
| Color transition | `animateColorAsState`, 200ms tween | Chip selection state changes |
| Typewriter | 1ms delay, 3 chars/step | Extracted text appearing in field |

---

## Screen Inventory

This plan covers UX/UI for six distinct surfaces:

1. **MainActivity** — entry point addition ("From Screenshot" trigger)
2. **OcrInputSheet** — primary interaction surface (image selection → extraction → confirm)
3. **VisionModelDownloadCard** — upgrade prompt within the sheet
4. **Download Progress State** — inline within the sheet during model download
5. **SettingsActivity** — new Vision AI section
6. **Error & Edge Case States** — all failure scenarios throughout

---

## 1. MainActivity Entry Point

### Placement

The "From Screenshot" trigger lives in the input area, below the existing `NeoInput` field. It is a secondary action — never competing visually with the primary "Generate Post" FAB.

### Component: Screenshot Entry Chip

A `NeoChip` with an image/camera icon and label "From Screenshot". Styled identically to the existing refinement chips — rectangular, sharp corners, 1dp border.

**Default state:**
- Border: subtle grey (same as unselected `NeoChip`)
- Icon: outlined image frame icon (Material Icons `image` or `add_photo_alternate`)
- Label: "From Screenshot"
- Background: transparent

**Pressed state:**
- Brief scale-down to 0.96f via `animateFloatAsState`
- Haptic: light tick via `HapticHelper`

**Placement within layout:**
```
[ NeoInput — main text field                    ]
[ From Screenshot chip ]   [ other existing chips ]
[ Generate Post FAB                             ]
```

The chip sits in the same horizontal chip row as other input-area chips. It does not get its own dedicated row — it is one of many chips.

### Transition to Sheet

On tap, `OcrInputSheet` slides up as a `ModalBottomSheet`. Entrance animation follows the existing `ShareBottomSheetFragment` pattern — sheet slides up from bottom with a subtle spring.

---

## 2. OcrInputSheet — Complete State Machine

The sheet has seven distinct states. Each state maps to a specific UI layout. States are mutually exclusive — only one is shown at a time in the content area, while the header and confirm button persist across all states.

### Persistent Elements (Always Visible)

**Sheet header row:**
- Left: "Import from Screenshot" label in body text weight
- Right: close icon button (`Icons.Default.Close`)
- Separator: 1dp horizontal divider below header, same border colour as `NeoCard`

**Image source selector row** (always visible after initial state):
- Two `NeoChip` options side by side: "Gallery" and "Camera"
- Selected chip gets primary accent border and background tint (matches existing `NeoChip` selection pattern)
- Camera chip hidden entirely (not greyed) if device has no camera hardware — check `PackageManager.hasSystemFeature(FEATURE_CAMERA_ANY)` before rendering

**Confirm button** (always visible, state-dependent enabled/disabled):
- `NeoButton` labelled "Use This Text"
- Disabled (40% opacity, no haptic) when: no text extracted yet, extraction in progress, text field is empty
- Enabled when: text is present and no loading is active
- On tap: medium haptic, sheet dismisses, text flows to main input

---

### State 1 — Idle (No Image Selected)

Shown immediately when sheet opens.

**Content area:**
- Large dashed-border rectangle (`NeoCard` with dashed border style) acting as image drop zone
- Centred inside: image icon (48dp), caption "Tap Gallery or Camera above to select a screenshot"
- Caption colour: muted (50% opacity on surface text colour)
- Height: 180dp fixed

**Entrance animation:**
- Sheet slides up with spring
- Drop zone fades in with 150ms alpha animation after sheet settles

**VisionModelDownloadCard position:**
- Shown below the drop zone if applicable (see Section 5)
- Separated by 12dp vertical spacing

---

### State 2 — Image Selected (Pre-Extraction)

Shown immediately after image is picked, before extraction begins.

**Content area:**
- Image thumbnail in a `NeoCard` container, 16:9 aspect ratio, fills card width
- Sharp corners — no rounding, consistent with Neo-Editorial
- Below thumbnail: two-column row
  - Left: file name or "Screenshot" label in caption size
  - Right: "Change" text button — tapping returns to State 1

**Transition from State 1:**
- Drop zone fades out (100ms alpha)
- Thumbnail card springs in from below (spring entrance)
- Extraction begins automatically on image selection — no separate "Extract" button needed

---

### State 3 — Extracting (Loading)

Shown while any extraction path is running (Gemini Nano, PaliGemma, Gemma 3, or ML Kit).

**Content area:**
- Thumbnail card remains visible at top (user can see what's being analysed)
- Below thumbnail: `EnhancedLoadingCard` (reuse existing component)
- Loading label inside `EnhancedLoadingCard` is dynamic based on `activeExtractorModel`:

| Model | Loading Label |
|-------|---------------|
| `GEMINI_NANO` | "Analysing with Gemini Nano…" |
| `PALIGEMMA_2_3B` | "Analysing with PaliGemma…" |
| `GEMMA_3_4B` | "Analysing with Gemma 3…" |
| `ML_KIT` | "Reading image…" |

- For Gemini Nano two-step process, label changes mid-extraction:
  - Step 1: "Describing image…"
  - Step 2: "Structuring data…"
  - Both steps use the same `EnhancedLoadingCard` with label update via `animateContentSize`

**Sub-label** (small caption below loading label):
- `GEMINI_NANO` → "On-device · No data leaves your phone"
- `PALIGEMMA_2_3B` / `GEMMA_3_4B` → "On-device · Fully offline"
- `ML_KIT` → "On-device · Text extraction only"

**Confirm button:** disabled during this state.

---

### State 4 — Extraction Complete

Shown after successful extraction.

**Content area layout (top to bottom):**

**Thumbnail row:**
- Thumbnail card shrinks to a compact 72dp tall strip (not full height)
- Transition: `animateContentSize` with tween 300ms
- "Change" button remains accessible at right edge of strip

**Source badge row:**
- Horizontal row of chips appearing with stagger entrance (30ms delay each)
- First chip: extraction source badge

| Source | Badge Label | Badge Style |
|--------|-------------|-------------|
| `GEMINI_NANO` | "✓ Gemini Nano" | Primary accent border + tint |
| `PALIGEMMA_2_3B` | "✓ PaliGemma Vision" | Primary accent border + tint |
| `GEMMA_3_4B` | "✓ Gemma 3 Vision" | Primary accent border + tint |
| `ML_KIT` | "OCR Fallback" | Muted border, no tint |

- Second chip (Vision models only): inference duration — "Extracted in 4.2s" in muted style
- These chips are informational only, not tappable

**Extracted text field:**
- `NeoInput` with label "Extracted Data"
- Text appears via typewriter effect (reuse existing `TypewriterText` animation logic — 3 chars per step, 1ms delay)
- Field is fully editable — user can correct errors
- Monospace font (matches existing `NeoInput` style)
- Minimum height: 120dp, expands with content
- Character count shown at bottom right in caption size

**Confirm button:** becomes enabled once text is non-empty and typewriter finishes.

---

### State 5 — Fallback Notification

Shown when extraction silently fell back to ML Kit after a vision model failed.

This is a non-blocking informational state — it does not replace State 4, it augments it.

**Additional element in State 4 layout:**
- A `NeoCard` with warning-style border (amber/yellow accent, 1dp) appears above the text field
- Content: icon (info outline) + "Vision extraction fell back to OCR. You may want to review the text below."
- Dismiss button: × at card right edge
- Card slides in with spring entrance after text field appears
- Does not block editing or confirmation

---

### State 6 — Full Error

Shown when both vision extraction and ML Kit fallback fail completely.

**Content area:**
- Thumbnail card remains (shows what was attempted)
- Error card below thumbnail:
  - `NeoCard` with primary accent border (matches error handling elsewhere in app)
  - Large icon: `Icons.Default.ErrorOutline`
  - Title: "Extraction Failed"
  - Body: specific message based on error type:
    - No text found: "No text was detected in this image. Try a clearer screenshot."
    - Timeout: "Extraction took too long. Try again or use a simpler image."
    - Generic: "Something went wrong. Please try again."
  - Single `NeoButton`: "Try Again" — resets to State 2 with same image still selected
  - Text link below button: "Select a different image" — resets to State 1

**Confirm button:** disabled.

---

### State 7 — Model Downloading (Inline)

Shown when user initiates a model download from the `VisionModelDownloadCard` inside the sheet. The sheet does not close during download.

**Content area:**
- `VisionModelDownloadCard` transitions to its downloading sub-state (see Section 5)
- Image source chips remain accessible — user can still pick an image
- If image is already selected: thumbnail stays visible above the card
- Extraction with ML Kit can still run during download — the fallback path remains available

---

## 3. VisionModelDownloadCard

Shown inside `OcrInputSheet` below the drop zone / thumbnail. Visible only when:
- `geminiNanoAvailable == false`
- No MediaPipe model is downloaded
- User has not permanently dismissed with "Use OCR Instead"

### Default Sub-state

```
┌─────────────────────────────────────────────┐
│  UPGRADE TO VISION AI                        │
│                                              │
│  Extract player stats and match data         │
│  directly from screenshots — no OCR          │
│  step needed. One-time download,             │
│  fully offline after.                        │
│                                              │
│  ○ Gemini Nano    Not supported on device    │
│                                              │
│  Select a model to download:                 │
│  ┌──────────────────┐ ┌──────────────────┐  │
│  │ PaliGemma 2 3B   │ │  Gemma 3 4B      │  │
│  │ ~1.5GB           │ │  ~2.0GB          │  │
│  │ Mid-range        │ │  Flagship        │  │
│  └──────────────────┘ └──────────────────┘  │
│                                              │
│  [ Download Selected Model    ]              │
│           Use OCR Instead                    │
└─────────────────────────────────────────────┘
```

**Card behaviour details:**

- Card entrance: springs in from below when sheet opens, 200ms delay after sheet settles
- Gemini Nano row: greyed out (40% opacity), no interaction — purely informational so user understands why it's absent
- Model chips: `NeoChip` selection pattern — one selected at a time, primary accent on selected
- Default selected chip: PaliGemma 2 3B (smaller, safer default for Malaysian mid-range devices)
- "Download Selected Model" `NeoButton`: disabled until a chip is selected (pre-selected by default, so effectively always enabled on render)
- "Use OCR Instead": plain text style, no border, positioned below the button with 8dp spacing

**RAM warning** (conditional):
- If device RAM < 6GB and PaliGemma is selected: small amber caption appears below PaliGemma chip — "May be slow on this device"
- If device RAM < 8GB and Gemma 3 is selected: same warning — "Recommended for 8GB+ devices"
- Warning animates in/out with `AnimatedVisibility` when chip selection changes

### Downloading Sub-state

Triggered when "Download Selected Model" is tapped.

```
┌─────────────────────────────────────────────┐
│  Downloading PaliGemma 2 3B…                 │
│                                              │
│  ████████████░░░░░░░░░░░░  62%              │
│  934 MB of 1,510 MB · ~3 min remaining      │
│                                              │
│  [ Cancel ]                                  │
└─────────────────────────────────────────────┘
```

**Progress bar:** custom linear progress using Canvas (consistent with existing chart components). Primary accent fill colour on surface background. Animated fill width via `animateFloatAsState`.

**Stats row:** progress percentage (bold), MB downloaded of total, estimated time remaining. Update every second.

**Cancel button:** secondary `NeoButton` style. On tap: cancels download coroutine, partial file deleted, card returns to default sub-state.

**WiFi detection:** if user is on mobile data, insert a warning row before the progress bar — "Downloading on mobile data · ~150MB remaining budget". One-time warning, dismissible.

### Complete Sub-state

Shown for 2 seconds after download finishes, then card auto-dismisses.

```
┌─────────────────────────────────────────────┐
│  ✓ PaliGemma 2 3B Ready                     │
│  Vision extraction is now active             │
└─────────────────────────────────────────────┘
```

- `AnimatedCheckmark` component (reuse existing) at left
- Card shrinks height with `animateContentSize` then fades out
- If an image was already selected in the sheet: extraction restarts automatically with new model

---

## 4. Extraction Source Badge — Detailed Spec

The `NeoChip` badge below the `NeoInput` label deserves its own spec because it communicates trust and transparency.

### Tappable Behaviour

The badge is tappable. On tap, a small tooltip `NeoCard` appears below it (like a popover) with more detail:

| Source | Tooltip Content |
|--------|-----------------|
| `GEMINI_NANO` | "Extracted using Gemini Nano running on your device via Google AICore. No data sent to any server." |
| `PALIGEMMA_2_3B` | "Extracted using PaliGemma 2 3B running fully offline on your device. Extracted in Xs." |
| `GEMMA_3_4B` | "Extracted using Gemma 3 4B running fully offline on your device. Extracted in Xs." |
| `ML_KIT` | "Extracted using ML Kit OCR. Text only — no AI interpretation. For better results, download a Vision AI model in Settings." |

Tooltip appears with fade + slight downward translate (150ms). Tapping anywhere else dismisses it.

### "OCR Fallback" Badge Additional Behaviour

When `ML_KIT` badge is shown after a silent fallback (vision model was attempted but failed), the badge gets an additional amber dot indicator at its top-right corner — a small 6dp circle in amber, no animation. This distinguishes "deliberately using OCR" from "fell back to OCR after failure". The tooltip text changes accordingly: "Vision extraction encountered an issue and fell back to OCR automatically."

---

## 5. Settings — Vision AI Section

### Section Placement

Inserted as a new section in `SettingsActivity` between the existing "AI Provider Setup" section and "Post Settings" section. Section header: "Vision AI" in the same section header style used throughout.

### Gemini Nano Row

```
Vision AI
─────────────────────────────────────────
Gemini Nano
On-device vision · No download needed
                              [ READY ✓ ]
─────────────────────────────────────────
```

**If supported:**
- Status badge: "READY ✓" in small caps, primary accent colour
- No button — managed by Google
- Subtitle: "On-device vision · No download needed"
- Row is not tappable — informational only

**If not supported:**
- Status badge: "NOT SUPPORTED" in small caps, muted colour
- Subtitle: "Not available on this device"
- Row is not tappable

### MediaPipe Model Rows

One row per supported model. Each row is a `NeoCard` with internal layout:

```
┌─────────────────────────────────────────────┐
│ PaliGemma 2 3B                  [ DOWNLOAD ] │
│ ~1.5GB · Mid-range friendly                  │
│ Recommended for 6GB+ RAM                     │
└─────────────────────────────────────────────┘
```

**Not downloaded state:**
- Status: no badge
- Button: `NeoButton` secondary style "Download"
- On tap: triggers download with progress inline (card expands)

**Downloading state:**
- Card expands with `animateContentSize`
- Progress bar replaces download button
- "Cancel" text button appears

**Downloaded state:**
```
┌─────────────────────────────────────────────┐
│ PaliGemma 2 3B            [ READY · 1.5GB ] │
│ Mid-range friendly                [ DELETE ] │
└─────────────────────────────────────────────┘
```
- Status badge: "READY · XGB" in primary accent small caps
- "Delete" text button — tapping shows confirmation dialog before deleting

**Delete confirmation dialog:**
- `AlertDialog` styled with `NeoCard` borders (consistent with existing dialogs in app)
- Title: "Delete PaliGemma 2 3B?"
- Body: "This will free 1.5GB of storage. You can re-download it later."
- Buttons: "Cancel" and "Delete" (delete in primary accent colour)

### Preferred Extraction Selector

Horizontal scrollable row of `NeoChip` options below the model rows:

```
Preferred extraction method:
[ Auto ] [ Gemini Nano ] [ PaliGemma ] [ Gemma 3 ] [ OCR Only ]
```

- "Auto" selected by default
- Greyed chips (40% opacity, not tappable) for:
  - Gemini Nano if not supported on device
  - PaliGemma / Gemma 3 if not yet downloaded
- Caption below selector: describes what "Auto" means — "Automatically uses the best available method"
- Selecting a specific model: caption updates to explain the pinned choice

### Storage Summary Row

At the bottom of the Vision AI section:

```
Vision AI Storage Used: 1.5 GB / 64 GB available
```

Plain caption row. Updates live as models are downloaded or deleted.

---

## 6. Edge Cases and Error States — Complete Spec

### 6.1 First Launch of Sheet — Gemini Nano Available

User opens sheet for the first time on a supported device.

- Sheet opens normally
- No download card shown
- Source badges not yet visible (no extraction run yet)
- Idle state shown with drop zone
- No onboarding tooltip or coach mark needed — the UI is self-explanatory

### 6.2 First Launch of Sheet — No Vision Model, No Gemini Nano

- Sheet opens
- Idle state shown
- `VisionModelDownloadCard` springs in below drop zone
- User can immediately pick an image — ML Kit will run while card is visible
- Card is offered, not forced — user can ignore it and proceed with OCR

### 6.3 Image Selected But Extraction Is Very Slow

For MediaPipe models on slow devices, inference can take 15–25 seconds. The UX must not feel frozen.

- After 5 seconds in loading state, add a sub-caption below the loading label: "This may take a moment on this device…"
- Sub-caption fades in with `AnimatedVisibility`, does not flash or pulse
- After 20 seconds, automatic timeout: abort inference, silently fall back to ML Kit, show State 5 (fallback notification)
- The 20 second threshold is defined as a constant in `GemmaVisionUtils` — not hardcoded in UI

### 6.4 Gemini Nano Quota Exceeded (BUSY Error)

- Loading state shows normally
- On BUSY error: first retry silently (up to 3 retries with 1s backoff)
- If all retries fail: fall back to next available path (MediaPipe or ML Kit)
- Fallback notification (State 5) shown — user sees result but knows it came from fallback
- No error dialog shown — silent degradation

### 6.5 No Text in Image

When extraction completes but returns empty string.

- Do not enter State 4
- Enter State 6 (error) with message: "No text was detected. Try a clearer screenshot or one with more visible text."
- Thumbnail still shown
- "Try Again" retries with same image
- "Select a different image" resets to State 1

### 6.6 User Picks Non-Football Image

Extraction will still run and return whatever text or description it finds. The app does not validate content — it's the user's responsibility. The editable text field will show the (likely irrelevant) output. User can clear it manually or pick a different image. No warning is shown.

### 6.7 Camera Permission Denied

- If user taps "Camera" chip and permission is denied:
  - Do not show system permission dialog again if permanently denied
  - Show a small inline message below the chip row: "Camera access denied. Enable in Settings." with a "Open Settings" text link
  - Message uses `AnimatedVisibility` to slide in
  - Gallery chip remains fully functional

### 6.8 Storage Full During Download

- Download progress bar turns amber
- Below progress bar: "Not enough storage to complete download. Free up space and try again."
- Progress bar stops updating
- "Resume" button replaces "Cancel" — resume attempts download from byte offset if server supports range requests; otherwise restarts
- Partial file is kept until user cancels or resumes

### 6.9 Download Interrupted (Network Loss)

- Progress bar pauses, label changes to "Connection lost — waiting to resume…"
- Retry automatically when connectivity returns (observe `ConnectivityManager`)
- If no connectivity after 30s: show "Download paused. Tap to retry." with "Retry" button
- Partial file preserved between app sessions

### 6.10 Model Deleted While Sheet Is Open

If the user deletes a model from Settings while `OcrInputSheet` is open in another task:

- `OcrViewModel` re-checks model availability on `onResume` equivalent
- If active model no longer available, `activeExtractorModel` falls back to next available
- If an extraction result is already shown, existing text is preserved
- Next extraction uses new active model — source badge updates accordingly

---

## 7. Animation Choreography — Full Sheet Lifecycle

The following describes the complete animation sequence from sheet open to confirmed text, for the happy path with a vision model available.

```
t=0ms     Sheet slides up (spring, MediumBouncy)
t=150ms   Header row fades in (alpha 0→1, 100ms)
t=200ms   Source chips row fades + translates in (spring)
t=250ms   Drop zone fades in (alpha 0→1, 150ms)

[User picks image]

t=0ms     Drop zone fades out (100ms)
t=100ms   Thumbnail card springs in (spring entrance, translateY 40→0)
t=200ms   Loading card fades in below thumbnail

[Extraction completes]

t=0ms     Loading card fades out (100ms)
t=100ms   Thumbnail shrinks to compact strip (animateContentSize, 300ms tween)
t=150ms   Source badge chip springs in (spring, stagger if multiple chips)
t=250ms   NeoInput field slides in from below (translateY 20→0, spring)
t=350ms   Typewriter effect begins on extracted text (3 chars/step, 1ms delay)
t=Xms     Typewriter completes
t=X+50ms  Confirm button transitions from disabled to enabled
          (colour animates from muted to primary accent, 200ms tween)
```

---

## 8. Accessibility

- All interactive elements have `contentDescription` for TalkBack
- Loading states announce themselves via `LiveRegion` semantics — `Role.LIVE_REGION` on the loading label composable so TalkBack reads "Analysing with Gemini Nano" without user interaction
- Confirm button `contentDescription` updates dynamically: "Use This Text — disabled, waiting for extraction" vs "Use This Text — tap to use extracted content"
- Source badge tooltip accessible via `semantics { onClick(label = "Learn more about extraction source") }`
- Download card model selector chips use `Role.RadioButton` semantics
- Progress bar uses `ProgressBarRangeInfo` semantics for TalkBack percentage announcements
- Minimum touch target 48dp on all interactive elements — verified against existing `NeoChip` and `NeoButton` sizes

---

## 9. New Composable Files Summary

| File | Contents |
|------|----------|
| `ui/components/OcrInputSheet.kt` | Modified — all 7 states, persistent elements, animation choreography |
| `ui/components/VisionModelDownloadCard.kt` | New — default, downloading, complete sub-states |
| `ui/components/ExtractionSourceBadge.kt` | New — badge chip + tooltip popover |
| `ui/components/FallbackNotificationCard.kt` | New — State 5 amber warning card |

Settings changes are in `SettingsActivity.kt` — no new file needed, new section added inline.

---

## 10. String Resources

All user-facing strings must be added to `res/values/strings.xml`. Key strings:

```xml
<!-- Entry point -->
<string name="ocr_entry_chip_label">From Screenshot</string>

<!-- Sheet header -->
<string name="ocr_sheet_title">Import from Screenshot</string>
<string name="ocr_image_source_gallery">Gallery</string>
<string name="ocr_image_source_camera">Camera</string>
<string name="ocr_drop_zone_hint">Tap Gallery or Camera above to select a screenshot</string>

<!-- Loading labels -->
<string name="ocr_loading_gemini_nano">Analysing with Gemini Nano…</string>
<string name="ocr_loading_gemini_nano_step1">Describing image…</string>
<string name="ocr_loading_gemini_nano_step2">Structuring data…</string>
<string name="ocr_loading_paligemma">Analysing with PaliGemma…</string>
<string name="ocr_loading_gemma3">Analysing with Gemma 3…</string>
<string name="ocr_loading_mlkit">Reading image…</string>
<string name="ocr_loading_slow_hint">This may take a moment on this device…</string>

<!-- Sub-labels -->
<string name="ocr_sublabel_ondevice_nano">On-device · No data leaves your phone</string>
<string name="ocr_sublabel_ondevice_offline">On-device · Fully offline</string>
<string name="ocr_sublabel_mlkit">On-device · Text extraction only</string>

<!-- Source badges -->
<string name="ocr_badge_gemini_nano">✓ Gemini Nano</string>
<string name="ocr_badge_paligemma">✓ PaliGemma Vision</string>
<string name="ocr_badge_gemma3">✓ Gemma 3 Vision</string>
<string name="ocr_badge_mlkit_fallback">OCR Fallback</string>

<!-- Text field -->
<string name="ocr_field_label">Extracted Data</string>
<string name="ocr_confirm_button">Use This Text</string>
<string name="ocr_change_image">Change</string>

<!-- Fallback notification -->
<string name="ocr_fallback_notice">Vision extraction fell back to OCR. You may want to review the text below.</string>

<!-- Error states -->
<string name="ocr_error_title">Extraction Failed</string>
<string name="ocr_error_no_text">No text was detected in this image. Try a clearer screenshot.</string>
<string name="ocr_error_timeout">Extraction took too long. Try again or use a simpler image.</string>
<string name="ocr_error_generic">Something went wrong. Please try again.</string>
<string name="ocr_retry">Try Again</string>
<string name="ocr_select_different">Select a different image</string>

<!-- Download card -->
<string name="download_card_title">Upgrade to Vision AI</string>
<string name="download_card_body">Extract player stats and match data directly from screenshots — no OCR step needed. One-time download, fully offline after.</string>
<string name="download_card_size_wifi">~1.5GB · Recommended on WiFi</string>
<string name="download_card_gemini_unsupported">Gemini Nano · Not supported on this device</string>
<string name="download_card_paligemma_chip">PaliGemma 2 3B · ~1.5GB</string>
<string name="download_card_gemma3_chip">Gemma 3 4B · ~2.0GB</string>
<string name="download_card_midrange">Mid-range friendly</string>
<string name="download_card_flagship">Flagship</string>
<string name="download_card_button">Download Selected Model</string>
<string name="download_card_skip">Use OCR Instead</string>
<string name="download_card_ram_warning_mid">May be slow on this device</string>
<string name="download_card_ram_warning_high">Recommended for 8GB+ devices</string>
<string name="download_progress_label">Downloading %1$s…</string>
<string name="download_progress_mobile_data">Downloading on mobile data</string>
<string name="download_complete">%1$s Ready</string>
<string name="download_complete_subtitle">Vision extraction is now active</string>
<string name="download_cancel">Cancel</string>
<string name="download_storage_full">Not enough storage to complete download. Free up space and try again.</string>
<string name="download_paused_network">Download paused. Tap to retry.</string>

<!-- Settings -->
<string name="settings_section_vision_ai">Vision AI</string>
<string name="settings_nano_ready">On-device vision · No download needed</string>
<string name="settings_nano_unsupported">Not available on this device</string>
<string name="settings_model_delete">Delete</string>
<string name="settings_model_delete_title">Delete %1$s?</string>
<string name="settings_model_delete_body">This will free %1$s of storage. You can re-download it later.</string>
<string name="settings_extraction_method_label">Preferred extraction method</string>
<string name="settings_extraction_auto">Auto</string>
<string name="settings_extraction_auto_desc">Automatically uses the best available method</string>
<string name="settings_storage_used">Vision AI Storage Used: %1$s / %2$s available</string>

<!-- Camera permission -->
<string name="camera_permission_rationale">Camera access is needed to photograph screenshots directly. You can also use Gallery without this permission.</string>
<string name="camera_permission_denied_inline">Camera access denied. Enable in Settings.</string>
<string name="camera_permission_open_settings">Open Settings</string>
```
