# Socurate (Oreamnos Android Edition)

A sleek, modern Android application that transforms global football news into polished Malaysian Malay social media posts using AI. Built with a premium dark theme for an exceptional user experience.

## ✨ What's New

### 🎨 Modern UI/UX Redesign
- **Premium Dark Theme**: Sleek dark interface with sophisticated Blue Grey accents
- **Theme Toggle**: Switch between Light, Dark, or System (auto) themes
- **Smooth Animations**: Fade-in/fade-out transitions and shimmer skeleton loading states
- **Material Design 3**: Cards, pill-shaped buttons, filter chips, and modern typography
- **Glassy Effects**: Subtle translucency for a premium feel

### 🔘 Dynamic Output Toggles
- **Title Toggle**: Show or hide the post title with a single tap
- **Hashtags Toggle**: Include or exclude hashtags on demand
- **Source Toggle**: Control source citation visibility dynamically
- **Filter Chips**: Beautiful, accessible chip-based toggle controls
- **Live Preview**: See changes instantly as you toggle options

### 🔔 Progress Notifications
- **Real-time Status**: Shows notification with progress bar during content generation
- **Completion Alert**: Updates notification when post is ready
- **Auto-dismiss**: Notifications clear automatically after a few seconds

### 📱 Enhanced Share Intent Experience
- **Modern Bottom Sheet**: Sleek slide-up overlay instead of full-screen activity
- **Animated Progress Bar**: Horizontal progress with stage labels ("Extracting..." → "Generating...")
- **Collapsible Input**: Auto-collapses after generation, tap to expand/collapse
- **Haptic Feedback**: Vibration feedback on generation start, completion, and copy actions
- **Tone Quick Toggle**: Switch between Formal/Casual tone without going to Settings
- **Background Processing**: Generate in background and continue using other apps

### 🤖 AI Model Selection
- **Multiple Models**: Choose from available Gemini models in Settings:
  - Gemini 2.5 Flash Lite (fastest)
  - Gemini 2.5 Flash
  - Gemini 2.5 Pro (highest quality)
  - Gemini 2.0 Flash (default)
- **Easy Switching**: Simple dropdown in Settings to change models

### 🔍 Source Citation
- **Automatic Attribution**: AI detects and includes source information in generated posts
- **Toggle Control**: Enable/disable source citation in settings and dynamically per post
- **Format**: Posts end with "Sumber: [Source Name]" when enabled

### ⚡ Post Refinement
- **Multiple Refinement Options**: Improve generated posts with targeted adjustments
  - **Rephrase**: Rewrite with different wording while maintaining meaning
  - **Recheck Flow**: Improve logical flow and structure
  - **Recheck Wording**: Enhance word choice and clarity
  - **Make Formal**: Adjust tone for official communication
  - **Make Conversational**: Shift tone for fan engagement
  - **Shorten But Detailed**: Make more concise while keeping all important details
- **One-Click Regeneration**: Apply selected refinements instantly

## 🎯 Core Features

### Content Generation
- **Share Intent Integration**: Share articles directly from Chrome, Twitter, or any app
- **Universal Football Coverage**: Works for any team or league worldwide
- **AI-Powered Curation**: Uses Google Gemini 2.0 Flash for professional Malaysian Malay posts
- **Smart Context Detection**: Adapts to quotes, tactical analysis, and different content types
- **One-Tap Actions**: Copy to clipboard or share directly to social media

### Customization & Productivity
- **Dual Tone Modes**: Choose between formal (professional) or casual (fan banter) styles
- **Hashtag Manager**: Configure default hashtags that auto-append to all posts
- **Quick Edit**: Edit generated posts inline before sharing with visual indicators
- **URL Extraction**: Automatically extracts and processes content from shared URLs

### Technical Excellence
- **Intelligent Retry Logic**: Automatically retries failed API calls with exponential backoff
- **Rate Limit Handling**: Parses and respects API's requested retry delays (up to 60s)
- **Secure Storage**: API keys encrypted using Android's EncryptedSharedPreferences
- **Comprehensive Logging**: Detailed logs with unique request IDs for debugging
- **Error Recovery**: User-friendly error messages with actionable suggestions

## 📋 Requirements

- Android 7.0 (API 24) or higher
- Google Gemini API key ([Get one here](https://ai.google.dev))
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
3. **API Configuration**:
   - Enter your Gemini API key
   - Select AI Model from dropdown (Gemini 2.5 Flash Lite, Flash, Pro, or 2.0 Flash)
   - (Optional) Test the connection
4. **Post Settings**:
   - Choose tone: Formal or Casual
   - Enable/disable source citation
5. **Hashtag Settings**:
   - Enter default hashtags (e.g., `#BolaSepak #JDT #Football`)
   - Toggle auto-append on/off
6. **Appearance**:
   - Choose theme: Light, Dark, or System
7. Tap "Save Settings"

## 💡 Usage

### Method 1: Share from Other Apps

1. Open a football article in Chrome, Twitter, or any browser
2. Tap the "Share" button
3. Select "Socurate" from the share menu
4. A **bottom sheet** slides up showing your shared content
5. **Tone Quick Toggle**: Choose Formal or Casual before generation starts
6. Watch the **animated progress bar** as content is extracted and generated
7. Review the generated content (input card auto-collapses)
8. **Toggle Options**: Use the filter chips to include/exclude:
   - **Title**: Toggle the post title on/off
   - **Hashtags**: Toggle your default hashtags
   - **Source**: Toggle source attribution
9. **Background Processing**: Tap "Generate in background" to continue in other apps
10. **Continue in Main App**: Tap to open full app with refinement options
11. Copy or share the final result

### Method 2: Direct Input

1. Open the Socurate app
2. Paste article text or URL into the input field
3. Tap the glowing "Generate Post" FAB button
4. Wait for the AI to process (shimmer loading animation)
5. Review the generated content
6. **Toggle Options**: Adjust title, hashtags, and source using chips
7. **Refine (Optional)**: Use refinement options to improve the post
8. **Edit (Optional)**: Tap "Edit" for manual adjustments
9. Copy or share the generated post

### Post Refinement Workflow

1. After initial generation, the refinement card appears below the output
2. Select one or more refinement options:
   - **Rephrase**: Get a fresh take on the same content
   - **Recheck Flow**: Improve paragraph structure and transitions
   - **Recheck Wording**: Enhance vocabulary and phrasing
   - **Make Formal**: Elevate tone for professional contexts
   - **Make Conversational**: Relax tone for fan communities
   - **Shorten But Detailed**: Make concise while keeping key information
3. Tap "Regenerate" to apply improvements
4. Refinement checkboxes auto-clear after each regeneration
5. Repeat as needed for perfect results

### Quick Edit Mode

1. After generation, tap the "Edit" button
2. The output field becomes editable
3. Make inline changes to the post
4. An "(Edited)" indicator appears to show modifications
5. Tap "Save" (button changes to checkmark icon)
6. Changes are locked in
7. Copy/share the edited version

### Hashtag Management

1. Go to Settings → Hashtag Settings
2. Enter comma or space-separated hashtags (e.g., `#BolaSepak, #JDT, #Football`)
3. Toggle "Auto-append hashtags" on/off
4. Hashtags are automatically formatted (# added if missing)
5. Use the Hashtags chip on the main screen to include/exclude hashtags per post

## 🏗️ Architecture

### Core Components

- **GeminiService**: Handles API communication with intelligent retry logic, exponential backoff, source detection, and post refinement
- **ContentGenerationService**: Background service for non-blocking content generation
- **WebContentExtractor**: Extracts main content from URLs using Jsoup with robust error handling
- **PreferencesManager**: Securely manages API keys, settings, and user preferences using encrypted storage

### Activities

- **MainActivity**: Primary interface for manual input, generation, editing, and refinement with dynamic toggle chips
- **ShareReceiverActivity**: Transparent host for the share bottom sheet
- **ShareBottomSheetFragment**: Modern bottom sheet with progress bar, tone toggle, and haptic feedback
- **SettingsActivity**: Configuration hub for API, tone, hashtags, source, and theme management

### UI Components

- **Shimmer Loading**: Facebook Shimmer library for elegant skeleton loading animations
- **Filter Chips**: Material Design 3 chips for Title, Hashtags, and Source toggles
- **Cards**: Material CardView with elevation and rounded corners
- **Buttons**: Pill-shaped buttons and Extended FAB with scale animations
- **Themes**: Dark and Light themes with consistent color schemes

## 🛠️ Technology Stack

- **Language**: Java
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Design**: Material Design 3 with custom dark theme
- **Libraries**:
  - AndroidX (AppCompat, Material Design 3)
  - OkHttp 4.12.0 (Networking with timeout configuration)
  - Gson 2.10.1 (JSON parsing with custom error handling)
  - Jsoup 1.17.2 (HTML parsing and content extraction)
  - Security Crypto 1.1.0-alpha06 (Encrypted preferences with AES256)
  - Facebook Shimmer 0.5.0 (Skeleton loading animations)

## 📁 Project Structure

```
app/src/main/
├── java/com/mycompany/oreamnos/
│   ├── MainActivity.java                  # Main content generation activity
│   ├── ShareReceiverActivity.java        # Share intent handler
│   ├── SettingsActivity.java             # Settings and configuration
│   ├── services/
│   │   ├── GeminiService.java            # AI API integration with retry logic
│   │   ├── ContentGenerationService.java # Background generation service
│   │   └── WebContentExtractor.java      # URL content extraction
│   └── utils/
│       ├── NotificationHelper.java       # Progress notifications
│       ├── HapticHelper.java             # Vibration feedback utility
│       └── PreferencesManager.java       # Secure settings storage
├── res/
│   ├── anim/                             # Animations (fade, slide)
│   │   ├── fade_in.xml
│   │   ├── fade_out.xml
│   │   └── slide_up.xml
│   ├── color/                            # Color state selectors
│   │   ├── chip_background_selector.xml  # Chip background states
│   │   └── chip_text_color_selector.xml  # Chip text color states
│   ├── drawable/                         # Custom drawables
│   │   ├── pill_button.xml              # Pill-shaped button background
│   │   ├── pill_button_dark.xml         # Dark theme variant
│   │   ├── skeleton_item.xml            # Skeleton loading item
│   │   └── ic_stadium.xml               # Placeholder illustration
│   ├── layout/
│   │   ├── activity_main.xml            # Main screen with filter chips
│   │   ├── activity_share_receiver.xml  # Share receiver layout
│   │   └── activity_settings.xml        # Settings screen layout
│   ├── values/
│   │   ├── strings.xml                  # App strings
│   │   ├── colors.xml                   # Color palette
│   │   └── themes.xml                   # Light theme
│   ├── values-night/
│   │   ├── colors.xml                   # Dark theme colors
│   │   └── themes.xml                   # Dark theme
│   ├── mipmap-*/                        # App icons (all densities)
│   └── menu/
│       └── menu_main.xml                # Toolbar menu
└── AndroidManifest.xml
```

## 🎨 Design System

### Color Palette

#### Dark Theme
- **Primary**: Blue Grey (#90A4AE) - Accents and CTAs
- **Background**: Dark Gray (#121212) - Main background
- **Surface**: Darker Gray (#1E2326) - Cards and elevated surfaces
- **Text**: Light Gray (#ECEFF1) - Primary text
- **Text Secondary**: Blue Gray (#B0BEC5) - Secondary text

#### Light Theme
- **Primary**: Blue Grey (#455A64) - Accents and CTAs
- **Background**: White (#FFFFFF) - Main background
- **Surface**: Light Gray (#F5F7F8) - Cards and elevated surfaces
- **Text**: Dark Gray (#1C2326) - Primary text
- **Text Secondary**: Blue Gray (#546E7A) - Secondary text

### Typography
- **Headlines**: Medium weight, 20-24sp
- **Body**: Regular weight, 14-16sp
- **Buttons**: Medium weight, 14sp, all caps

### Filter Chips
- **Unchecked**: Transparent background with outline
- **Checked**: Primary color background with white text
- **States**: Proper color contrast for accessibility in both themes

## 🎭 Theme Customization

### Changing the Theme

1. Go to Settings → Appearance
2. Select your preferred theme:
   - **Light**: Clean, bright interface
   - **Dark**: Modern dark theme with Blue Grey accents (default)
   - **System**: Automatically follows device theme
3. Theme applies immediately

### Changing the Tone

The app supports two tone styles for generated content:

- **Formal**: Professional, suitable for official club communications
- **Casual**: Engaging, conversational for fan communities

Change this in Settings → Post Settings → Tone

### Advanced Configuration

In Settings → Advanced Settings → AI Model, select from available Gemini models:
- **Gemini 2.5 Flash Lite**: Fastest response times
- **Gemini 2.5 Flash**: Balanced speed and quality
- **Gemini 2.5 Pro**: Highest quality outputs
- **Gemini 2.0 Flash**: Default, reliable performance

## 🔒 Privacy & Security

- **Encrypted Storage**: API keys stored using Android's EncryptedSharedPreferences with AES256-GCM
- **No Data Collection**: Zero telemetry, analytics, or third-party tracking
- **Local Processing**: All processing happens on-device except necessary API calls to Google Gemini
- **Secure Communication**: HTTPS-only API communication with timeout protection
- **No Permissions**: App requires only internet permission, no access to contacts, storage, or location

## 🐛 Troubleshooting

### "API key required" error

**Cause**: No API key configured or key was cleared.

**Solution**: Go to Settings and enter a valid Gemini API key. Use "Test Connection" to verify.

### "Could not extract content from URL"

**Cause**: Website blocking automated access or poor connectivity.

**Solutions**:
- Try copying the article text directly instead
- Check if the URL is accessible in a browser
- Verify internet connection

### Connection errors

**Symptoms**: "Network error", "Connection timeout", "Unknown error"

**Solutions**:
- Check your internet connection
- Verify your API key is correct in Settings
- Try the "Test Connection" button in Settings
- Check if Google services are accessible in your region
- Reset endpoint to default in Advanced Settings

### Rate limit errors

**Cause**: Exceeded Gemini API quota (requests per minute/day).

The app now handles rate limits intelligently:
- Automatically retries with the exact delay requested by the API (up to 60 seconds)
- Shows user-friendly messages: "Please wait X seconds and try again"
- Logs all retry attempts with unique IDs in Logcat for debugging
- Suggests using `gemini-1.5-flash` model for better quotas

**Solutions**:
- Wait for the suggested time period before retrying
- Check your API quota at [Google AI Studio](https://ai.google.dev)
- Consider upgrading your API plan for higher limits

### Theme not applying

**Solution**: Ensure you've saved settings after changing theme. Theme applies immediately on save.

### Generated text too short/long

The app automatically adapts output length to 40-60% of input length. For better control:
- Provide clearer, more structured input
- Use formal tone for concise output
- Edit the generated text manually if needed

### Debugging with Logcat

The app includes comprehensive logging with unique request IDs for all actions:

1. Open Android Studio → Logcat tab
2. Filter by package: `com.najmi.oreamnos`
3. Look for these tags:
   - `MainActivity` - UI interactions, button clicks, toggle changes
   - `GeminiService` - API calls, retries, responses, source detection
   - `ContentGenerationService` - Background processing events
   - `WebContentExtractor` - URL parsing, content extraction
   - `SettingsActivity` - Configuration changes, theme switches
   - `PreferencesManager` - Settings storage and retrieval

**Example logs:**
```
I/MainActivity: === MainActivity onCreate ===
I/MainActivity: >>> Generate button clicked <<<
I/GeminiService: === GEMINI API CALL START [a1b2c3d4] ===
I/GeminiService: [a1b2c3d4] Input text length: 2456 characters
I/GeminiService: [a1b2c3d4] Gemini attempt 1/4
I/GeminiService: [a1b2c3d4] Response code: 200 (time: 1834ms) on attempt 1
I/GeminiService: [a1b2c3d4] Success! Output: 892 chars (total time: 1842ms)
I/MainActivity: Post generation SUCCESSFUL
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Original web app: [Oreamnos](https://github.com/NaimNajmios/Oreamnos)
- Powered by [Google Gemini API](https://ai.google.dev)
- Built with [Material Design 3](https://m3.material.io/)
- Shimmer effect by [Facebook Shimmer](https://github.com/facebook/shimmer-android)
- Icons from Material Design Icons

## 📧 Contact

For issues or questions, please open an issue on [GitHub](https://github.com/NaimNajmios/Socurate/issues).

---

**Made with ⚽ for the Malaysian football community**
