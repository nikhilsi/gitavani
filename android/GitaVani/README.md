# GitaVani Android

Native Android version of GitaVani — a clean, ad-free Bhagavad Gita reader built with Kotlin and Jetpack Compose.

## Requirements

- Android Studio Panda Patch 1 (or newer)
- JDK 21 (bundled with Android Studio)
- Android SDK 35
- Android emulator or physical device (API 26+)

## Getting Started

### 1. Open in Android Studio

1. Open Android Studio
2. **File > Open** and select `android/GitaVani/`
3. Wait for Gradle sync to complete (1-2 minutes first time)

### 2. Run on Emulator

1. Select a device from the device dropdown (e.g. "Medium Phone API 36.1")
2. Click the green **Run** button (or `Ctrl+R`)
3. Wait ~60 seconds for the 35.6 MB JSON data to load on the emulator (much faster on real devices)

### 3. Build from Terminal

```bash
cd android/GitaVani
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Build release AAB (requires signing config)
./gradlew bundleRelease
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.1.10 |
| UI Framework | Jetpack Compose (Material 3) |
| Min SDK | API 26 (Android 8.0, covers 95%+ devices) |
| Target SDK | 35 |
| JSON Parsing | kotlinx.serialization 1.7.3 |
| Navigation | Navigation Compose 2.8.5 |
| State Management | StateFlow + SharedPreferences |
| Audio | Android MediaPlayer |
| Build System | Gradle 8.11.1 + AGP 8.9.1 |
| Compose BOM | 2024.12.01 |

**Zero third-party dependencies.** Everything is first-party Jetpack / kotlinx.

## Project Structure

```
app/src/main/
├── java/com/nikhilsi/gitavani/
│   ├── GitaVaniApp.kt                  # Application class
│   ├── MainActivity.kt                 # Single Activity, edge-to-edge, onboarding gate
│   │
│   ├── model/                          # Data models (@Serializable)
│   │   ├── Chapter.kt                  # Chapter with localized name, summary, meaning
│   │   ├── Verse.kt                    # Verse with slok, transliteration, translations, commentaries
│   │   ├── Translation.kt              # Translation + Commentary data classes
│   │   ├── Language.kt                 # Enum: HINDI, ENGLISH, SANSKRIT
│   │   ├── LocalizedText.kt            # Hindi/English text pair with forLanguage()
│   │   └── GitaData.kt                 # Top-level wrapper (metadata + chapters + verses)
│   │
│   ├── data/
│   │   └── GitaDataService.kt          # Loads JSON from assets on Dispatchers.IO
│   │                                    # Builds O(1) lookup maps: verseById, versesByChapter
│   │
│   ├── audio/
│   │   └── AudioService.kt             # MediaPlayer wrapper — play/pause/stop from assets
│   │
│   ├── state/
│   │   ├── AppSettings.kt              # SharedPreferences-backed settings with StateFlow
│   │   │                                # (theme, fontSize, language, transliteration, favorites,
│   │   │                                #  preferred authors, onboarding flag)
│   │   └── ReadingProgress.kt          # Last-read chapter/verse tracking
│   │
│   ├── theme/
│   │   ├── AppTheme.kt                 # 4 themes: Sattva, Parchment, Dusk, Lotus
│   │   │                                # (exact same hex values as iOS)
│   │   └── Theme.kt                    # GitaVaniTheme composable + LocalAppTheme
│   │
│   ├── viewmodel/
│   │   └── GitaViewModel.kt            # AndroidViewModel holding all services
│   │
│   └── ui/
│       ├── navigation/
│       │   └── NavGraph.kt             # 7 routes: chapters, verses, settings, help, about, favorites
│       │
│       ├── chapters/
│       │   ├── ChapterListScreen.kt    # Home: book cover header, search, resume banner, chapter list
│       │   ├── ChapterDetailScreen.kt  # Summary card (expand/collapse), chapter search, verse list
│       │   └── ChapterRowItem.kt       # Chapter number, transliteration, Hindi name, verse count
│       │
│       ├── verses/
│       │   ├── VerseDetailScreen.kt    # HorizontalPager for swipe, prev/next buttons, reading progress
│       │   ├── ShlokSection.kt         # Sanskrit Devanagari text + optional transliteration
│       │   ├── TranslationSection.kt   # English/Hindi toggle, author picker chips, translation text
│       │   ├── CommentarySection.kt    # 3-language toggle, author picker, 1500-char truncation
│       │   └── ActionBar.kt            # Play, Romanize, Save, Share — 4 icon buttons
│       │
│       ├── settings/
│       │   ├── SettingsScreen.kt       # Theme picker (2x2 grid), font slider, language, transliteration
│       │   └── AboutScreen.kt          # Version, credits, data source, license, privacy, links
│       │
│       ├── favorites/
│       │   └── FavoritesScreen.kt      # Sort toggle (Recent/Chapter), verse list with heart remove
│       │
│       ├── onboarding/
│       │   └── OnboardingScreen.kt     # 4-page HorizontalPager, page dots, Next/Skip/Get Started
│       │
│       └── common/
│           ├── HelpScreen.kt           # 10-item feature guide with Material icons
│           └── ResumeReadingBanner.kt   # "Continue Reading" card on home screen
│
├── assets/
│   ├── gita_data.json                   # 35.6 MB — 18 chapters, 701 verses, all translations/commentaries
│   └── audio/                           # 701 MP3 files (126 MB total, 96kbps each)
│       ├── BG1.1.mp3
│       ├── BG1.2.mp3
│       └── ... (701 files)
│
└── res/
    ├── values/
    │   ├── strings.xml                  # App name
    │   └── themes.xml                   # Base theme (transparent status/nav bars)
    ├── drawable/
    │   ├── ic_launcher_foreground.xml   # Adaptive icon: white book on saffron
    │   └── ic_launcher_background.xml   # Saffron solid color
    └── mipmap-*/
        └── ic_launcher.png              # Legacy icons for pre-API 26
```

## Features

All features match the iOS version:

- **Reading**: All 701 verses across 18 chapters in Sanskrit Devanagari
- **Audio**: Sanskrit verse recitation for every verse (play/pause, auto-stop on navigation)
- **Transliteration**: Romanized Sanskrit text (toggleable)
- **Translations**: Hindi (3 authors) and English (9 authors) with per-verse author picker
- **Commentaries**: 11,000+ commentaries from 17 scholars in English, Hindi, and Sanskrit
- **Search**: Global search from home screen, chapter-scoped search from chapter detail (300ms debounce)
- **Favorites**: Heart icon to save verses, favorites screen with Recent/Chapter Order sort
- **Share**: Share verse text (Sanskrit + translation) via Android share sheet
- **4 Themes**: Sattva (white/teal), Parchment (cream/amber), Dusk (dark/gold), Lotus (saffron/orange)
- **Font Size**: Adjustable 14-28pt with live preview
- **Resume Reading**: Auto-bookmarks current verse, "Continue Reading" banner on home
- **Navigation**: Swipe between verses (HorizontalPager) + Previous/Next buttons (cross-chapter)
- **Onboarding**: 4-page walkthrough on first launch
- **Help**: Feature guide accessible from home screen
- **Settings**: Theme picker, font size, default language, transliteration toggle
- **About**: Credits, data source, license (LGPL-3.0), privacy policy, support links
- **Offline**: Everything bundled locally — works without internet

## Architecture

```
assets/gita_data.json (35.6 MB)
    │  parsed on Dispatchers.IO via kotlinx.serialization
    ▼
GitaDataService (chapters, verses, O(1) lookups)
    │
    ▼
GitaViewModel (AndroidViewModel)
    │  holds: dataService, settings, readingProgress, audioService
    │  exposes: currentTheme, isLoading, loadError via StateFlow
    ▼
NavGraph (Navigation Compose)
    │  7 routes → 10 Compose screens
    ▼
UI (Jetpack Compose + Material 3)
    │  each screen collects StateFlow as Compose state
    ▼
User sees themed, reactive UI
```

## Navigation Routes

| Route | Screen | Description |
|-------|--------|-------------|
| `chapter_list` | ChapterListScreen | Home: header, search, resume banner, 18 chapters |
| `chapter_detail/{n}` | ChapterDetailScreen | Summary, search, verse list for chapter n |
| `verse_detail/{id}` | VerseDetailScreen | Reading screen with swipe (e.g. `BG2.47`) |
| `settings` | SettingsScreen | Theme, font, language, transliteration |
| `about` | AboutScreen | Credits, license, privacy |
| `help` | HelpScreen | Feature guide |
| `favorites` | FavoritesScreen | Saved verses with sort |

## iOS Parity

This app is a direct port from the iOS version with these mappings:

| iOS (SwiftUI) | Android (Compose) |
|---------------|-------------------|
| `@Observable` | `StateFlow` + `ViewModel` |
| `UserDefaults` | `SharedPreferences` |
| `NavigationStack` | `Navigation Compose` |
| `AVAudioPlayer` | `MediaPlayer` |
| `DragGesture` | `HorizontalPager` |
| `.searchable()` | `SearchBar` composable |
| `UIActivityViewController` | `Intent.ACTION_SEND` |
| `@ScaledMetric` | `sp` units (system scaling) |
| `Bundle.main.url(forResource:)` | `assets/` + `AssetManager` |

## APK Size

| Component | Size |
|-----------|------|
| Code + resources | ~6 MB |
| gita_data.json | 35.6 MB |
| Audio (701 MP3s) | 126 MB |
| **Total debug APK** | **~153 MB** |

For Play Store distribution, audio files will use Play Asset Delivery (install-time asset pack) to stay within the 150 MB AAB limit.

## Known Limitations

- JSON parsing takes ~60s on emulator (fast on real devices)
- Share is text-only (iOS has themed image card — planned for Android)
- No Play Store listing yet (needs $25 developer account + signed release build)

## License

- **App Code**: MIT License
- **Verse Data**: LGPL-3.0 (sourced from [Vedic Scriptures project](https://github.com/vedicscriptures/bhagavad-gita))
- **Sanskrit Shlokas**: Public domain (ancient religious text)
- **Audio**: Sourced from shlokam.org
