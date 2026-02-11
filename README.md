# GitaVani — Voice of the Gita

A clean, ad-free iOS app for reading and studying the Bhagavad Gita. Sanskrit shlokas with Hindi and English translations from 12 scholars and commentators.

## Why

Every Gita app out there is plagued with gaudy backgrounds, unreadable fonts, and intrusive ads. GitaVani is built to be the antidote — a beautiful, distraction-free reading experience with multiple translations, customizable themes, and offline access.

## Features (V1)

- All 18 chapters, 701 verses of the Bhagavad Gita
- Original Sanskrit text in Devanagari
- Romanized transliteration (on by default, toggleable)
- Hindi translations from 3 authors
- English translations from 9 authors
- Per-verse author picker — switch translators on any verse
- 4 visual themes (Sattva, Parchment, Dusk, Lotus)
- Adjustable font size (14-28pt, applies globally)
- Auto-bookmark (resume where you left off)
- Swipe and button navigation between verses (cross-chapter)
- First-launch onboarding walkthrough
- Help screen for feature discovery
- Fully offline — all data bundled locally
- Universal app (iPhone + iPad)

## Roadmap

- **V2**: Scholar commentaries, verse search, favorites
- **V3**: Sanskrit audio recitation
- **V4**: Additional Indian languages (Telugu, Tamil, Gujarati, Bengali, etc.)

## Tech Stack

- **App**: Swift / SwiftUI (iOS 17+)
- **Data**: Bundled JSON — no backend, no API calls
- **Data Source**: [Vedic Scriptures Bhagavad Gita](https://github.com/vedicscriptures/bhagavad-gita) (LGPL-3.0 via [Kaggle](https://www.kaggle.com/datasets/ptprashanttripathi/bhagavad-gita-api-database))
- **Data Pipeline**: Python scripts to fetch, parse, and validate data

## Project Structure

```
GitaVani/
├── docs/                # Architecture documentation
├── scripts/             # Data pipeline (Python)
│   ├── fetch_gita_data.py      # Fetch from API
│   ├── parse_gita_data.py      # Parse from local repo clone
│   └── validate_gita_data.py   # 8-section data validation
├── data/                # Generated JSON data (35.6 MB)
└── ios/GitaVani/        # Xcode project (SwiftUI)
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

## Data Source

All verse data sourced from the [Vedic Scriptures Bhagavad Gita](https://github.com/vedicscriptures/bhagavad-gita) project by Pt. Prashant Tripathi. Includes translations and commentaries from 12 scholars including Swami Sivananda, Swami Ramsukhdas, Sri Shankaracharya, A.C. Bhaktivedanta Swami Prabhupada, and others.

Also available on [Kaggle](https://www.kaggle.com/datasets/ptprashanttripathi/bhagavad-gita-api-database) under LGPL-3.0.

## License

- **App Code**: MIT License (see [LICENSE](LICENSE))
- **Verse Data**: LGPL-3.0 (see [LICENSE-LGPL-3.0](LICENSE-LGPL-3.0))
- **Sanskrit Shlokas**: Public domain (ancient religious text)
