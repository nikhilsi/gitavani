# Claude Code Development Guide

---
**Last Updated**: February 10, 2026
**Purpose**: Rules and workflow for working with this codebase
---

## 🎯 Starting a New Session

**Read these docs in order:**

1. **CLAUDE.md** (this file) - Rules & workflow
2. **README.md** - Project overview
3. **docs/architecture.md** - Full architecture, data model, screen flow, theming, build order
4. **CURRENT_STATE.md** - What's built & current status
5. **CHANGELOG.md** - Version history & recent changes
6. **NOW.md** - What to work on next
7. **`git log --oneline -10`** - Recent commits

---

## 🚨 Critical Rules

### What Pisses Me Off (AVOID AT ALL COSTS)
1. **Unauthorized commits** - NEVER commit without explicit approval
2. **Over-engineering** - KISS principle always. This is a reader app, not a framework.
3. **Not reading requirements** - Full attention to specs, read architecture.md thoroughly
4. **Being lazy** - Read ALL the docs before starting
5. **Lying or pretending** - Say "I don't know" if unsure
6. **Not thinking critically** - Question things that don't make sense
7. **Adding dependencies without asking** - V1 has zero external Swift dependencies. Keep it that way.

### Golden Rules
1. **Always discuss architecture/flow BEFORE writing code**
2. **Document as you go** - Update CURRENT_STATE.md after completing features
3. **Small, focused changes** - Don't refactor the world in one go
4. **Test on both iPhone and iPad** - This is a universal app
5. **Production Mindset** - Build as if going to the App Store

---

## 🏗️ Architecture Summary

**This is a local-first iOS reader app. No backend. No network calls in V1.**

```
Bundled JSON (gita_data.json)
        │
        ▼
  GitaDataService (loads & parses)
        │
        ▼
  SwiftUI Views (NavigationStack)
        │
        ├── ChapterListView (Home)
        ├── ChapterDetailView (Verse list)
        ├── VerseDetailView (Reading screen)
        └── SettingsView (Themes, font, language)
```

**Key architectural decisions:**
- **Bundled local data** - All 700 verses + 18 chapters in a single JSON file. No API calls at runtime.
- **Offline-first** - Works without internet. Always.
- **MVVM-lite** - Views + Services + Observable state. No heavy frameworks.
- **@AppStorage** - UserDefaults wrapper for persisted settings and bookmarks.
- **NavigationStack** - Standard iOS drill-down: Chapters → Verses → Detail.
- **Swipe + buttons** - Verse navigation supports both gestures and tap.

**For full architecture details, data models, screen flow, and theming, see docs/architecture.md**

---

## 🗄️ Tech Stack

| Layer | Technology |
|-------|-----------|
| App | Swift / SwiftUI |
| Minimum iOS | 17.0 |
| Data | Bundled JSON (no database) |
| State | @AppStorage + @Observable |
| Data Pipeline | Python 3.12+ (one-time script) |
| Target | iPhone + iPad (Universal) |

---

## 🗄️ Data Source

- **API**: Vedic Scriptures Bhagavad Gita API (`https://vedicscriptures.github.io`)
- **License**: GPL-3.0
- **Data pipeline**: `scripts/fetch_gita_data.py` fetches all data and normalizes into `data/gita_data.json`
- **Re-run pipeline** if you need to add/remove translators or the API updates

---

## 📚 Documentation Structure

**Root Level:**
- **CLAUDE.md** - Rules & workflow (this file)
- **README.md** - Project overview
- **CURRENT_STATE.md** - What's built, feature status
- **NOW.md** - Current priorities
- **CHANGELOG.md** - Version history

**Docs:**
- **docs/architecture.md** - Full architecture, data model, screen flow, theming, build order

**Code:**
- **scripts/** - Data pipeline (Python)
- **data/** - Generated JSON data file
- **ios/GitaVani/** - Xcode project and SwiftUI source

---

## 📂 Project Structure

```
GitaVani/
├── CLAUDE.md
├── README.md
├── CURRENT_STATE.md
├── NOW.md
├── CHANGELOG.md
├── docs/
│   └── architecture.md
├── scripts/
│   └── fetch_gita_data.py
├── data/
│   └── gita_data.json
└── ios/
    └── GitaVani/
        ├── GitaVani.xcodeproj
        ├── GitaVaniApp.swift
        ├── ContentView.swift
        ├── Models/
        ├── Views/
        │   ├── Chapters/
        │   ├── Verses/
        │   ├── Settings/
        │   └── Common/
        ├── Services/
        ├── State/
        ├── Theme/
        ├── Extensions/
        ├── Resources/
        │   └── gita_data.json (copied from data/)
        └── Assets.xcassets/
```

## 📂 Build Order

1. ✅ Documentation & architecture
2. **Data pipeline** — Python script to fetch and normalize all Gita data
3. **Xcode project setup** — Create project, folder structure, bundle gita_data.json
4. **Models** — Chapter, Verse, Translation Swift structs
5. **GitaDataService** — Load and parse JSON
6. **ThemeManager + AppSettings** — 4 themes + persistent settings
7. **ChapterListView** — Home screen
8. **ChapterDetailView** — Verse list per chapter
9. **VerseDetailView** — Main reading screen
10. **Verse navigation** — Swipe + prev/next buttons
11. **Settings screen** — Theme, font size, language
12. **Resume reading** — Bookmark + home banner
13. **Polish** — iPad layout, animations, app icon

---

## 🎨 Theming

4 built-in themes:
- **Sattva** — Clean, white, modern
- **Parchment** — Warm cream, classic book feel
- **Dusk** — Dark mode, gold accents
- **Lotus** — Saffron tones, devotional

All themes defined in `ThemeManager.swift`. User selection persisted via `@AppStorage`.

---

## ⚠️ Reminders

- This app is a **gift for my wife**. Quality and polish matter.
- She's using a **crappy app with colored backgrounds, hard-to-read fonts, and ads**. We're building the antidote.
- **I have zero iOS/Swift experience** — explain SwiftUI concepts as we build, don't assume knowledge.
- The data source API is **static and free** — no API keys, no rate limits.
- **No external Swift dependencies** for V1. SwiftUI + Foundation only.
- **GPL-3.0 license** on data source — if publishing to App Store, app code must be open-sourced.
