# GitaVani — Architecture Document

> **Voice of the Gita** — A personal Bhagavad Gita reader app for iOS
> Created: February 2026
> Author: Nikhil
> Status: Pre-development

---

## 1. Project Overview

### 1.1 Purpose
GitaVani is a clean, ad-free iOS app for reading and studying the Bhagavad Gita. Built as a personal gift, it prioritizes readability, simplicity, and a beautiful reading experience over feature bloat.

### 1.2 Target Users
- Primary: Nikhil's wife — English-first reader learning Hindi, part of a Gita study group
- Secondary: Study group members (multi-language potential in future)

### 1.3 Platforms
- iPhone (primary)
- iPad (universal app — SwiftUI handles layout adaptation)

### 1.4 Phased Roadmap

| Phase | Features | Priority |
|-------|----------|----------|
| **V1** | Chapter list, verse reader, Sanskrit/Hindi/English translations, 4 themes, bookmarking, transliteration toggle | Current |
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
- 700 verses × ~5KB each ≈ 3-5MB total — perfectly manageable
- Offline-first is essential for a reader app (commute, travel, no-wifi scenarios)
- Instant loading with zero network dependency
- No API downtime risk

### 2.4 Data Pipeline

A one-time Python script will:
1. Fetch all 18 chapters from `/chapters`
2. Fetch all 700 verses from `/slok/:ch/:sl`
3. Normalize the messy author-keyed format into clean structured JSON
4. Output a single `gita_data.json` file bundled with the app

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
        { "author": "Swami Sivananda", "language": "english", "text": "..." },
        { "author": "Swami Ramsukhdas", "language": "hindi", "text": "..." },
        { "author": "A.C. Bhaktivedanta Swami Prabhupada", "language": "english", "text": "..." }
      ],
      "commentaries": [
        { "author": "Swami Sivananda", "language": "english", "text": "..." },
        { "author": "Swami Chinmayananda", "language": "hindi", "text": "..." },
        { "author": "Sri Shankaracharya", "language": "sanskrit", "text": "..." }
      ]
    }
  ]
}
```

### 2.7 Available Translators

**Hindi Translations (~6 authors):**
- Swami Tejomayananda
- Swami Ramsukhdas
- Swami Chinmayananda (commentary)
- Sri Shankaracharya
- And others

**English Translations (~8 authors):**
- Swami Sivananda
- Shri Purohit Swami
- Swami Adidevananda
- Swami Gambirananda
- Dr. S. Sankaranarayan
- A.C. Bhaktivedanta Swami Prabhupada
- And others

**V1 Recommendation:** Include all translators. Let user pick their preferred default, but allow switching per-verse. More choice = more value vs. competing apps.

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
    let verseId: String           // "BG1.1"
    let chapter: Int
    let verse: Int
    let slok: String              // Sanskrit in Devanagari
    let transliteration: String   // Romanized Sanskrit
    let translations: [Translation]
    let commentaries: [Commentary]

    var id: String { verseId }
}
```

### 3.3 Translation & Commentary

```swift
struct Translation: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language)" }
}

struct Commentary: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language)-commentary" }
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

---

## 4. Screen Flow

```
┌──────────────────────────────────────────────────┐
│                                                  │
│   Home (Chapter List)                            │
│   ┌──────────────────────────────────────────┐   │
│   │ Ch 1 · अर्जुनविषादयोग                     │   │
│   │        Arjuna's Dilemma · 47 verses      │   │
│   ├──────────────────────────────────────────┤   │
│   │ Ch 2 · सांख्ययोग                           │   │
│   │        Transcendental Knowledge · 72 v.  │   │
│   ├──────────────────────────────────────────┤   │
│   │ ...                                      │   │
│   └──────────────────────────────────────────┘   │
│                                    [⚙ Settings]  │
│                                                  │
│   ↓ Tap chapter                                  │
│                                                  │
│   Chapter Detail (Verse List)                    │
│   ┌──────────────────────────────────────────┐   │
│   │ Chapter Summary (collapsible)            │   │
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
│   │ Chapter 1 · Verse 1                      │   │
│   │                                          │   │
│   │ धृतराष्ट्र उवाच |                          │   │
│   │ धर्मक्षेत्रे कुरुक्षेत्रे समवेता युयुत्सवः |    │   │
│   │ मामकाः पाण्डवाश्चैव किमकुर्वत सञ्जय ||    │   │
│   │                                          │   │
│   │ [Transliteration toggle]                 │   │
│   │ dhṛtarāṣṭra uvāca ...                   │   │
│   │                                          │   │
│   │ ─── Translation ──────────────────────── │   │
│   │ [Hindi] [English]     Author: [Picker]   │   │
│   │                                          │   │
│   │ "Dhritarashtra said: O Sanjaya, what     │   │
│   │  did my sons and the sons of Pandu do    │   │
│   │  when they assembled..."                 │   │
│   │                                          │   │
│   │          [← Prev]  [Next →]              │   │
│   └──────────────────────────────────────────┘   │
│   ← Swipe left/right also navigates verses →     │
│                                                  │
│   Settings Screen                                │
│   ┌──────────────────────────────────────────┐   │
│   │ Theme: [Sattva] [Parchment] [Dusk] [Lo] │   │
│   │ Default Language: [Hindi / English]      │   │
│   │ Font Size: [─────●────]                  │   │
│   │ About GitaVani                           │   │
│   └──────────────────────────────────────────┘   │
│                                                  │
└──────────────────────────────────────────────────┘
```

### 4.1 Navigation Pattern
- **NavigationStack** for drill-down: Home → Chapter → Verse
- **Swipe gesture + buttons** for verse-to-verse navigation
- **Settings** accessible via gear icon from Home screen
- **Resume reading** banner on Home screen (shows last read verse)

---

## 5. SwiftUI View Hierarchy

```
GitaVaniApp.swift                    (App entry point)
│
├── ContentView.swift                (Root view with NavigationStack)
│
├── Views/
│   ├── Chapters/
│   │   ├── ChapterListView.swift    (Home screen — list of 18 chapters)
│   │   ├── ChapterRowView.swift     (Single chapter row component)
│   │   └── ChapterDetailView.swift  (Verse list for a chapter + summary)
│   │
│   ├── Verses/
│   │   ├── VerseListRowView.swift   (Single verse row in chapter detail)
│   │   ├── VerseDetailView.swift    (Main reading screen — swipeable)
│   │   ├── ShlokView.swift          (Sanskrit text + transliteration toggle)
│   │   ├── TranslationView.swift    (Language toggle + author picker + text)
│   │   └── VerseNavigationView.swift (Prev/Next buttons)
│   │
│   ├── Settings/
│   │   ├── SettingsView.swift       (Main settings screen)
│   │   ├── ThemePickerView.swift    (Visual theme selection)
│   │   └── FontSizeView.swift       (Font size slider)
│   │
│   └── Common/
│       ├── ResumeReadingBanner.swift (Shows on Home — tap to resume)
│       └── LoadingView.swift         (Initial data load spinner)
│
├── Models/
│   ├── Chapter.swift
│   ├── Verse.swift
│   ├── Translation.swift
│   └── Commentary.swift
│
├── Services/
│   └── GitaDataService.swift        (Loads & parses bundled JSON)
│
├── State/
│   ├── AppSettings.swift            (Theme, font size, default language, transliteration)
│   └── ReadingProgress.swift        (Last read chapter/verse, bookmarks)
│
├── Theme/
│   ├── AppTheme.swift               (Theme definition — colors, fonts)
│   └── ThemeManager.swift           (Theme switching logic)
│
├── Resources/
│   └── gita_data.json               (Bundled dataset — all chapters + verses)
│
└── Extensions/
    └── Color+Theme.swift            (Color extensions for themes)
```

---

## 6. State Management

### 6.1 Persisted State (survives app restart)

Using `@AppStorage` (UserDefaults wrapper):

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `selectedTheme` | String | "sattva" | Current visual theme |
| `fontSize` | Double | 18.0 | Base font size |
| `defaultLanguage` | String | "english" | Default translation language |
| `showTransliteration` | Bool | false | Transliteration toggle |
| `lastReadChapter` | Int | 0 | Bookmark — last chapter |
| `lastReadVerse` | Int | 0 | Bookmark — last verse |
| `preferredHindiAuthor` | String | "" | Preferred Hindi translator |
| `preferredEnglishAuthor` | String | "" | Preferred English translator |

### 6.2 Transient State (in-memory only)

Using `@State` / `@Observable`:
- Currently selected chapter
- Currently selected verse
- Currently selected language tab (Hindi/English)
- Currently selected author (per session override)
- Search text (V2)

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
    let backgroundColor: Color
    let primaryTextColor: Color
    let secondaryTextColor: Color
    let accentColor: Color
    let cardBackgroundColor: Color
    let shlokFontName: String       // Sanskrit-appropriate font
    let bodyFontName: String        // Body text font
}
```

### 7.3 Font Considerations

- Sanskrit (Devanagari): System Devanagari font or "Kohinoor Devanagari" (bundled with iOS)
- English: System font (San Francisco) — clean and readable
- Hindi: Same Devanagari font as Sanskrit
- Font size adjustable via settings slider (range: 14-28pt)

---

## 8. Project Structure (Xcode)

```
GitaVani/
├── GitaVani.xcodeproj
├── GitaVani/
│   ├── GitaVaniApp.swift
│   ├── ContentView.swift
│   ├── Models/
│   ├── Views/
│   │   ├── Chapters/
│   │   ├── Verses/
│   │   ├── Settings/
│   │   └── Common/
│   ├── Services/
│   ├── State/
│   ├── Theme/
│   ├── Extensions/
│   ├── Resources/
│   │   └── gita_data.json
│   └── Assets.xcassets/
│       ├── AppIcon.appiconset/
│       └── Colors/
├── GitaVaniTests/
└── README.md
```

---

## 9. Data Pipeline Script

### 9.1 Purpose
One-time Python script to fetch all data from the API and produce the bundled JSON.

### 9.2 Script: `scripts/fetch_gita_data.py`

Steps:
1. Fetch `GET /chapters` → parse all 18 chapters
2. For each chapter, loop through verses: `GET /slok/:ch/:verse`
3. Normalize each verse:
   - Extract translations (filter for `ht` and `et` fields)
   - Extract commentaries (filter for `hc`, `ec`, `sc` fields)
   - Map author keys to full names
4. Combine into single JSON structure
5. Write to `gita_data.json`

### 9.3 Author Key Mapping

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
| Data storage | Bundled local JSON | Offline-first, instant load, static content |
| Navigation | NavigationStack | Standard iOS drill-down pattern |
| Verse navigation | Swipe (TabView) + buttons | Covers both gesture and tap users |
| State persistence | @AppStorage (UserDefaults) | Simple key-value, perfect for settings + bookmark |
| Theming | Observable ThemeManager | Instant app-wide theme switching |
| Minimum iOS | iOS 17+ | Access to latest SwiftUI features (@Observable, etc.) |
| Architecture | MVVM-lite | Views + Services + Observable state — no heavy framework needed |

---

## 11. CLAUDE.md (for Claude Code sessions)

When working on this project in Claude Code, the following context should be maintained:

```
# GitaVani — Bhagavad Gita Reader for iOS

## Project
- SwiftUI iOS app (iPhone + iPad universal)
- Minimum iOS 17
- No external dependencies for V1
- Bundled local data (no network calls)

## Architecture
- MVVM-lite: Views → Services → Models
- @AppStorage for persistent settings
- @Observable for data service and theme manager
- NavigationStack for chapter → verse drill-down
- TabView with page style for verse swiping

## Data
- Source: vedicscriptures.github.io API (GPL-3.0)
- Bundled as gita_data.json (all 700 verses + 18 chapters)
- Normalized from API format into clean Translation/Commentary arrays

## Key Files
- GitaVaniApp.swift — entry point
- GitaDataService.swift — loads and parses gita_data.json
- ThemeManager.swift — manages 4 themes (Sattva, Parchment, Dusk, Lotus)
- AppSettings.swift — persisted user preferences
- ReadingProgress.swift — bookmark state

## Current Phase: V1
- Chapter list, verse reader
- Sanskrit shloka with transliteration toggle
- Hindi/English translations with author picker
- 4 themes, font size control
- Auto-bookmark last read verse
- Swipe + button verse navigation

## Conventions
- Use SwiftUI previews for all views
- Group files by feature (Chapters/, Verses/, Settings/)
- Keep views small — extract reusable components
```

---

## 12. Build Order (Implementation Plan)

Recommended sequence for building V1:

1. **Data pipeline** — Python script to fetch and normalize all Gita data
2. **Xcode project setup** — Create project, folder structure, add gita_data.json
3. **Models** — Chapter, Verse, Translation, Commentary Swift structs
4. **GitaDataService** — Load and parse JSON, expose chapters and verses
5. **ThemeManager + AppSettings** — Theme definitions and persistent settings
6. **ChapterListView** — Home screen with list of 18 chapters
7. **ChapterDetailView** — Verse list for a chapter
8. **VerseDetailView** — Main reading screen (Sanskrit + translation toggle)
9. **Verse navigation** — Swipe + prev/next buttons
10. **Settings screen** — Theme picker, font size, language preference
11. **Resume reading** — Bookmark persistence + banner on home screen
12. **Polish** — iPad layout, animations, app icon

---

*This document serves as the single source of truth for GitaVani's architecture. Update as decisions evolve.*
