# Claude Code Development Guide

---
**Last Updated**: February 11, 2026
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
Bundled JSON (gita_data.json, 35.6 MB)
        │
        ▼
  GitaDataService (loads & parses at launch)
        │
        ▼
  SwiftUI Views (NavigationStack + NavigationPath)
        │
        ├── OnboardingView (first launch only)
        ├── ChapterListView (Home — book cover + chapter list)
        ├── ChapterDetailView (Summary snippet + verse list)
        ├── VerseDetailView (Reading screen — swipe between verses)
        ├── SettingsView (Themes, font, language)
        └── HelpView (Feature guide)
```

**Key architectural decisions:**
- **Bundled local data** - All 701 verses + 18 chapters in a single JSON file. No API calls at runtime.
- **Offline-first** - Works without internet. Always.
- **MVVM-lite** - Views + Services + Observable state. No heavy frameworks.
- **@Observable + UserDefaults** - Observable classes with didSet persistence for settings and bookmarks.
- **NavigationStack** - Standard iOS drill-down: Chapters → Verses → Detail.
- **DragGesture + buttons** - Verse navigation supports both swipe gestures and tap, works across chapter boundaries.
- **UINavigationBarAppearance** - Navigation bars themed via UIKit to match active theme.

**For full architecture details, data models, screen flow, and theming, see docs/architecture.md**

---

## 🗄️ Tech Stack

| Layer | Technology |
|-------|-----------|
| App | Swift / SwiftUI |
| Minimum iOS | 17.0 |
| Data | Bundled JSON (no database) |
| State | @Observable + UserDefaults |
| Data Pipeline | Python 3.12+ (one-time script) |
| Target | iPhone + iPad (Universal) |

---

## 🗄️ Data Source

- **API**: Vedic Scriptures Bhagavad Gita API (`https://vedicscriptures.github.io`)
- **Data License**: LGPL-3.0 (via [Kaggle](https://www.kaggle.com/datasets/ptprashanttripathi/bhagavad-gita-api-database))
- **App License**: MIT
- **Data pipeline**: `scripts/fetch_gita_data.py` (API) and `scripts/parse_gita_data.py` (local repo)
- **Validation**: `scripts/validate_gita_data.py` — 8-section data validation
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
- **docs/architecture.md** - Full iOS architecture, data model, screen flow, theming
- **docs/android-architecture.md** - Android architecture, Kotlin/Compose, build commands
- **docs/submission_prep.md** - App Store submission checklist and code review

**Code:**
- **scripts/** - Data pipeline (Python)
- **data/** - Generated JSON data file
- **ios/GitaVani/** - Xcode project and SwiftUI source
- **android/GitaVani/** - Android Studio project and Kotlin/Compose source

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
│   ├── architecture.md
│   └── submission_prep.md
├── scripts/
│   ├── fetch_gita_data.py
│   ├── parse_gita_data.py
│   └── validate_gita_data.py
├── data/
│   └── gita_data.json
├── ios/
│   └── GitaVani/
│       ├── GitaVani.xcodeproj
│       └── GitaVani/
│           ├── GitaVaniApp.swift
│           ├── ContentView.swift
│           ├── Models/
│           ├── Views/
│           │   ├── Onboarding/
│           │   ├── Chapters/
│           │   ├── Verses/
│           │   ├── Settings/
│           │   └── Common/
│           ├── Services/
│           ├── State/
│           ├── Theme/
│           ├── Resources/
│           │   └── gita_data.json
│           └── Assets.xcassets/
└── android/
    └── GitaVani/
        ├── app/src/main/java/com/nikhilsi/gitavani/
        │   ├── model/          # Data models (kotlinx.serialization)
        │   ├── data/           # GitaDataService
        │   ├── audio/          # AudioService (MediaPlayer)
        │   ├── state/          # AppSettings, ReadingProgress
        │   ├── theme/          # AppTheme, MaterialTheme wrapper
        │   ├── ui/             # Compose screens
        │   └── viewmodel/      # GitaViewModel
        ├── app/src/main/assets/ # gita_data.json + audio/
        └── gradle/             # Build config
```

## 📂 Build Order (V1 Complete)

1. ✅ Documentation & architecture
2. ✅ Data pipeline — fetch, parse, and validate scripts
3. ✅ Xcode project setup — iOS 17+, universal, bundled JSON
4. ✅ Models — Chapter, Verse, Translation, Commentary, Language, LocalizedText, GitaData
5. ✅ GitaDataService — load and parse JSON
6. ✅ ThemeManager + AppSettings — 4 themes + persistent settings
7. ✅ ChapterListView — home screen with book cover header
8. ✅ ChapterDetailView — read-more summary + verse list
9. ✅ VerseDetailView — main reading screen
10. ✅ Verse navigation — DragGesture + prev/next buttons (cross-chapter)
11. ✅ Settings screen — theme, font size, language, transliteration
12. ✅ Resume reading — auto-bookmark + home banner
13. ✅ Polish — onboarding, help screen, app icon, themed nav bars

---

## 🎨 Theming

4 built-in themes:
- **Sattva** — Clean, white, modern
- **Parchment** — Warm cream, classic book feel
- **Dusk** — Dark mode, gold accents
- **Lotus** — Saffron tones, devotional

All themes defined in `AppTheme.swift`, managed by `ThemeManager.swift`. Theme applies to all views including navigation bars (via UINavigationBarAppearance). User selection persisted via UserDefaults.

---

## ⚠️ Reminders

- This app is a **gift for my wife**. Quality and polish matter.
- She's using a **crappy app with colored backgrounds, hard-to-read fonts, and ads**. We're building the antidote.
- The data source API is **static and free** — no API keys, no rate limits.
- **No external Swift dependencies** for V1. SwiftUI + Foundation + UIKit only.
- **MIT license** on app code, **LGPL-3.0** on data source. Source code is public on GitHub.
