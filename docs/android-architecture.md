# GitaVani Android — Architecture

**Last Updated**: March 7, 2026

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
│   │   ├── chant_data.json              # Word by word meanings (~1 MB, built by scripts/build_chant_data.py)
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
| `learn/{verseId}` | Learn lesson (Abhyāsa), Android 13+ |

## Learn (Abhyāsa)

The chant coach. Full design: `docs/chant-coach-spec.md`.

- **Listening:** `learn/ChantListener.kt`. AudioRecord (VOICE_RECOGNITION, 16 kHz mono) feeds the on-device `SpeechRecognizer` (hi-IN) through a pipe, from a plain thread. One recognizer serves the whole lesson (the on-device service allows one session at a time). A watchdog ends a session after 5 s with no sign of life. The language check gives up after 10 s. Calibration decodes the verse MP3 with MediaCodec and resamples to 16 kHz.
- **Scoring:** `learn/ChantScorer.kt`, a port of `scripts/chant_scorer.py`, plus `devaToIast` because the recognizer writes Devanagari. Tests: `./gradlew testDebugUnitTest` (JUnit 4, test-only).
- **Lesson:** `ui/learn/LessonState.kt` (steps, calibration, takes) and `LessonScreen.kt`. Learn button shown when `deviceCanLearn()` and the verse's `learn` flag are true.
- **Progress:** `state/LearnProgress.kt`, key `learnProgress` in `gitavani_progress` prefs.
- **Permission:** RECORD_AUDIO, asked when a lesson opens.
- **Debug aids:** `adb shell am start -n com.nikhilsi.gitavani.debug/com.nikhilsi.gitavani.MainActivity --es openVerse BG4.7 --ez openLesson true` opens a lesson. `src/debug/.../ListenerSelfTestActivity.kt` checks the listener on a phone.

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

# Release APK (requires signing)
./gradlew assembleRelease

# Release AAB (for Play Store)
./gradlew bundleRelease

# Unit tests (Learn scorer against scripts/data/scorer_fixtures.json)
./gradlew testDebugUnitTest
```

## Release Size

| Component | Size |
|-----------|------|
| Code + resources | ~6 MB |
| gita_data.json | 35.6 MB |
| chant_data.json | ~1 MB |
| Audio (701 MP3s) | 126 MB |
| **Release AAB** | **134 MB** (under 150 MB Play Store limit) |

R8 minification (isMinifyEnabled + isShrinkResources) keeps the AAB under 150 MB. No Play Asset Delivery needed.

## Signing

- Keystore: `keystore/gitavani-release.jks` (gitignored)
- Credentials: `keystore.properties` (gitignored)
- `build.gradle.kts` reads signing config conditionally — builds succeed without keystore (for F-Droid)

## Distribution

| Channel | How | Auto-update |
|---------|-----|-------------|
| GitHub Releases | Push `v*` tag → GitHub Actions builds APK + AAB | Yes (on tag) |
| F-Droid | Live since Apr 27, 2026 (MR #34390 merged) | Yes (detects new tags) |
| Google Play | Upload AAB manually in Play Console | Manual |

### Release Process
1. Bump `versionCode` (integer) and `versionName` in `app/build.gradle.kts`
2. Commit and tag: `git tag v1.1.0 && git push origin main --tags`
3. GitHub Actions auto-builds and publishes to GitHub Releases
4. F-Droid auto-detects the new tag (1-2 week build cycle)
5. Play Store: manually upload new AAB in Play Console
