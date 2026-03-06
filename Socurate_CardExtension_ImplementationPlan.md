# Socurate — Visual Post Card Extension
### Full Implementation Plan for Claude Code
**Android (Jetpack Compose + Kotlin) · Instagram / Facebook · March 2026**

---

## Overview

This document is a complete, Claude Code-ready implementation plan to extend Socurate with a **Visual Post Card Generator** — a new section of the app that transforms AI-generated Malaysian Malay football content into shareable Instagram/Facebook-optimised image cards.

The feature integrates natively into the existing Jetpack Compose architecture, reusing the current AI provider abstraction layer (Gemini / Groq / OpenRouter / Cerebras).

> **Core Goal:** User pastes article text or shares a URL → AI generates content (existing flow) → User taps **"Create Card"** → Picks a card template → Customises background/colors → Exports a 1080×1080px or 1080×1920px image ready for Instagram/Facebook.

> **Integration Strategy:** The card generator is a **NEW tab/section** alongside the existing generate flow. It does NOT replace any existing feature. All AI content from the main flow can be piped directly into the card generator with one tap.

---

## 01 · Feature Scope & Card Types

The extension introduces four card templates, each serving a distinct content type extracted from the article text by AI:

| Card Type | AI-Extracted Content | Recommended Format |
|---|---|---|
| **Match Result Card** | Score, teams, key stats (possession, shots) | Square 1:1 — best for feed posts |
| **Player Spotlight Card** | Player name, rating, goals, assists | Portrait 4:5 — works for feed & stories |
| **Headline / Quote Card** | AI-extracted key quote or headline | Square 1:1 or Story 9:16 |
| **Top 3 Stats Card** | Three standout stats as infographic | Square 1:1 — bold, shareable |

### Background Options

- **Solid gradient** — auto-generated from team colors extracted by AI
- **Gallery photo** — user picks any image from device gallery (blurred overlay applied)
- **Preset textures** — bundled in-app (grass, stadium blur, dark mesh)
- **Team color palette** — AI extracts team name, app maps to a known color seed

---

## 02 · New Package Structure

All new files live under the existing `com.najmi.oreamnos` package. No existing files are deleted — only additive changes plus minor wiring in `MainActivity` and the navigation graph.

### New Files to Create

| File / Package | Purpose |
|---|---|
| `cardgen/` | Root package for all card generation features |
| `cardgen/model/CardTemplate.kt` | Sealed class: `MatchResult \| PlayerSpotlight \| HeadlineQuote \| TopStats` |
| `cardgen/model/CardData.kt` | Data class holding AI-extracted structured JSON for card rendering |
| `cardgen/model/CardConfig.kt` | User config: template choice, background type, colors, font size toggle |
| `cardgen/prompt/CardPromptManager.kt` | Prompts that extract structured JSON from article text for each card type |
| `cardgen/extractor/CardDataExtractor.kt` | Calls AI provider, parses JSON response into `CardData` objects |
| `cardgen/renderer/CardRenderer.kt` | Composable → Bitmap pipeline using `ComposeView.drawToBitmap()` |
| `cardgen/renderer/CardCanvas.kt` | The actual Compose UI for each card template (Canvas + Compose) |
| `cardgen/ui/CardGeneratorScreen.kt` | Main screen: template picker, preview, background selector, export |
| `cardgen/ui/TemplatePickerRow.kt` | Horizontal `LazyRow` of card template thumbnails |
| `cardgen/ui/BackgroundPickerSheet.kt` | Bottom sheet: Gallery / Gradient / Preset selector |
| `cardgen/ui/CardPreviewPane.kt` | Live preview of the card with current config applied |
| `cardgen/ui/ExportBottomSheet.kt` | Export sheet: size selector (square/portrait), save/share buttons |
| `cardgen/viewmodel/CardGeneratorViewModel.kt` | State: `cardData`, `config`, `renderState`, `exportState` |
| `cardgen/utils/BitmapExporter.kt` | Saves bitmap to MediaStore (gallery) + triggers share intent |
| `cardgen/utils/ColorExtractor.kt` | Maps team name string to a Material color seed for gradients |
| `cardgen/utils/GradientBuilder.kt` | Creates `Brush` gradients from extracted team colors |

### Modifications to Existing Files

| File | Change Required |
|---|---|
| `MainActivity.kt` | Add `BottomNavigation` with 'Card' tab; wire `NavHost` destination |
| `navigation/AppNavGraph.kt` | Add `cardGeneratorScreen()` composable destination |
| `curator/IContentCurator.kt` | No change — `CardDataExtractor` reuses existing interface |
| `curator/CuratorFactory.kt` | No change needed |
| `prompts/PromptManager.kt` | Optional: add `generateCardPrompt()` here or keep in `CardPromptManager` |
| `ui/components/NeoButton.kt` | Add 'Create Card' action button to existing output card UI |
| `res/drawable/` | Add 3 preset background textures as drawable resources |

---

## 03 · AI Prompt Engineering

The card generator uses a **different prompt strategy** from the main content flow. Instead of generating prose, these prompts instruct the AI to return **ONLY a JSON object** that the app parses into `CardData`. The existing multi-provider abstraction (`GeminiCurator` / `OpenAICompatibleCurator`) handles the API calls.

### CardPromptManager.kt — Prompt Designs

**Match Result Card**
```
You are a football data extractor. Given the article text below, extract match result data
and return ONLY a valid JSON object with these exact keys:
homeTeam (string), awayTeam (string), homeScore (int), awayScore (int),
competition (string), matchDate (string),
homeStats (object: possession int, shots int, shotsOnTarget int),
awayStats (same),
keyMoment (string, max 80 chars in Bahasa Melayu).
Return nothing else — no markdown, no explanation.
```

**Player Spotlight Card**
```
You are a football data extractor. Extract player highlight data and return ONLY a JSON
object with: playerName (string), club (string), position (string), rating (float 0-10),
goals (int), assists (int),
keyQuote (string max 100 chars in Bahasa Melayu describing the performance).
Return nothing else.
```

**Headline / Quote Card**
```
Extract the single most impactful quote or headline from this article.
Return ONLY JSON: headline (string max 120 chars in Bahasa Melayu),
subtext (string max 60 chars), source (string, publication name).
```

**Top 3 Stats Card**
```
Extract the 3 most interesting statistics from this article.
Return ONLY JSON: stats (array of 3 objects, each with:
label string max 30 chars in Bahasa Melayu, value string max 10 chars,
context string max 50 chars).
```

### Error Handling Strategy

- Wrap `JSON.parse()` in try/catch — if parsing fails, show `'Could not extract card data'` snackbar
- Strip any markdown fences (` ```json `) before parsing — use existing `MarkdownUtils` pattern from codebase
- If a required field is null/missing, substitute a placeholder string so the card still renders
- Rate limit fallback: reuse the existing interactive rate limit recovery dialog — no new code needed

---

## 04 · Card Rendering System

The rendering pipeline has two stages: (1) a live Compose preview the user sees and interacts with, and (2) a high-resolution Bitmap export for saving/sharing. Both use the same `CardCanvas` composable, rendered at different sizes.

### CardCanvas.kt — Template Composables

| Composable | Layout Description |
|---|---|
| `MatchResultCanvas` | Two-column layout: home vs away. Score in large bold center. Stats row at bottom. Team color gradient background. |
| `PlayerSpotlightCanvas` | Full-bleed background image (player photo or gradient). Player name overlaid with stat chips. Rating badge top-right. |
| `HeadlineQuoteCanvas` | Large quote text centered, with decorative quotation mark (Canvas-drawn). Source credit bottom-right. |
| `TopStatsCanvas` | Three horizontal stat rows, each with label, giant value, and small context line. Alternating accent colors. |

### CardRenderer.kt — Bitmap Export Pipeline

```
1. Inflate CardCanvas in an offscreen ComposeView
2. Set layout params to export size (e.g. 1080×1080)
3. Call view.drawToBitmap()
4. Pass bitmap to BitmapExporter
5. Save to MediaStore via ContentResolver
6. Trigger share intent with FileProvider URI
```

### Export Sizes

| Size | Dimensions | Target Use |
|---|---|---|
| Square (1:1) | 1080 × 1080 px | Instagram feed, Facebook post |
| Portrait (4:5) | 1080 × 1350 px | Instagram feed portrait |
| Story (9:16) | 1080 × 1920 px | Instagram / Facebook Stories |

### Key Technical Notes for Claude Code

- Use `AndroidView { ComposeView(...) }` pattern for offscreen rendering — do NOT render to screen then capture
- Fonts: use existing `Typography` from `ui/theme/Type.kt` — do NOT embed new fonts unless needed
- Background images: load with Coil's `rememberAsyncImagePainter`, then apply a dark scrim (`Box` with `alpha 0.45f` black overlay) for text legibility
- Gradient: use `Brush.verticalGradient()` or `Brush.linearGradient()` with colors from `GradientBuilder.kt`
- Canvas drawings (decorative elements): use `DrawScope` inside `Canvas {}` composable — keep separate from layout composables
- Permission: `WRITE_EXTERNAL_STORAGE` not needed for API 29+; use `MediaStore.Images.Media.getContentUri("external")` directly

---

## 05 · ViewModel & State Management

### CardGeneratorViewModel.kt — State Properties

| Property | Purpose |
|---|---|
| `inputText: String` | Article text piped in from main flow or typed directly |
| `selectedTemplate: CardTemplate` | Which of the 4 card types is active |
| `cardData: CardData?` | Nullable — null until AI extraction completes |
| `cardConfig: CardConfig` | User's background, color, size choices |
| `extractionState: UiState` | `Loading \| Success \| Error` — drives UI feedback |
| `exportState: ExportState` | `Idle \| Exporting \| Saved \| Shared` — drives export sheet |
| `backgroundBitmap: Bitmap?` | Set when user picks a photo from gallery |

### Key Functions

- `extractCardData(template)` — calls `CardDataExtractor` with correct prompt, updates `extractionState`
- `updateConfig(config)` — updates `cardConfig`, triggers recomposition of `CardPreviewPane`
- `exportCard(size)` — triggers `CardRenderer`, then `BitmapExporter`, updates `exportState`
- `pipeFromMainFlow(text)` — called by `MainActivity` when user taps 'Create Card' on existing output

---

## 06 · UI Screen Breakdown

### CardGeneratorScreen.kt — Full Layout

The screen is divided into three vertical zones using a `Column` with a `Scaffold`:

1. **Top: `TemplatePickerRow`** — horizontal `LazyRow` of 4 card type cards, each with an icon and label. Tapping one sets `selectedTemplate` and triggers AI extraction if `inputText` is present.
2. **Middle: `CardPreviewPane`** — shows a live scaled preview of the card at 90% screen width. Updates instantly as `cardConfig` changes. Shows `EnhancedLoadingCard` (reused from existing codebase) during AI extraction.
3. **Bottom: Action bar** — 'Change Background' button (opens `BackgroundPickerSheet`), 'Export' FAB button (opens `ExportBottomSheet`). Background chip row showing current selection.

### BackgroundPickerSheet.kt

- Three tabs: **Gradient | Gallery | Preset**
- **Gradient tab:** shows 6 color pair swatches based on common Malaysian club colors (JDT, Selangor, Pahang, Kedah, Perak, Johor)
- **Gallery tab:** `LazyVerticalGrid` showing recent photos via MediaStore query; tapping one sets `backgroundBitmap`
- **Preset tab:** 3 bundled drawables (stadium blur, dark mesh, grass texture)

### ExportBottomSheet.kt

- **Size selector:** 3 chips (Square / Portrait / Story) using existing `NeoChip` component
- **'Save to Gallery'** button — primary action, uses `BitmapExporter`
- **'Share'** button — secondary action, triggers share intent to Instagram/WhatsApp/etc
- **Success state:** shows `AnimatedCheckmark` (reused from existing codebase) with 'Saved!' text

---

## 07 · Navigation Integration

Socurate currently uses multiple Activities (`MainActivity`, `SettingsActivity`, `UsageActivity`, etc). The card generator section should be added as a new tab in `MainActivity` using Jetpack Compose navigation — keeping it consistent with the Compose migration direction.

### Recommended Approach: Bottom Navigation Tab

- Add `BottomNavigation` to `MainActivity`'s `Scaffold` with two items: **'Generate'** (existing) and **'Card'** (new)
- Use `NavHost` with two destinations: `mainRoute` and `cardGeneratorRoute`
- 'Create Card' button on existing output card calls `navController.navigate(cardGeneratorRoute)` and passes the generated text via shared ViewModel

> **Passing Data Between Screens:** Use a shared `AppViewModel` (scoped to `MainActivity`'s `ViewModelStoreOwner`) to hold the piped article text. `CardGeneratorViewModel` observes this. Avoids nav argument size limits for long article text.

### Entry Points to Card Generator

- **Tab tap:** user navigates to Card tab manually, pastes text, picks template, extracts
- **'Create Card' button:** appears on output card after successful generation in main flow; tapping pre-fills text and navigates
- **Share intent:** if Socurate receives a share, both the main generate flow AND card generate flow can be triggered from `ShareBottomSheetFragment`

---

## 08 · Phased Implementation Plan

Execute phases in order. Each phase is independently committable and testable.

### Phase 1 — Data Models & Prompt Layer `1–2 hrs`

- [ ] Create `cardgen/model/CardTemplate.kt` (sealed class with 4 subclasses)
- [ ] Create `cardgen/model/CardData.kt` (data classes for each template's JSON structure)
- [ ] Create `cardgen/model/CardConfig.kt` (background type enum, color pair, size enum)
- [ ] Create `cardgen/prompt/CardPromptManager.kt` (4 prompt functions returning `String`)
- [ ] Create `cardgen/extractor/CardDataExtractor.kt` (calls `CuratorFactory`, parses JSON, returns `CardData`)
- [ ] Write unit tests for JSON parsing in `CardDataExtractor`

### Phase 2 — ViewModel & State `1 hr`

- [ ] Create `cardgen/viewmodel/CardGeneratorViewModel.kt`
- [ ] Implement all state properties (`inputText`, `selectedTemplate`, `cardData`, `cardConfig`, `extractionState`, `exportState`)
- [ ] Implement `extractCardData()`, `updateConfig()`, `pipeFromMainFlow()` functions
- [ ] Add `AppViewModel` to `MainActivity` for cross-screen text sharing

### Phase 3 — Card Canvas Composables `3–4 hrs`

- [ ] Create `cardgen/renderer/CardCanvas.kt` with all 4 template composables
- [ ] Implement `MatchResultCanvas` — focus on score typography and stat row
- [ ] Implement `HeadlineQuoteCanvas` — Canvas-drawn decorative quote marks
- [ ] Implement `PlayerSpotlightCanvas` — background image with scrim overlay
- [ ] Implement `TopStatsCanvas` — three animated stat rows
- [ ] Create `cardgen/utils/GradientBuilder.kt` and `ColorExtractor.kt`
- [ ] Test all 4 templates with hardcoded mock `CardData` first

### Phase 4 — UI Screens `2–3 hrs`

- [ ] Create `cardgen/ui/TemplatePickerRow.kt`
- [ ] Create `cardgen/ui/CardPreviewPane.kt` (scaled preview, loading state)
- [ ] Create `cardgen/ui/BackgroundPickerSheet.kt` (3 tabs)
- [ ] Create `cardgen/ui/ExportBottomSheet.kt` (size chips, action buttons)
- [ ] Create `cardgen/ui/CardGeneratorScreen.kt` (assembles all components)
- [ ] Add bundled background drawable assets to `res/drawable/`

### Phase 5 — Export Pipeline `1–2 hrs`

- [ ] Create `cardgen/renderer/CardRenderer.kt` (`ComposeView` → Bitmap)
- [ ] Create `cardgen/utils/BitmapExporter.kt` (MediaStore save + FileProvider share)
- [ ] Wire export flow: `ExportBottomSheet` → ViewModel → `CardRenderer` → `BitmapExporter`
- [ ] Test save to gallery on API 24, API 29, API 33
- [ ] Test share intent to Instagram and WhatsApp

### Phase 6 — Navigation & Integration `1 hr`

- [ ] Add `BottomNavigation` with 'Generate' and 'Card' tabs to `MainActivity`
- [ ] Add `NavHost` destinations in `AppNavGraph.kt`
- [ ] Add 'Create Card' `NeoButton` to existing output card in main flow
- [ ] Wire `AppViewModel` for cross-screen text passing
- [ ] Test full end-to-end: share URL → generate text → create card → export

---

## 09 · Dependencies & Gradle Changes

| Dependency / Change | Details |
|---|---|
| **Coil Compose** | `implementation("io.coil-kt:coil-compose:2.6.0")` — for loading gallery images into card background |
| **No new AI deps** | Reuse existing OkHttp + Gson — `CardDataExtractor` uses same `CuratorFactory` |
| **No new UI deps** | Reuse existing Compose BOM, Material 3, and custom Neo components |
| **No new storage deps** | MediaStore API is built into Android SDK — no library needed for gallery save |
| **Permissions (manifest)** | `READ_MEDIA_IMAGES` for API 33+; no new internet permissions needed |

> ✅ **Minimal footprint:** The entire extension adds only ONE new library (Coil). All AI, networking, storage, and UI infrastructure is reused from the existing Socurate codebase.

---

## 10 · Claude Code Prompts

Use these as your starting prompts when working with Claude Code. Always include `MIGRATION_CONTEXT.md` from the repo in your Claude Code context first.

### Phase 1 Prompt
```
In the Socurate Android project (Jetpack Compose, Kotlin, package com.najmi.oreamnos),
create a new package called cardgen/model/.

Create CardTemplate.kt as a sealed class with four object subclasses:
MatchResult, PlayerSpotlight, HeadlineQuote, TopStats.

Create CardData.kt as a sealed class with a data class for each template's JSON shape:
- CardData.MatchResult(homeTeam, awayTeam, homeScore, awayScore, competition, matchDate, homeStats, awayStats, keyMoment)
- CardData.PlayerSpotlight(playerName, club, position, rating, goals, assists, keyQuote)
- CardData.HeadlineQuote(headline, subtext, source)
- CardData.TopStats(stats: List<StatItem>) where StatItem(label, value, context)

Create CardConfig.kt with:
- enum BackgroundType { GRADIENT, GALLERY, PRESET }
- enum ExportSize { SQUARE, PORTRAIT, STORY }
- data class CardConfig(template: CardTemplate, backgroundType: BackgroundType, colorPair: Pair<Color, Color>, exportSize: ExportSize, backgroundBitmap: Bitmap? = null)

Follow existing Kotlin style in the codebase.
```

### Phase 3 Prompt
```
Create cardgen/renderer/CardCanvas.kt in the Socurate project.

Implement MatchResultCanvas as a @Composable fun that takes CardData.MatchResult
and CardConfig as parameters and renders:
- Team names in NeoCard style at top-left and top-right
- Score in bold 72sp center typography
- A horizontal divider
- A 3-column stat row (possession, shots, shots on target) for each team at the bottom
- Background using Brush.verticalGradient() from CardConfig.colorPair

The composable must render correctly both on-screen (for preview) and
offscreen (for bitmap export via ComposeView.drawToBitmap()).
Reuse colors and typography from ui/theme/Color.kt and ui/theme/Type.kt.
Add an @Preview function for each composable.
```

### General Claude Code Tips

- Always ask Claude Code to read `MIGRATION_CONTEXT.md` first before writing any new file
- Reference existing files explicitly: *"follow the pattern in `GeminiCurator.kt`"* to keep style consistent
- For Canvas composables, always request a `@Preview` function for each template
- Run `./gradlew assembleDebug` after each phase to catch compile errors before moving on
- Use the existing `LogListActivity` to verify AI extraction logs during testing

---

## 11 · Testing Checklist

| Test Case | Verification Method |
|---|---|
| AI extraction — all 4 templates | Paste a real Malay football article, verify JSON parses correctly for each card type |
| Null/missing field handling | Pass an article with missing data, verify placeholders render without crash |
| Rate limit recovery | Trigger rate limit, verify existing fallback dialog appears and card retry works |
| Background: gallery image | Pick a dark and a light photo, verify scrim maintains text legibility |
| Background: gradient | Test all 6 color swatches, verify gradient renders on all 4 templates |
| Export: save to gallery | Save on API 24, API 29, API 33 — verify file appears in Photos app |
| Export: share intent | Share to Instagram, WhatsApp — verify image transfers correctly |
| Export sizes | Verify pixel dimensions with an EXIF reader: 1080×1080, 1080×1350, 1080×1920 |
| Navigation | 'Create Card' button pipes text correctly from main flow |
| Theme consistency | Test all 3 Socurate themes (Light, Dark, Deep Blue) — card generator UI matches |

---

*Socurate Visual Post Card Extension · Implementation Plan · For the Malaysian Football Community ⚽*
