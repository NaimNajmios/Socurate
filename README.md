# Oreamnos (Android Edition)

A native Android application that transforms global football news into polished Malaysian Malay social media posts using AI. Perfect for football enthusiasts managing social media pages or community groups.

## 🎯 Features

- **Share Intent Integration**: Share articles directly from Chrome, Twitter, or any app
- **Universal Football Coverage**: Works for any team or league, not just Arsenal
- **AI-Powered Curation**: Uses Google Gemini to generate professional Malaysian Malay posts
- **Tone Customization**: Choose between formal (professional) or casual (fan banter) styles
- **URL Extraction**: Automatically extracts content from shared URLs
- **One-Tap Actions**: Copy to clipboard or share directly to X/Facebook
- **Secure Storage**: API keys encrypted using Android's EncryptedSharedPreferences

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
5. Copy or share the result

### Method 2: Direct Input

1. Open the Oreamnos app
2. Paste article text or URL into the input field
3. Tap the "Generate Post" button
4. Copy or share the generated post

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
