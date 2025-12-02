# Oreamnos (Android Edition)

A native Android application that transforms global football news into polished Malaysian Malay social media posts using AI. Perfect for football enthusiasts managing social media pages or community groups.

## 🎯 Features

### Core Functionality
- **Share Intent Integration**: Share articles directly from Chrome, Twitter, or any app
- **Universal Football Coverage**: Works for any team or league worldwide
- **AI-Powered Curation**: Uses Google Gemini 2.0 Flash for professional Malaysian Malay posts
- **One-Tap Actions**: Copy to clipboard or share directly to X/Facebook

### Customization & Productivity
- **Tone Customization**: Choose between formal (professional) or casual (fan banter) styles
- **Hashtag Manager**: Configure default hashtags that auto-append to all posts
- **Quick Edit**: Edit generated posts inline before sharing
- **URL Extraction**: Automatically extracts content from shared URLs

### Technical Features
- **Intelligent Retry Logic**: Automatically retries failed API calls with exponential backoff
- **Rate Limit Handling**: Parses and respects API's requested retry delays
- **Secure Storage**: API keys encrypted using Android's EncryptedSharedPreferences
- **Comprehensive Logging**: Detailed logs for every user action and API call for debugging

## 📋 Requirements

- Android 7.0 (API 24) or higher
- Google Gemini API key ([Get one here](https://ai.google.dev))
- Internet connection

## 🚀 Installation

### Option 1: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/NaimNajmios/Oreamnos-Android.git
   cd Oreamnos-Android
   ```

2. Open the project in Android Studio

3. Build and run on your device or emulator

### Option 2: Install APK

Download the latest APK from the [Releases](https://github.com/NaimNajmios/Oreamnos-Android/releases) page.

## ⚙️ Setup

1. Launch the app
2. Tap the settings icon (⚙️)
3. Enter your Gemini API key
4. (Optional) Test the connection
5. (Optional) Choose your preferred tone (Formal/Casual)
6. Tap "Save Settings"

## 💡 Usage

### Method 1: Share from Other Apps

1. Open a football article in Chrome, Twitter, or any browser
2. Tap the "Share" button
3. Select "Oreamnos" from the share menu
4. The app automatically processes and generates your post
5. **NEW**: Toggle "Include hashtags" to auto-append your default hashtags
6. Copy or share the result

### Method 2: Direct Input

1. Open the Oreamnos app
2. Paste article text or URL into the input field
3. Tap the "Generate Post" button
4. **NEW**: Tap "Edit" to modify the generated text
5. **NEW**: Check "Include hashtags" box before copying
6. Copy or share the generated post

### NEW: Hashtag Management

1. Go to Settings → Hashtag Settings
2. Enter your default hashtags (e.g., `#BolaSepak #JDT #Football`)
3. Toggle "Auto-append hashtags" on/off
4. Hashtags automatically added when you copy or share posts

### NEW: Quick Edit

1. After generation, tap the "Edit" button
2. Make inline changes to the post
3. An "(Edited)" indicator appears
4. Tap "Save" to lock your changes
5. Copy/share the edited version

## 🏗️ Architecture

### Core Components

- **GeminiService**: Handles API communication with retry logic and exponential backoff
- **WebContentExtractor**: Extracts main content from URLs using Jsoup
- **PreferencesManager**: Securely manages API keys and app settings

### Activities

- **MainActivity**: Primary interface for manual input and generation
- **ShareReceiverActivity**: Handles share intents from other apps
- **SettingsActivity**: Configuration and API key management

## 🛠️ Technology Stack

- **Language**: Java
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Libraries**:
  - AndroidX (AppCompat, Material Design 3)
  - OkHttp (Networking)
  - Gson (JSON parsing)
  - Jsoup (HTML parsing)
  - Security Crypto (Encrypted preferences)

## 📁 Project Structure

```
app/src/main/
├── java/com/mycompany/oreamnos/
│   ├── MainActivity.java
│   ├── ShareReceiverActivity.java
│   ├── SettingsActivity.java
│   ├── services/
│   │   ├── GeminiService.java
│   │   └── WebContentExtractor.java
│   └── utils/
│       └── PreferencesManager.java
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_share_receiver.xml
│   │   └── activity_settings.xml
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── menu/
│       └── menu_main.xml
└── AndroidManifest.xml
```

## 🎨 Customization

### Changing the Tone

The app supports two tone styles:

- **Formal**: Professional, suitable for official club communications
- **Casual**: Engaging, conversational for fan communities

Change this in Settings → Post Settings → Tone

### Advanced Configuration

For advanced users, you can customize the API endpoint in Settings → Advanced Settings.

## 🔒 Privacy & Security

- API keys are stored using Android's EncryptedSharedPreferences
- No data is collected or sent to third parties
- All processing happens on-device except API calls to Google Gemini

## 🐛 Troubleshooting

### "API key required" error

Go to Settings and enter a valid Gemini API key.

### "Could not extract content from URL"

The website may be blocking automated access. Try copying the article text directly instead.

### Connection errors

- Check your internet connection
- Verify your API key is correct
- Try the "Test Connection" button in Settings

### Rate limit errors

The app now handles rate limits intelligently:
- Automatically retries with the exact delay requested by the API
- Shows user-friendly messages: "Please wait X seconds and try again"
- Logs all retry attempts in Logcat for debugging

### Debugging with Logcat

The app includes comprehensive logging for all actions:

1. Open Android Studio → Logcat tab
2. Filter by package: `com.mycompany.oreamnos`
3. Look for these tags:
   - `MainActivity` - UI interactions, button clicks
   - `GeminiService` - API calls, retries, responses
   - `WebContentExtractor` - URL parsing
   - `SettingsActivity` - Configuration changes

**Example logs:**
```
I/MainActivity: >>> Generate button clicked <<<
I/GeminiService: === GEMINI API CALL START [abc123] ===
I/GeminiService: [abc123] Response code: 200 (time: 1234ms)
I/MainActivity: Post generation SUCCESSFUL
```

See the [Debugging Guide](debugging_guide.md) for complete log examples.

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Original web app: [Oreamnos](https://github.com/NaimNajmios/Oreamnos)
- Powered by [Google Gemini API](https://ai.google.dev)
- Built with [Material Design 3](https://m3.material.io/)

## 📧 Contact

For issues or questions, please open an issue on GitHub or contact the maintainer.

---

**Made with ⚽ for the football community**
