# Socurate: Java to Kotlin/Jetpack Compose Migration Context

## 🎯 Project Overview

**Socurate** (package: `com.najmi.oreamnos`) is an Android app that transforms football news articles into Malaysian Malay social media posts using AI (Gemini, Groq, OpenRouter).

### Key Stats:
- **Current Language**: Java (100%)
- **Target**: Kotlin + Jetpack Compose
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **LOC Estimate**: ~180,000+ characters of Java code

---

## ✅ Preparation Already Completed

The following Compose enablement has already been done in `build.gradle` files:

### Root `build.gradle`
```gradle
plugins {
    id 'com.android.application' version '8.4.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
}
```

### `app/build.gradle`
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    // ...
    kotlinOptions {
        jvmTarget = '1.8'
    }
    buildFeatures {
        viewBinding true
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // Compose BOM
    def composeBom = platform('androidx.compose:compose-bom:2024.02.00')
    implementation composeBom
    
    // Material 3 + Compose UI
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    implementation 'androidx.activity:activity-compose:1.8.2'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
}
```

---

## 📁 Files to Migrate

### Activities (Primary Migration Targets)

| File | Size | Priority | Notes |
|------|------|----------|-------|
| `MainActivity.java` | 76KB | HIGH | Main screen with input, generation, editing, refinement |
| `SettingsActivity.java` | 29KB | HIGH | Multi-provider config, toggles, theme selection |
| `UsageActivity.java` | 26KB | MEDIUM | Token stats, charts, usage visualization |
| `ShareBottomSheetFragment.java` | 24KB | HIGH | Bottom sheet with progress bar, tone toggle |
| `LogListActivity.java` | 11KB | LOW | Debug log viewer |
| `HashtagManagerActivity.java` | 9KB | LOW | Hashtag CRUD management |
| `SessionListActivity.java` | 7KB | LOW | Session history viewer |
| `ShareReceiverActivity.java` | 3KB | LOW | Transparent share intent handler |

### Services Layer

| File | Location | Notes |
|------|----------|-------|
| `GeminiService.java` | `services/` | AI API calls with retry logic, multi-provider support |
| `ContentGenerationService.java` | `services/` | Background service for async generation |
| `WebContentExtractor.java` | `services/` | URL content extraction using Jsoup |
| `OpenRouterService.java` | `services/` | OpenRouter API integration |

### Utils Layer

| File | Location | Notes |
|------|----------|-------|
| `PreferencesManager.java` | `utils/` | EncryptedSharedPreferences wrapper |
| `NotificationHelper.java` | `utils/` | Progress notifications |
| `HapticHelper.java` | `utils/` | Vibration feedback |
| `StringUtils.java` | `utils/` | Emoji stripping, text utilities |
| `ReadabilityUtils.java` | `utils/` | Readability score calculation |

### Models

| File | Location | Notes |
|------|----------|-------|
| `GenerationPill.java` | `model/` | Custom refinement command model |
| `UsageStats.java` | `model/` | Token usage statistics model |

### ViewModels

Located in `viewmodel/`:
- ViewModel classes for MVVM architecture

### Curator Package

Located in `curator/`:
- 4 files related to content curation

---

## 🎨 UI/Design Assets (Keep As-Is)

These XML resources should NOT be migrated (Compose will use them):

```
res/
├── drawable/          # 15+ vector icons (ic_*.xml), shapes, gradients
├── anim/              # 6 animation XMLs (fade, slide, button press)
├── color/             # Color state selectors for chips
├── layout/            # XML layouts (will be replaced by Compose)
├── menu/              # Navigation menus
├── values/            # strings.xml, colors.xml, themes.xml
└── values-night/      # Dark theme overrides
```

### Key Theme Colors
- **Primary Dark**: Blue Grey `#90A4AE`
- **Primary Light**: Blue Grey `#455A64`
- **Background Dark**: `#121212`
- **Surface Dark**: `#1E2326`

---

## 🔧 Recommended Migration Strategy

### Phase 1: Foundation (Convert Non-UI First)
1. Convert `model/` classes to Kotlin data classes
2. Convert `utils/` to Kotlin
3. Convert `services/` to Kotlin (keep Java-compatible APIs)

### Phase 2: ViewModels
1. Convert ViewModels to Kotlin
2. Use StateFlow instead of LiveData
3. Add Compose-specific state handling

### Phase 3: UI - Activity by Activity
1. Start with `ShareReceiverActivity.java` (smallest, 3KB)
2. Move to `HashtagManagerActivity.java` (simple CRUD)
3. Then `SettingsActivity.java` (Settings is a good Compose candidate)
4. Finally `MainActivity.java` (largest, most complex)

### Phase 4: Bottom Sheet
1. Convert `ShareBottomSheetFragment.java` to Compose BottomSheet

---

## ⚠️ Migration Considerations

### Keep Java-Kotlin Interop
- Some files may need to stay Java temporarily
- Use `@JvmStatic`, `@JvmField` annotations where needed
- Ensure null-safety at boundaries

### EncryptedSharedPreferences
- `PreferencesManager` uses AndroidX Security
- Works fine with Kotlin

### Third-Party Libraries (All Kotlin-Compatible)
- OkHttp 4.x - Kotlin-first
- Gson - Works with Kotlin, consider kotlinx.serialization
- Jsoup - Java library, works fine
- Markwon - Works with Compose (markwon-compose exists)
- Shimmer - XML-based, may need Compose alternative

### Compose Alternatives Needed
- Shimmer → Use `Modifier.placeholder()` from accompanist
- Markwon → Use `markwon-compose` or compose-native markdown

---

## 🔗 Key Dependencies to Add (Optional)

```gradle
// Navigation Compose (if needed)
implementation 'androidx.navigation:navigation-compose:2.7.7'

// Accompanist utilities
implementation 'com.google.accompanist:accompanist-placeholder-material:0.34.0'

// Kotlin serialization (replace Gson)
implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'

// Coil for image loading
implementation 'io.coil-kt:coil-compose:2.5.0'
```

---

## 📝 Quick Start Commands

```bash
# Navigate to project
cd "c:\Users\NAIM\Documents\Personal\Project\Socurate"

# Sync Gradle (in Android Studio)
# File → Sync Project with Gradle Files

# Build to verify Kotlin setup
./gradlew assembleDebug

# Convert single file (Android Studio)
# Right-click .java file → Convert Java File to Kotlin File
```

---

## 🎯 Success Criteria

1. All Java files converted to Kotlin
2. Activities using Jetpack Compose for UI
3. ViewModels using StateFlow
4. App compiles and runs without regression
5. All features work identically to Java version

---

## 📞 Contact/Resources

- **Repository**: https://github.com/NaimNajmios/Socurate
- **Package**: `com.najmi.oreamnos`
- **Namespace**: Same as package

---

*Context document created: 2025-12-24*
