# GitaVani — Voice of the Gita

A clean, ad-free iOS app for reading and studying the Bhagavad Gita. Sanskrit shlokas with Hindi and English translations from 20+ scholars and commentators.

## Why

Every Gita app out there is plagued with gaudy backgrounds, unreadable fonts, and intrusive ads. GitaVani is built to be the antidote — a beautiful, distraction-free reading experience with multiple translations, customizable themes, and offline access.

## Features (V1)

- All 18 chapters, 700 verses of the Bhagavad Gita
- Original Sanskrit text in Devanagari
- Romanized transliteration (toggleable)
- Hindi translations from multiple authors
- English translations from multiple authors
- 4 visual themes (Sattva, Parchment, Dusk, Lotus)
- Adjustable font size
- Auto-bookmark (resume where you left off)
- Swipe and button navigation between verses
- Fully offline — all data bundled locally
- Universal app (iPhone + iPad)

## Roadmap

- **V2**: Scholar commentaries, verse search, favorites
- **V3**: Sanskrit audio recitation
- **V4**: Additional Indian languages (Telugu, Tamil, Gujarati, Bengali, etc.)

## Tech Stack

- **App**: Swift / SwiftUI (iOS 17+)
- **Data**: Bundled JSON — no backend, no API calls
- **Data Source**: [Vedic Scriptures Bhagavad Gita API](https://vedicscriptures.github.io/) (GPL-3.0)
- **Data Pipeline**: Python script to fetch and normalize data

## Project Structure

```
GitaVani/
├── docs/               # Architecture documentation
├── scripts/             # Data pipeline (Python)
├── data/                # Generated JSON data
└── ios/                 # Xcode project (SwiftUI)
```

## Getting Started

### Data Pipeline

```bash
cd scripts
python3 fetch_gita_data.py
# Outputs: data/gita_data.json
```

### iOS App

Open `ios/GitaVani/GitaVani.xcodeproj` in Xcode and run on simulator or device.

## Data Source

All verse data sourced from the [Vedic Scriptures Bhagavad Gita API](https://github.com/vedicscriptures/bhagavad-gita), an open-source dataset with translations and commentaries from 20+ scholars including Swami Sivananda, Swami Ramsukhdas, Sri Shankaracharya, A.C. Bhaktivedanta Swami Prabhupada, and many others.

## License

Data: GPL-3.0 (inherited from source API)
App Code: GPL-3.0
