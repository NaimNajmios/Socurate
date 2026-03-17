# Oreamnos Android

A sleek, modern Android application that transforms global football news into polished Malaysian Malay social media posts using AI. Built with **Jetpack Compose** and **Kotlin** featuring a premium design system and sophisticated animations.

## ✨ What's New

### 👁️ On-Device Vision AI (Screenshot Extraction)
- **Multi-Model Vision Extraction**: Extract football stats directly from screenshots
- **Gemma 3n E2B** (~2.9GB): Best for screenshots - multimodal model that processes images directly
- **Gemma 3 1B** (~557MB): Lightweight text-only model (uses OCR first)
- **PaliGemma 3B** (~3GB): Legacy vision option
- **HuggingFace Download**: One-time model download from LiteRT Community
- **Fully Offline**: All processing on-device after download
- **ML Kit Fallback**: Always-available OCR backup for any device
- **Auto-Selection**: Automatically picks the best available model

### 🖼️ Automated Sports Card Generator (with Design Studio)
- **Premium Graphics**: Generate 1:1 square sports cards resembling premium NBA coverage and sports media.
- **Multiple Templates**: 10+ built-in templates:
  - **Breaking News**: High-impact headline card
  - **Match Preview**: Pre-match analysis layout
  - **Match Result**: Full scoreboard design
  - **Detailed Scoreboard**: Stats-heavy format
  - **Transfer News**: Player transfer announcement
  - **Starting XI**: Team lineup display
  - **On This Day**: Historical moment card
  - **Player Spotlight**: Individual player highlight
  - **Headline Quote**: Quote with attribution
  - **Top Stats**: Statistical comparison
- **Image Position Modes**: 10 visual layouts:
  - Full Background, Split Left/Right, Overlay Top
  - Cutout (transparent PNG), Minimal, Magazine Bold
  - Offset Card, Brutalist, Float Window
- **Inline Editing**: Dynamically edit the extracted AI fields right in the app to perfect the copy before rendering.
- **Inline Editing**: Dynamically edit the extracted AI fields right in the app to perfect the copy before rendering.
- **Dynamic Backgrounds & Opacity**: Custom GradientBuilder and an interactive background picker UI for versatile styling. Adjust overlay opacity via a granular slider.
- **Typography Controls**: Instantly swap between Default, Classic Serif, and Typewriter Monospace fonts across all components.
- **Integrated Export**: Dedicated export bottom sheet to directly save or share generated cards.

### 🔄 Multi-Provider AI Support
- **Gemini (Google)**: Default provider with multiple model options
- **Groq (Llama 3.3)**: Fast inference alternative
- **OpenRouter**: Access to free models
- **Cerebras**: Ultra-fast inference provider
- **Dynamic Model Selection**: Each provider shows its available models in the dropdown

### 🛡️ Interactive Rate Limit Recovery
- **Smart Fallback**: When rate limited, a dialog offers to switch providers automatically
- **Fallback Chain**: Gemini → Groq → OpenRouter → Cerebras → Gemini
- **Seamless Retry**: Switch providers and retry with one tap
- **API Key Check**: Only offers fallback if alternative provider is configured

### 🎨 Jetpack Compose UI (Kotlin Rewrite)
- **100% Kotlin**: Complete migration from Java to Kotlin
- **Jetpack Compose**: Modern declarative UI framework
- **Neo-Editorial Design System**: Custom components with sharp corners and bold accents
- **12 Custom Components**: NeoCard, NeoChip, NeoButton, NeoInput, and more
- **Spring Physics**: Natural-feeling animations with bounce and inertia

### 🎭 Three Theme Modes
- **Light Mode**: Clean, bright interface with International Orange accent
- **Dark Mode**: High contrast with monochrome base
- **Deep Blue Mode**: Premium navy background with Electric Blue accent

### 📊 Usage Statistics & Analytics
- **Token Tracking**: Monitor total tokens used (prompt + response)
- **Interactive Charts**: Three custom Canvas-based Compose charts:
  - **Token Usage Graph**: Line chart with 7/30/90 day views
  - **Success Rate Chart**: Animated donut chart with per-provider breakdown
  - **Response Time Analytics**: Bar chart with min/max indicators
- **Session History**: Track last 20 requests with full details
- **Per-Provider Stats**: Token usage and success rates by AI provider

### ✨ Premium Animations
- **Typewriter Effect**: Character-by-character text reveal (3 chars/ms)
- **Stagger Entrance**: Cascading chip animations with delay
- **Spring Physics**: Bouncy swipe gestures and card interactions
- **Animated Checkmark**: Custom Canvas-drawn success animation
- **Pulsing Loading**: Progress indicator with percentage display
- **Haptic Feedback**: Tactile vibrations on key interactions

### 📝 Markdown Rendering
- **Rich Text Display**: AI output with proper formatting
- **Bold Text**: `**text**` displays as actual bold
- **Headers**: `## Header` displays as styled headers
- **Italic Support**: `*text*` and `_text_` formatting
- **Selection Container**: Copy portions of rendered text

### 🎯 Gesture Controls
- **Swipe Left to Copy**: Quick copy action on output card
- **Swipe Right to Share**: Quick share action on output card
- **Long-Press Pills**: Edit or delete custom refinement commands
- **Pull-to-Refresh**: Refresh usage statistics

### 🔘 Dynamic Output Toggles
- **Title Toggle**: Show or hide the post title
- **Hashtags Toggle**: Include or exclude hashtags
- **Source Toggle**: Control source citation visibility
- **Live Preview**: See changes instantly

### 💊 Custom Refinement Pills
- **Create Custom Commands**: Define your own refinement instructions
- **Built-in Refinements**: Rephrase, Check Flow, Check Wording
- **Selectable Chips**: Custom pills appear with orange border
- **Easy Management**: Tap "+" to create, long-press to edit/delete

### 📱 Enhanced Share Intent
- **Modern Bottom Sheet**: Sleek slide-up overlay
- **Animated Progress**: Stage labels during generation
- **Collapsible Input**: Auto-collapses after generation
- **Tone Quick Toggle**: Switch Formal/Casual before generating

## 🎯 Core Features

### Content Generation
- **Share Intent Integration**: Share from Chrome, Twitter, or any app
- **Universal Football Coverage**: Works for any team or league
- **AI-Powered Curation**: Professional Malaysian Malay posts
- **Smart Context Detection**: Adapts to quotes, tactical analysis, different content types
- **URL Extraction**: Automatically extracts content from shared URLs
- **Instant Sports Cards**: Turn any generated news into a visually stunning, shareable 1:1 layout card with a single tap.

### Customization & Productivity
- **Dual Tone Modes**: Formal (professional) or Casual (fan banter)
- **Hashtag Manager**: Configure default hashtags
- **Quick Edit**: Inline editing with monospace font
- **Reading Mode**: Full-screen immersive content viewing
- **Adjustable Text Size**: Customize output font size

### Technical Excellence
- **Intelligent Retry Logic**: Exponential backoff for transient errors
- **Rate Limit Handling**: Respects API retry delays (up to 60s)
- **Response Time Tracking**: Duration tracked for all requests
- **Secure Storage**: API keys encrypted with EncryptedSharedPreferences
- **Comprehensive Logging**: Detailed logs with unique request IDs

## 📋 Requirements

- **Android 8.0 (API 26)** or higher
- **Note**: Vision AI features require API 26+. ML Kit OCR fallback works on API 24+
- At least one AI provider API key:
  - Google Gemini: [Get one here](https://ai.google.dev)
  - Groq: [Get one here](https://console.groq.com)
  - OpenRouter: [Get one here](https://openrouter.ai)
  - Cerebras: [Get one here](https://cerebras.ai)
- Internet connection

## 🚀 Installation

### Option 1: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/NaimNajmios/Socurate.git
   cd Socurate
   ```

2. Open the project in Android Studio

3. Build and run on your device or emulator

### Option 2: Install APK

Download the latest APK from the [Releases](https://github.com/NaimNajmios/Socurate/releases) page.

## ⚙️ Setup

1. Launch the app
2. Tap the settings icon (⚙️) in the toolbar
3. **AI Provider Setup**:
   - Select your preferred provider (Gemini, Groq, OpenRouter, or Cerebras)
   - Enter the API key for your selected provider
   - Select AI Model from the dropdown
4. **Post Settings**:
   - Choose tone: Formal or Casual
   - Enable/disable source citation
5. **Hashtag Settings**:
   - Enter default hashtags (e.g., `#BolaSepak #JDT #Football`)
   - Toggle auto-append on/off
6. **Appearance**:
   - Choose theme: Light, Dark, Deep Blue, or System

## 💡 Usage

### Method 1: Share from Other Apps

1. Open a football article in any browser
2. Tap the "Share" button
3. Select "Oreamnos" from the share menu
4. A **bottom sheet** slides up showing your shared content
5. **Tone Quick Toggle**: Choose Formal or Casual
6. Watch the **animated progress** as content is generated
7. **Toggle Options**: Use chips to include/exclude Title, Hashtags, Source
8. **Swipe gestures**: Left to copy, right to share
9. Copy or share the final result

### Method 2: Direct Input

1. Open the Oreamnos app
2. Paste article text or URL into the input field
3. Tap the "Generate Post" FAB button
4. Watch the loading animation with percentage
5. Review the generated content with typewriter effect
6. Adjust display options using toggle chips
7. Copy or share the generated post

### Generating Sports Cards

1. Generate text content using direct input or share intent
2. Tap the "Generate Card" button on the output card
3. Choose a template (Match Result, Spotlight, Quote, Top Stats)
4. Customize the background color and design
5. Save or directly share the 1:1 image to supported platforms

### Post Refinement Workflow

1. After generation, the refinement card appears
2. Select refinement options:
   - **Rephrase**: Fresh take on the same content
   - **Recheck Flow**: Improve paragraph structure
   - **Recheck Wording**: Enhance vocabulary
3. **Custom Pills**: Tap "+" to create your own (e.g., "Make it punchy")
4. Tap "Apply Refinements" to regenerate
5. Repeat as needed

### Reading Mode

1. After generation, tap the expand button
2. Full-screen immersive content view
3. Swipe down to dismiss

### Extracting from Screenshots

1. From the main screen, tap the **"From Screenshot"** chip below the input field
2. Select an image from Gallery or Camera
3. The app automatically extracts text using the best available model:
   - **Gemma 3n E2B**: Direct image understanding (best results)
   - **Gemma 3 1B**: OCR + text structuring
   - **ML Kit OCR**: Always-available fallback
4. Review and edit the extracted text
5. Tap "Use This Text" to proceed with generation

## 🏗️ Architecture

### Package Structure

```
com.najmi.oreamnos/
├── cardgen/                    # Sports Card Generator Features
│   ├── canvas/                # Canvas renderers (BreakingNews, MatchPreview, etc.)
│   ├── extractor/             # Parsing AI output to structural data
│   ├── model/                # Card configuration, templates, ImagePosition
│   ├── prompt/                # Card-specific AI prompts
│   ├── renderer/             # Custom Compose-to-Bitmap rendering
│   ├── ui/                   # Generator UI, background picker, export
│   └── viewmodel/            # CardGeneratorViewModel
├── curator/                    # AI Provider Abstraction
│   ├── IContentCurator.kt     # Content curator interface
│   ├── CuratorFactory.kt      # Factory for creating curators
│   ├── GeminiCurator.kt      # Gemini implementation
│   └── OpenAICompatibleCurator.kt  # Groq/OpenRouter/Cerebras
├── model/                      # Data Models
│   ├── GenerationPill.kt      # Custom refinement pill model
│   ├── UsageStats.kt         # Usage statistics with chart data
│   └── VisionExtractionResult.kt  # Vision extraction result
├── prompts/                    # Prompt Engineering
│   └── PromptManager.kt       # Centralized prompt building
├── services/                   # Background Services
│   ├── GeminiService.kt       # Gemini API communication
│   ├── ContentGenerationService.kt  # Background generation
│   ├── GenerateTileService.kt # Quick Settings tile
│   └── WebContentExtractor.kt # URL content extraction
├── ui/                         # UI Layer
│   ├── components/            # Reusable Compose components
│   │   ├── NeoCard.kt        # Base card component
│   │   ├── NeoChip.kt        # Selection chip with animations
│   │   ├── NeoButton.kt      # Primary action button
│   │   ├── NeoInput.kt       # Text input field
│   │   ├── NeoCopyButton.kt  # Copy button with feedback
│   │   ├── FluidRefinementFlow.kt  # Refinement UI
│   │   ├── OcrInputSheet.kt  # Vision extraction bottom sheet
│   │   ├── VisionModelDownloadCard.kt  # Model download UI
│   │   ├── ExtractionSourceBadge.kt  # Source indicator
│   │   ├── TypewriterText.kt # Animated text reveal
│   │   ├── AnimatedCheckmark.kt    # Success animation
│   │   ├── EnhancedLoadingCard.kt  # Progress indicator
│   │   ├── TokenUsageChart.kt # Usage line chart
│   │   ├── SuccessRateChart.kt    # Donut chart
│   │   └── ResponseTimeChart.kt   # Bar chart
│   └── theme/                 # Material 3 Theme
│       ├── Color.kt           # Color palette
│       ├── Theme.kt           # Theme configurations
│       └── Type.kt            # Typography styles
├── utils/                      # Utilities
│   ├── PreferencesManager.kt  # Encrypted settings
│   ├── VisionModelManager.kt  # LiteRT model download & storage
│   ├── HapticHelper.kt        # Vibration feedback
│   ├── NotificationHelper.kt   # Notification management
│   ├── ReadabilityUtils.kt    # Flesch-Kincaid scoring
│   ├── MarkdownUtils.kt       # Markdown parsing
│   ├── OcrUtils.kt           # ML Kit OCR wrapper
│   └── FootballOcrParser.kt   # Football-specific OCR formatting
├── vision/                     # On-Device Vision AI
│   ├── IVisionExtractor.kt   # Extractor interface
│   ├── VisionExtractorFactory.kt  # Model selection
│   ├── VisionModel.kt        # Model definitions (Gemma3n, Gemma3, etc.)
│   ├── LiteRTEngine.kt       # LiteRT runtime wrapper
│   ├── Gemma3nVisionExtractor.kt  # Multimodal extractor
│   ├── Gemma3TextExtractor.kt # Text-only extractor
│   ├── GeminiNanoExtractor.kt # ML Kit-based extractor
│   └── MlKitFallbackExtractor.kt  # OCR fallback
├── viewmodel/                  # MVVM ViewModels
├── MainActivity.kt            # Main screen (Compose)
├── SettingsActivity.kt        # Settings (Compose)
├── UsageActivity.kt           # Usage stats with charts
├── HashtagManagerActivity.kt  # Hashtag management
├── LogListActivity.kt         # Debug log viewer
├── SessionListActivity.kt     # Session history
├── ShareReceiverActivity.kt   # Share intent host
├── ShareBottomSheetFragment.kt # Share bottom sheet
└── ReadingModeDialog.kt       # Full-screen reader
```

### Design System

The app uses a **Neo-Editorial** design language:

| Component | Description |
|-----------|-------------|
| **NeoCard** | Cards with sharp corners and subtle borders |
| **NeoChip** | Rectangular chips with animated color transitions |
| **NeoButton** | Primary buttons with haptic feedback |
| **NeoInput** | Monospace text fields with labels |

### Vision AI Architecture

The app implements a multi-path on-device vision extraction system:

```
IVisionExtractor (interface)
├── Gemma3nVisionExtractor    # Multimodal - processes images directly
├── Gemma3TextExtractor       # Text-only - uses OCR first
├── GeminiNanoExtractor        # ML Kit + structuring
└── MlKitFallbackExtractor    # Always-available OCR fallback
```

**Model Selection Priority:**
1. Gemma 3n E2B (if downloaded) - Best results
2. Gemma 3 1B (if downloaded) - Lightweight option
3. ML Kit OCR - Always available

### Animation System

| Animation | Implementation |
|-----------|----------------|
| Typewriter | `LaunchedEffect` with 1ms delay, 3 chars/step |
| Stagger Entrance | `updateTransition` with indexed delay (30ms/item) |
| Spring Physics | `spring(dampingRatio = MediumBouncy)` |
| Swipe Gestures | `pointerInput` + `detectHorizontalDragGestures` |
| Color Transitions | `animateColorAsState` with 200ms tween |

## 🛠️ Technology Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM with ViewModels

### Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2024.02.00 | UI framework |
| Material 3 | Latest | Design system |
| OkHttp | 4.12.0 | Networking |
| Gson | 2.10.1 | JSON parsing |
| Jsoup | 1.17.2 | HTML parsing |
| Security Crypto | 1.1.0-alpha06 | Encrypted preferences |
| Shimmer | 0.5.0 | Skeleton loading |
| Markwon | 4.6.2 | Markdown rendering |
| ML Kit Text Recognition | 16.0.0 | OCR fallback |
| LiteRT-LM | latest.release | On-device GenAI (vision models) |
| Coil | 2.6.0 | Image loading |

## 🎨 Theme Configuration

### Color Palettes

#### Light Theme
| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#FF4500` | International Orange accent |
| Background | `#FFFFFF` | Neo White |
| Surface | `#F9F9F9` | Neo Off White |
| On Surface | `#000000` | Neo Black |

#### Dark Theme
| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#FF4500` | International Orange accent |
| Background | `#000000` | Neo Black |
| Surface | `#121212` | Neo Dark Grey |
| On Surface | `#FFFFFF` | Neo White |

#### Deep Blue Theme
| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#3B82F6` | Electric Blue accent |
| Background | `#0B1120` | Deep Navy |
| Surface | `#1E293B` | Slate |
| On Surface | `#FFFFFF` | White |

## 🔒 Privacy & Security

- **Encrypted Storage**: API keys stored using EncryptedSharedPreferences with AES256-GCM
- **No Data Collection**: Zero telemetry, analytics, or third-party tracking
- **Local Processing**: All processing on-device except necessary API calls
- **HTTPS Only**: Secure API communication with timeout protection
- **Minimal Permissions**: Only internet permission required

## 🐛 Troubleshooting

### "API key required" error
**Solution**: Go to Settings and enter a valid API key for your selected provider.

### Rate limit errors
The app handles rate limits with **Interactive Recovery**:
- Shows a dialog when rate limited
- Offers to switch to an alternative provider
- One-tap retry with the new provider

**Additional solutions**:
- Configure multiple provider API keys for seamless fallback
- Wait for the suggested time period before retrying

### Connection errors
**Solutions**:
- Check your internet connection
- Verify your API key in Settings
- Try the "Test Connection" button

### Debugging with Logcat
Filter by package: `com.najmi.oreamnos`

Look for these tags:
- `MainActivity` - UI interactions
- `GeminiService` - API calls, retries
- `OpenAICompatibleCurator` - Groq/OpenRouter/Cerebras API calls
- `WebContentExtractor` - URL parsing
- `LiteRTEngine` - Vision model inference
- `VisionModelManager` - Model download

### Vision Extraction Issues

**"No text detected"**
- Try a clearer screenshot with more visible text
- Ensure the image has good contrast

**Download failing**
- Check internet connection
- Ensure sufficient storage space (models are 557MB-3GB)
- For gated models, provide HuggingFace token in Settings

**Slow extraction**
- Gemma 3n E2B is optimized for GPU; may be slower on older devices
- Use Gemma 3 1B (557MB) for faster processing on mid-range devices

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Original web app: [Oreamnos](https://github.com/NaimNajmios/Oreamnos)
- Powered by [Google Gemini API](https://ai.google.dev), [Groq](https://groq.com), [OpenRouter](https://openrouter.ai), [Cerebras](https://cerebras.ai)
- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Material Design 3](https://m3.material.io/)

## 📧 Contact

For issues or questions, please open an issue on [GitHub](https://github.com/NaimNajmios/Socurate/issues).

---

**Made with ⚽ for the Malaysian football community**
