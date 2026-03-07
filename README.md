# GitaVani — Voice of the Gita

A clean, ad-free app for reading and studying the Bhagavad Gita. Available on iOS and Android. Sanskrit verse audio for all 701 verses, Sanskrit shlokas with Hindi and English translations from 12 scholars, commentaries from 17 scholars in 3 languages, search, favorites, and beautiful sharing.

## Why

Every Gita app out there is plagued with gaudy backgrounds, unreadable fonts, and intrusive ads. GitaVani is built to be the antidote — a beautiful, distraction-free reading experience with multiple translations, customizable themes, and offline access.

## Features

- Sanskrit verse audio — listen to all 701 verses chanted in Sanskrit
- All 18 chapters, 701 verses of the Bhagavad Gita
- Original Sanskrit text in Devanagari
- Romanized transliteration (on by default, toggleable)
- Hindi translations from 3 authors
- English translations from 9 authors
- Per-verse author picker — switch translators on any verse
- 11,000+ commentaries from 17 scholars in English, Hindi, and Sanskrit
- Search across all verses (Sanskrit, transliteration, translations)
- Save favorite verses with sort by recent or chapter order
- Share verses as beautiful themed image cards
- 4 visual themes (Sattva, Parchment, Dusk, Lotus)
- Adjustable font size (14-28pt) with Dynamic Type support
- Auto-bookmark (resume where you left off)
- Swipe and button navigation between verses (cross-chapter)
- First-launch onboarding walkthrough
- Help screen for feature discovery
- Fully offline — all data bundled locally
- Universal app (iPhone + iPad + Android phones + Android tablets)

## Roadmap

- **V4**: Additional Indian languages (Telugu, Tamil, Gujarati, Bengali, etc.)

## Tech Stack

| Platform | Technology |
|----------|-----------|
| iOS | Swift / SwiftUI (iOS 17+) |
| Android | Kotlin / Jetpack Compose (min API 26, Android 8.0+) |
| Data | Bundled JSON — no backend, no API calls |
| Data Source | [Vedic Scriptures Bhagavad Gita](https://github.com/vedicscriptures/bhagavad-gita) (LGPL-3.0 via [Kaggle](https://www.kaggle.com/datasets/ptprashanttripathi/bhagavad-gita-api-database)) |
| Data Pipeline | Python scripts to fetch, parse, and validate data |

## Project Structure

```
GitaVani/
├── .github/workflows/   # CI/CD (Android release on tag push)
├── docs/                # Architecture documentation
├── scripts/             # Data pipeline (Python)
│   ├── fetch_gita_data.py      # Fetch from API
│   ├── parse_gita_data.py      # Parse from local repo clone
│   └── validate_gita_data.py   # 8-section data validation
├── data/                # Generated JSON data (35.6 MB)
├── ios/GitaVani/        # Xcode project (SwiftUI)
├── android/GitaVani/    # Android Studio project (Kotlin + Compose)
├── fastlane/            # F-Droid metadata (auto-picked up by F-Droid)
├── appstore/            # iOS App Store metadata & screenshots
└── playstore/           # Google Play Store metadata & screenshots
```

## Getting Started

### Data Pipeline

```bash
# Fetch from API (takes ~4 min with rate limiting)
python3 scripts/fetch_gita_data.py

# Or parse from local repo clone (instant)
python3 scripts/parse_gita_data.py

# Validate the output
python3 scripts/validate_gita_data.py
```

### iOS App

Open `ios/GitaVani/GitaVani.xcodeproj` in Xcode and run on simulator or device (iOS 17+).

### Android App

Open `android/GitaVani/` in Android Studio and run on emulator or device (API 26+).

```bash
# Build debug APK
cd android/GitaVani && ./gradlew assembleDebug

# Build signed release AAB
cd android/GitaVani && ./gradlew bundleRelease
```

## Download

### Android
- **Direct APK**: Download from [GitHub Releases](https://github.com/nikhilsi/gitavani/releases) — install directly on any Android 8.0+ device
- **F-Droid**: Coming soon (submitted, pending review)
- **Play Store**: Coming soon (in review)

### iOS
- **App Store**: v1.0.0 submitted for review

## Help Get GitaVani on the Play Store

Google requires **12 testers** to opt in before an app can go to production on the Play Store. If you have an Android phone and want to help:

1. [Join the closed test](https://play.google.com/apps/testing/com.nikhilsi.gitavani)
2. Install the app from the Play Store link that appears
3. That's it — you're helping an open-source, ad-free Gita app reach everyone

No personal data collected. Takes 2 minutes. Thank you!

## Data Source

All verse data sourced from the [Vedic Scriptures Bhagavad Gita](https://github.com/vedicscriptures/bhagavad-gita) project by Pt. Prashant Tripathi. Includes translations and commentaries from 12 scholars including Swami Sivananda, Swami Ramsukhdas, Sri Shankaracharya, A.C. Bhaktivedanta Swami Prabhupada, and others.

Also available on [Kaggle](https://www.kaggle.com/datasets/ptprashanttripathi/bhagavad-gita-api-database) under LGPL-3.0.

## License

- **App Code**: MIT License (see [LICENSE](LICENSE))
- **Verse Data**: LGPL-3.0 (see [LICENSE-LGPL-3.0](LICENSE-LGPL-3.0))
- **Sanskrit Shlokas**: Public domain (ancient religious text)
