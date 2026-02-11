# GitaVani — Architecture Document

> **Voice of the Gita** — A personal Bhagavad Gita reader app for iOS
> Created: February 2026
> Author: Nikhil
> Status: V1 Complete

---

## 1. Project Overview

### 1.1 Purpose
GitaVani is a clean, ad-free iOS app for reading and studying the Bhagavad Gita. Built as a personal gift, it prioritizes readability, simplicity, and a beautiful reading experience over feature bloat.

### 1.2 Target Users
- Primary: Nikhil's wife — English-first reader learning Hindi, part of a Gita study group
- Secondary: Family members and study group members

### 1.3 Platforms
- iPhone (primary, tested on iPhone 15 Pro)
- iPad (universal app — SwiftUI handles layout adaptation)

### 1.4 Phased Roadmap

| Phase | Features | Status |
|-------|----------|--------|
| **V1** | Chapter list, verse reader, Sanskrit/Hindi/English translations, 4 themes, bookmarking, transliteration, onboarding, help | Complete |
| **V2** | Commentaries/explanations from scholars, search across verses, favorites/bookmarks list | Next |
| **V3** | Audio recitation of Sanskrit verses, read-along mode | Future |
| **V4** | Additional languages (Telugu, Tamil, Gujarati, Bengali, etc.), study group sharing features | Future |

---

## 2. Data Source

### 2.1 API: Vedic Scriptures Bhagavad Gita API
- **Base URL:** `https://vedicscriptures.github.io`
- **License:** GPL-3.0
- **Type:** Static REST API hosted on GitHub Pages (free, no API key, no rate limits)

### 2.2 Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /chapters` | All 18 chapters with metadata |
| `GET /chapter/:ch` | Single chapter info |
| `GET /slok/:ch/:sl` | Specific verse with all translations and commentaries |

### 2.3 Data Strategy: Bundled Local JSON

**Decision: Bundle all data locally with the app. No runtime API calls for V1.**

Rationale:
- The Gita is a static text — it never changes
- 701 verses with all translations and commentaries ≈ 35.6 MB — manageable
- Offline-first is essential for a reader app (commute, travel, no-wifi scenarios)
- Instant loading with zero network dependency
- No API downtime risk

### 2.4 Data Pipeline

Two Python scripts in `scripts/`:

1. **`fetch_gita_data.py`** — Fetches from API, normalizes, outputs `data/gita_data.json`
2. **`parse_gita_data.py`** — Reads from local repo clone (`vedicscriptures.github.io`), same normalization, outputs `data/gita_data_parsed.json`
3. **`validate_gita_data.py`** — 8-section validation: structure, chapters, verse continuity, content quality, author coverage, commentary quality, Swift compatibility, data size

Both fetch and parse scripts produce byte-identical output. Validated against local repo clone for data integrity.

### 2.5 Raw API Structure (per verse)

The API returns each translator as a separate key with inconsistent field names:
```json
{
  "_id": "BG1.1",
  "chapter": 1,
  "verse": 1,
  "slok": "Sanskrit in Devanagari...",
  "transliteration": "Romanized Sanskrit...",
  "tej": { "author": "Swami Tejomayananda", "ht": "Hindi translation..." },
  "siva": { "author": "Swami Sivananda", "et": "English translation...", "ec": "English commentary..." },
  "rams": { "author": "Swami Ramsukhdas", "ht": "Hindi translation...", "hc": "Hindi commentary..." },
  "prabhu": { "author": "A.C. Bhaktivedanta Swami Prabhupada", "et": "English translation...", "ec": "English commentary..." }
}
```

Field key conventions in raw API:
- `ht` = Hindi translation
- `et` = English translation
- `hc` = Hindi commentary
- `ec` = English commentary
- `sc` = Sanskrit commentary

### 2.6 Normalized Structure (what we bundle)

```json
{
  "metadata": {
    "source": "Vedic Scriptures API",
    "source_url": "https://vedicscriptures.github.io",
    "license": "GPL-3.0",
    "generated_at": "2026-02-11T...",
    "total_chapters": 18,
    "total_verses": 701
  },
  "chapters": [
    {
      "chapter_number": 1,
      "verses_count": 47,
      "name": "अर्जुनविषादयोग",
      "translation": "Arjuna Visada Yoga",
      "transliteration": "Arjun Viṣhād Yog",
      "meaning": { "en": "Arjuna's Dilemma", "hi": "अर्जुन विषाद योग" },
      "summary": { "en": "...", "hi": "..." }
    }
  ],
  "verses": [
    {
      "id": "BG1.1",
      "chapter": 1,
      "verse": 1,
      "slok": "Sanskrit Devanagari text...",
      "transliteration": "Romanized text...",
      "translations": [
        { "author": "Swami Tejomayananda", "language": "hindi", "text": "..." },
        { "author": "Swami Sivananda", "language": "english", "text": "..." }
      ],
      "commentaries": [
        { "author": "Swami Sivananda", "language": "english", "text": "..." },
        { "author": "Sri Shankaracharya", "language": "sanskrit", "text": "..." }
      ]
    }
  ]
}
```

### 2.7 Data Stats

| Metric | Value |
|--------|-------|
| Chapters | 18 |
| Verses | 701 |
| Hindi translations | 2,050 (3 authors) |
| English translations | 6,133 (9 authors) |
| Commentaries | 11,189 (V2 feature) |
| File size | 35.6 MB |

### 2.8 Available Translators

**Hindi Translations (3 authors):**
- Swami Tejomayananda
- Swami Ramsukhdas
- Sri Shankaracharya

**English Translations (9 authors):**
- Swami Sivananda
- Shri Purohit Swami
- Swami Adidevananda
- Swami Gambirananda
- Dr. S. Sankaranarayan
- A.C. Bhaktivedanta Swami Prabhupada
- Sri Ramanuja
- Sri Abhinav Gupta
- Sri Shankaracharya

All translators included. User can switch per-verse via author picker.

### 2.9 Known Data Issues
- 2 verses (BG12.3, BG12.18) have only 1 translation — other scholars did not comment
- 2 commentaries have U+FFFD encoding issues (BG8.1, BG12.18) — upstream source data

---

## 3. Data Model (Swift)

### 3.1 Chapter

```swift
struct Chapter: Codable, Identifiable {
    let chapterNumber: Int
    let versesCount: Int
    let name: String              // Sanskrit name in Devanagari
    let translation: String       // English name
    let transliteration: String   // Romanized name
    let meaning: LocalizedText
    let summary: LocalizedText

    var id: Int { chapterNumber }
}
```

### 3.2 Verse

```swift
struct Verse: Codable, Identifiable {
    let id: String                // "BG1.1"
    let chapter: Int
    let verse: Int
    let slok: String              // Sanskrit in Devanagari
    let transliteration: String   // Romanized Sanskrit
    let translations: [Translation]
    let commentaries: [Commentary]
}
```

### 3.3 Translation & Commentary

```swift
struct Translation: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language.rawValue)" }
}

struct Commentary: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language.rawValue)-commentary" }
}

enum Language: String, Codable, CaseIterable {
    case hindi
    case english
    case sanskrit
}
```

### 3.4 LocalizedText

```swift
struct LocalizedText: Codable {
    let en: String
    let hi: String
}
```

### 3.5 GitaData (top-level wrapper)

```swift
struct GitaMetadata: Codable {
    let source: String
    let sourceUrl: String
    let license: String
    let generatedAt: String
    let totalChapters: Int
    let totalVerses: Int
}

struct GitaData: Codable {
    let metadata: GitaMetadata
    let chapters: [Chapter]
    let verses: [Verse]
}
```

JSON uses `keyDecodingStrategy = .convertFromSnakeCase` so `chapter_number` maps to `chapterNumber` automatically.

---

## 4. Screen Flow

```
┌──────────────────────────────────────────────────┐
│                                                  │
│   First Launch: Onboarding (4 swipeable pages)   │
│   - Welcome to GitaVani                          │
│   - Multiple Translations                        │
│   - Transliteration                              │
│   - Make It Yours (themes/fonts)                 │
│   [Skip] / [Next] / [Get Started]                │
│                                                  │
│   ↓ After onboarding (or on subsequent launches) │
│                                                  │
│   Home (Chapter List)              [?]     [⚙]   │
│   ┌──────────────────────────────────────────┐   │
│   │       📖 GitaVani                        │   │
│   │    The Bhagavad Gita                     │   │
│   │    18 chapters · 701 verses              │   │
│   ├──────────────────────────────────────────┤   │
│   │ ▸ Continue Reading: Ch 2, Verse 14       │   │
│   ├──────────────────────────────────────────┤   │
│   │ Ch 1 · अर्जुनविषादयोग                     │   │
│   │        Arjuna's Dilemma · 47 verses      │   │
│   ├──────────────────────────────────────────┤   │
│   │ Ch 2 · सांख्ययोग                           │   │
│   │        Transcendental Knowledge · 72 v.  │   │
│   ├──────────────────────────────────────────┤   │
│   │ ...                                      │   │
│   └──────────────────────────────────────────┘   │
│                                                  │
│   ↓ Tap chapter                                  │
│                                                  │
│   Chapter Detail (Verse List)                    │
│   ┌──────────────────────────────────────────┐   │
│   │ Chapter Summary (3-line preview)         │   │
│   │ Read more...                             │   │
│   ├──────────────────────────────────────────┤   │
│   │ 1.1  धृतराष्ट्र उवाच | धर्मक्षेत्रे...       │   │
│   ├──────────────────────────────────────────┤   │
│   │ 1.2  सञ्जय उवाच | दृष्ट्वा तु...           │   │
│   ├──────────────────────────────────────────┤   │
│   │ ...                                      │   │
│   └──────────────────────────────────────────┘   │
│                                                  │
│   ↓ Tap verse                                    │
│                                                  │
│   Verse Detail (Main Reading Screen)             │
│   ┌──────────────────────────────────────────┐   │
│   │ Chapter 1 · Verse 1           [📖 toggle]│   │
│   │                                          │   │
│   │ धृतराष्ट्र उवाच |                          │   │
│   │ धर्मक्षेत्रे कुरुक्षेत्रे समवेता युयुत्सवः |    │   │
│   │ मामकाः पाण्डवाश्चैव किमकुर्वत सञ्जय ||    │   │
│   │                                          │   │
│   │ dhṛtarāṣṭra uvāca ...  (transliteration)│   │
│   │                                          │   │
│   │ [English] [Hindi]                        │   │
│   │ [Author 1] [Author 2] [Author 3]        │   │
│   │                                          │   │
│   │ "Dhritarashtra said: O Sanjaya, what     │   │
│   │  did my sons and the sons of Pandu do    │   │
│   │  when they assembled..."                 │   │
│   │                          — Swami Sivananda│   │
│   │                                          │   │
│   │          [← Prev]  [Next →]              │   │
│   └──────────────────────────────────────────┘   │
│   ← Swipe left/right also navigates verses →     │
│                                                  │
│   Settings Screen                                │
│   ┌──────────────────────────────────────────┐   │
│   │ Theme: [Sattva] [Parchment]              │   │
│   │        [Dusk]   [Lotus]                  │   │
│   │ Font Size: [A ─────●──── A]              │   │
│   │ Default Language: [English] [Hindi]      │   │
│   │ Transliteration: [toggle]                │   │
│   └──────────────────────────────────────────┘   │
│                                                  │
│   Help Screen (accessible via ? icon)            │
│   ┌──────────────────────────────────────────┐   │
│   │ How to use GitaVani                      │   │
│   │ - Navigate verses (swipe/buttons)        │   │
│   │ - Switch language                        │   │
│   │ - Transliteration                        │   │
│   │ - Change theme                           │   │
│   │ - Adjust font size                       │   │
│   │ - Resume reading                         │   │
│   │ - Chapter summary                        │   │
│   └──────────────────────────────────────────┘   │
│                                                  │
└──────────────────────────────────────────────────┘
```

### 4.1 Navigation Pattern
- **NavigationStack** with programmatic `NavigationPath` for drill-down: Home → Chapter → Verse
- **DragGesture + buttons** for verse-to-verse navigation (works across chapter boundaries)
- **Settings** accessible via gear icon from Home screen
- **Help** accessible via ? icon from Home screen
- **Resume reading** banner on Home screen (taps navigate directly to saved verse)
- **Onboarding** shown as full-screen cover on first launch only

---

## 5. SwiftUI View Hierarchy

```
GitaVaniApp.swift                    (App entry point — creates shared services)
│
├── ContentView.swift                (Root view with NavigationStack + onboarding)
│
├── Views/
│   ├── Onboarding/
│   │   └── OnboardingView.swift     (4-page first-launch walkthrough)
│   │
│   ├── Chapters/
│   │   ├── ChapterListView.swift    (Home screen — book cover header + chapter list)
│   │   ├── ChapterRowView.swift     (Single chapter row component)
│   │   └── ChapterDetailView.swift  (Chapter summary snippet + verse list)
│   │
│   ├── Verses/
│   │   ├── VerseListRowView.swift   (Single verse row in chapter detail)
│   │   ├── VerseDetailView.swift    (Main reading screen — swipe + nav buttons)
│   │   ├── ShlokView.swift          (Sanskrit text + transliteration toggle)
│   │   ├── TranslationView.swift    (Language toggle + author picker + text)
│   │   └── VerseNavigationView.swift (Prev/Next buttons)
│   │
│   ├── Settings/
│   │   ├── SettingsView.swift       (Main settings screen)
│   │   ├── ThemePickerView.swift    (Visual 2x2 grid theme selector)
│   │   └── FontSizeView.swift       (Font size slider with preview)
│   │
│   └── Common/
│       ├── ResumeReadingBanner.swift (Shows on Home — tap to resume)
│       └── HelpView.swift           (Feature guide for older users)
│
├── Models/
│   ├── Language.swift
│   ├── LocalizedText.swift
│   ├── Translation.swift            (includes Commentary)
│   ├── Chapter.swift
│   ├── Verse.swift
│   └── GitaData.swift               (includes GitaMetadata)
│
├── Services/
│   └── GitaDataService.swift        (Loads & parses bundled JSON)
│
├── State/
│   ├── AppSettings.swift            (Font size, language, transliteration, preferred authors)
│   └── ReadingProgress.swift        (Last read chapter/verse)
│
├── Theme/
│   ├── AppTheme.swift               (Theme definitions — 4 themes with colors)
│   └── ThemeManager.swift           (Theme switching + UINavigationBar appearance)
│
├── Resources/
│   └── gita_data.json               (Bundled dataset — 35.6 MB)
│
└── Assets.xcassets/
    └── AppIcon.appiconset/          (Lotus + book icon, saffron gradient)
```

---

## 6. State Management

### 6.1 Persisted State (survives app restart)

Using `@Observable` classes with `UserDefaults` persistence:

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `selectedTheme` | String | "sattva" | Current visual theme |
| `fontSize` | Double | 18.0 | Base font size |
| `defaultLanguage` | String | "english" | Default translation language |
| `showTransliteration` | Bool | true | Transliteration toggle |
| `lastReadChapter` | Int | 0 | Bookmark — last chapter |
| `lastReadVerse` | Int | 0 | Bookmark — last verse |
| `preferredHindiAuthor` | String | "" | Preferred Hindi translator |
| `preferredEnglishAuthor` | String | "" | Preferred English translator |
| `hasSeenOnboarding` | Bool | false | First-launch onboarding flag |

### 6.2 Transient State (in-memory only)

Using `@State`:
- Navigation path (NavigationStack)
- Currently selected language tab (Hindi/English) per verse view
- Currently selected author per verse view
- Chapter summary expanded/collapsed
- Current verse ID during navigation

---

## 7. Theming System

### 7.1 Theme Definitions

| Theme | Background | Primary Text | Secondary Text | Accent | Vibe |
|-------|-----------|-------------|---------------|--------|------|
| **Sattva** | White (#FFFFFF) | Dark Gray (#1A1A1A) | Gray (#666666) | Teal (#008080) | Clean, modern |
| **Parchment** | Cream (#F5F0E8) | Dark Brown (#3E2C1C) | Brown (#7A6552) | Burnt Orange (#C17817) | Classic book |
| **Dusk** | Dark (#1C1C2E) | Light (#E8E4DC) | Muted Gold (#A89880) | Gold (#D4A843) | Night reading |
| **Lotus** | Soft Saffron (#FFF5E6) | Deep Red-Brown (#4A1C1C) | Warm Gray (#8B7355) | Saffron (#FF6B00) | Devotional |

### 7.2 Theme Structure

```swift
struct AppTheme {
    let name: String
    let displayName: String
    let backgroundColor: Color
    let primaryTextColor: Color
    let secondaryTextColor: Color
    let accentColor: Color
    let cardBackgroundColor: Color
}
```

### 7.3 Theme Application
- **ThemeManager** is `@Observable`, updates `UINavigationBarAppearance` on theme change
- Navigation bar background and title colors honor the active theme
- Status bar adapts via `preferredColorScheme` (dark for Dusk theme)
- Font size applies globally: chapter list, verse list, chapter detail, reading screen

### 7.4 Font Considerations
- Sanskrit (Devanagari): System font — iOS renders Devanagari natively
- English: System font (San Francisco) — clean and readable
- Hindi: Same system Devanagari rendering
- Book cover header and app name: Serif design font
- Font size adjustable via settings slider (range: 14-28pt)

---

## 8. Project Structure

```
GitaVani/
├── CLAUDE.md                    (Development rules & workflow)
├── README.md                    (Project overview)
├── CURRENT_STATE.md             (What's built & status)
├── NOW.md                       (Current priorities)
├── CHANGELOG.md                 (Version history)
├── docs/
│   └── architecture.md          (This file)
├── scripts/
│   ├── fetch_gita_data.py       (API-based data pipeline)
│   ├── parse_gita_data.py       (Local repo-based pipeline)
│   └── validate_gita_data.py    (Data validation)
├── data/
│   ├── gita_data.json           (Generated — 35.6 MB)
│   └── gita_data_parsed.json    (From local repo — identical)
└── ios/
    └── GitaVani/
        ├── GitaVani.xcodeproj
        └── GitaVani/
            ├── GitaVaniApp.swift
            ├── ContentView.swift
            ├── Models/
            ├── Views/
            │   ├── Onboarding/
            │   ├── Chapters/
            │   ├── Verses/
            │   ├── Settings/
            │   └── Common/
            ├── Services/
            ├── State/
            ├── Theme/
            ├── Resources/
            │   └── gita_data.json
            └── Assets.xcassets/
```

---

## 9. Data Pipeline Scripts

### 9.1 fetch_gita_data.py
Fetches from API with 0.3s delay between calls, normalizes author-keyed format into clean arrays, filters "did not comment" placeholders. Uses `AUTHOR_MAP` with 22 author keys.

### 9.2 parse_gita_data.py
Same normalization logic but reads from local clone of `vedicscriptures.github.io` repo. Accepts optional path argument. Produces byte-identical output to fetch script.

### 9.3 validate_gita_data.py
8-section validation: structure/schema, chapter metadata, verse continuity, content quality, author coverage, commentary quality, Swift compatibility, data size.

### 9.4 Author Key Mapping

| API Key | Author Name | Available Fields |
|---------|-------------|-----------------|
| `tej` | Swami Tejomayananda | ht |
| `siva` | Swami Sivananda | et, ec |
| `purohit` | Shri Purohit Swami | et |
| `chinmay` | Swami Chinmayananda | hc |
| `san` | Dr. S. Sankaranarayan | et |
| `adi` | Swami Adidevananda | et |
| `gambir` | Swami Gambirananda | et |
| `rams` | Swami Ramsukhdas | ht, hc |
| `raman` | Sri Ramanuja | sc, et |
| `abhinav` | Sri Abhinav Gupta | sc, et |
| `sankar` | Sri Shankaracharya | ht, sc, et |
| `prabhu` | A.C. Bhaktivedanta Swami Prabhupada | et, ec |
| `vallabh` | Sri Vallabhacharya | sc |
| `ms` | Sri Madhusudan Saraswati | sc |
| `srid` | Sri Sridhara Swami | sc |
| `dhan` | Sri Dhanpati | sc |
| `venkat` | Vedantadeshikacharya Venkatanatha | sc |
| `puru` | Sri Purushottamji | sc |
| `neel` | Sri Neelkanth | sc |
| `madhav` | Sri Madhavacharya | sc |
| `anand` | Sri Anandgiri | sc |
| `jaya` | Sri Jayatritha | sc |

---

## 10. Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Data storage | Bundled local JSON (35.6 MB) | Offline-first, instant load, static content |
| Navigation | NavigationStack + NavigationPath | Standard iOS drill-down with programmatic navigation |
| Verse navigation | DragGesture + prev/next buttons | Covers both gesture and tap users, works across chapters |
| State persistence | @Observable + UserDefaults | Simple, reactive, no database needed |
| Theming | Observable ThemeManager + UIKit appearance | Instant app-wide switching including nav bars |
| Minimum iOS | iOS 17+ | @Observable, modern SwiftUI features |
| Architecture | MVVM-lite | Views + Services + Observable state — no heavy frameworks |
| Dependencies | Zero external Swift packages | SwiftUI + Foundation + UIKit only |
| JSON decoding | .convertFromSnakeCase | Automatic snake_case → camelCase mapping |
| App icon | AI-generated via Recraft | Lotus + open book, saffron gradient |

---

*This document serves as the single source of truth for GitaVani's architecture. Update as decisions evolve.*
