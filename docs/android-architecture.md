# GitaVani Android — Architecture

**Last Updated**: February 19, 2026

## Overview

Native Android version of GitaVani using Kotlin + Jetpack Compose. Full feature parity with the iOS app. Same repo, `android/` folder alongside `ios/`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose (Material 3) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | 35 |
| JSON | kotlinx.serialization |
| Navigation | Navigation Compose |
| State | StateFlow + SharedPreferences |
| Audio | Android MediaPlayer |
| Build | Gradle 8.11.1 + AGP 8.9.1 |

**Zero third-party dependencies** — same philosophy as iOS.

## Project Structure

```
android/GitaVani/
├── app/src/main/
│   ├── java/com/nikhilsi/gitavani/
│   │   ├── GitaVaniApp.kt              # Application class
│   │   ├── MainActivity.kt             # Single activity, edge-to-edge, onboarding gate
│   │   ├── model/
│   │   │   ├── Chapter.kt              # @Serializable with @SerialName for snake_case
│   │   │   ├── Verse.kt
│   │   │   ├── Translation.kt          # Translation + Commentary data classes
│   │   │   ├── Language.kt             # Enum: HINDI, ENGLISH, SANSKRIT
│   │   │   ├── LocalizedText.kt        # forLanguage() + oppositeLanguage()
│   │   │   └── GitaData.kt             # GitaMetadata + GitaData wrapper
│   │   ├── data/
│   │   │   └── GitaDataService.kt      # Loads JSON from assets, O(1) lookups
│   │   ├── audio/
│   │   │   └── AudioService.kt         # MediaPlayer wrapper, play/pause/stop
│   │   ├── state/
│   │   │   ├── AppSettings.kt          # SharedPreferences + MutableStateFlow
│   │   │   └── ReadingProgress.kt      # Last chapter/verse tracking
│   │   ├── theme/
│   │   │   ├── AppTheme.kt             # 4 themes with exact iOS color values
│   │   │   └── Theme.kt               # MaterialTheme + LocalAppTheme
│   │   ├── ui/
│   │   │   ├── navigation/NavGraph.kt  # 7 routes: chapters, verses, settings, etc.
│   │   │   ├── chapters/
│   │   │   │   ├── ChapterListScreen.kt    # Home: header + search + chapter list
│   │   │   │   ├── ChapterDetailScreen.kt  # Summary + search + verse list
│   │   │   │   └── ChapterRowItem.kt       # Chapter number, name, verse count
│   │   │   ├── verses/
│   │   │   │   ├── VerseDetailScreen.kt     # HorizontalPager + prev/next
│   │   │   │   ├── ShlokSection.kt          # Sanskrit text + transliteration
│   │   │   │   ├── TranslationSection.kt    # Language toggle + author picker
│   │   │   │   ├── CommentarySection.kt     # 3-language + truncation
│   │   │   │   └── ActionBar.kt             # Play, Romanize, Save, Share
│   │   │   ├── settings/
│   │   │   │   ├── SettingsScreen.kt        # Theme, font, language, transliteration
│   │   │   │   └── AboutScreen.kt           # Version, credits, privacy, links
│   │   │   ├── favorites/
│   │   │   │   └── FavoritesScreen.kt       # Sort toggle + verse list
│   │   │   ├── onboarding/
│   │   │   │   └── OnboardingScreen.kt      # 4-page HorizontalPager
│   │   │   └── common/
│   │   │       ├── HelpScreen.kt            # Feature guide with icons
│   │   │       └── ResumeReadingBanner.kt   # Continue reading card
│   │   └── viewmodel/
│   │       └── GitaViewModel.kt         # AndroidViewModel: data, settings, audio
│   ├── assets/
│   │   ├── gita_data.json               # 35.6 MB bundled JSON
│   │   └── audio/                       # 701 MP3 files (126 MB)
│   └── res/
│       ├── values/strings.xml, themes.xml
│       ├── drawable/                    # Adaptive icon foreground/background
│       └── mipmap-*/                    # Legacy launcher icons
├── build.gradle.kts
├── gradle/libs.versions.toml           # Version catalog
└── local.properties                    # SDK path (gitignored)
```

## iOS → Android Mapping

| iOS | Android |
|-----|---------|
| SwiftUI | Jetpack Compose |
| @Observable | StateFlow + ViewModel |
| UserDefaults | SharedPreferences |
| NavigationStack | Navigation Compose |
| AVAudioPlayer | MediaPlayer |
| Bundle.main.url(forResource:) | assets/ + AssetManager |
| DragGesture | HorizontalPager |
| UINavigationBarAppearance | TopAppBar + MaterialTheme |
| @ScaledMetric | sp units (system scaling) |
| UIActivityViewController | Android Intent.ACTION_SEND |
| .searchable() | SearchBar composable |

## Data Flow

```
assets/gita_data.json (35.6 MB)
    │  loaded on Dispatchers.IO
    ▼
GitaDataService
    │  chapters, verses, verseById, versesByChapter
    ▼
GitaViewModel (AndroidViewModel)
    │  holds dataService, settings, readingProgress, audioService
    ▼
NavGraph → Compose Screens
    │  collect StateFlow values as Compose state
    ▼
UI renders with current theme, font size, language
```

## Navigation Routes

| Route | Screen |
|-------|--------|
| `chapter_list` | Home — book cover + chapters |
| `chapter_detail/{chapterNumber}` | Summary + verse list |
| `verse_detail/{verseId}` | Reading screen (HorizontalPager) |
| `settings` | Theme, font, language, transliteration |
| `about` | Credits, license, privacy |
| `help` | Feature guide |
| `favorites` | Saved verses with sort |

## Theme System

4 themes with exact same hex values as iOS:
- **Sattva** — white background, teal accent
- **Parchment** — cream background, amber accent
- **Dusk** — dark background, gold accent
- **Lotus** — saffron background, orange accent

Themes are `AppTheme` data classes. `GitaVaniTheme` composable wraps `MaterialTheme` and provides `LocalAppTheme` via `CompositionLocal`. Status bar adapts via `SystemBarStyle` (light icons for dark themes, dark icons for light themes).

## Build Commands

```bash
cd android/GitaVani
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Debug build
./gradlew assembleDebug

# Install on emulator
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release build (requires signing)
./gradlew assembleRelease
```

## APK Size

| Component | Size |
|-----------|------|
| Code + resources | ~6 MB |
| gita_data.json | 35.6 MB |
| Audio (701 MP3s) | 126 MB |
| **Total APK** | **~153 MB** |

For Play Store: will need Play Asset Delivery for audio files (150 MB AAB limit).
