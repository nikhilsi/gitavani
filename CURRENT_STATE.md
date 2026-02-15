# Current State

**Last Updated**: February 14, 2026

## Status: V1.0.0 Submitted for App Store Review

All V1 + V2 + V3 (audio) features built, tested, and submitted for Apple review.

### What's Done

**Core App:**
- [x] Architecture design and documentation
- [x] Data pipeline — fetch, parse, and validate scripts
- [x] Generated gita_data.json — 18 chapters, 701 verses, 8183 translations, 11189 commentaries
- [x] Xcode project setup (iOS 17+, universal iPhone + iPad)
- [x] Swift models (Chapter, Verse, Translation, Commentary, Language, LocalizedText, GitaData)
- [x] GitaDataService — async background loading with dictionary lookups
- [x] ThemeManager — 4 WCAG-compliant themes with UINavigationBar appearance
- [x] AppSettings — persistent font size, language, transliteration, preferred authors
- [x] ReadingProgress — persistent last-read chapter/verse
- [x] Chapter list (home screen) with book cover header and resume reading banner
- [x] Chapter detail with read-more summary snippets + verse list
- [x] Verse detail (reading screen) — Sanskrit shlok, transliteration toggle, translation
- [x] Language toggle (Hindi/English) with author picker
- [x] Chapter titles/summaries honor default language setting
- [x] Verse navigation — swipe gestures + prev/next buttons (works across chapters)
- [x] Settings screen — theme picker, font size slider, language, transliteration toggle
- [x] Settings gear icon accessible from all screens
- [x] Resume reading banner — auto-saves progress, tap to jump back
- [x] Themes fully applied — navigation bars, status bar, all views honor active theme
- [x] Font size applies globally (chapter list, verse list, chapter detail, reading screen)
- [x] Transliteration defaults to on for new installs
- [x] App display name set to "GitaVani"
- [x] First-launch onboarding (4 swipeable pages)
- [x] Help screen (accessible from ? icon on home screen)
- [x] App icon (lotus + open book, saffron gradient, AI-generated via Recraft)
- [x] Tested on physical device (iPhone 15 Pro)

**App Store Prep:**
- [x] Async data loading (background thread, no main-thread blocking)
- [x] Error state for data load failure
- [x] Print statements gated behind #if DEBUG
- [x] O(1) dictionary-based verse/chapter lookups
- [x] TranslationView state reset on verse swipe (.id modifier)
- [x] PrivacyInfo.xcprivacy manifest (UserDefaults declaration)
- [x] Accessibility labels on all icon-only buttons
- [x] About/Credits screen (version, attribution, license, privacy)
- [x] WCAG 4.5:1 color contrast verified + Lotus theme fixed
- [x] License files — MIT (app code) + LGPL-3.0 (data attribution)
- [x] App Store submission prep doc (docs/submission_prep.md)

**V2 Features:**
- [x] Commentaries UI — 17 authors, 3 languages (EN/HI/SA), language toggle + author picker
- [x] Commentary text truncation (1500 chars with Read more/Show less)
- [x] Commentary encoding fix (BG8.1, BG12.18 U+FFFD removed)
- [x] Persistent language/author selections for both translations and commentaries
- [x] Favorites — heart icon on verse detail, favorites list with sort toggle (Recent/Chapter Order)
- [x] Favorites accessible from home screen and chapter detail toolbar
- [x] Search — pull-down searchable on home (global) and chapter detail (chapter-scoped)
- [x] Search across Sanskrit, transliteration, and all translations with debounce
- [x] Dynamic Type — system text size scales on top of custom font slider via @ScaledMetric
- [x] Share verse — themed image card via iOS share sheet with text fallback

**V3 Audio:**
- [x] 701 Sanskrit verse audio files bundled (96kbps MP3, 126MB total)
- [x] Audio source: shlokam.org individual verse recordings
- [x] AudioService — AVAudioPlayer with play/pause/stop
- [x] Action bar UI below Sanskrit text — Play, Romanize, Save, Share buttons
- [x] Auto-stop when verse finishes playing
- [x] Audio stops on verse navigation (swipe, prev/next) and when leaving screen

**Website (gitavani.app):**
- [x] Domain purchased (gitavani.app on GoDaddy)
- [x] Static site hosted on GitHub Pages (nikhilsi/gitavani-site)
- [x] Landing page with app description and features
- [x] Privacy policy page (gitavani.app/privacy)
- [x] Support page with FAQ (gitavani.app/support)
- [x] Support email (thegitavani@gmail.com)

### What's Next
- [x] Apple Developer Program signup ($99/year) — enrolled, pending activation
- [x] Add privacy policy link in app About screen — done (gitavani.app/privacy)
- [x] App Store screenshots (iPhone 6.9", iPad 13") — 8 each, all V2 features + 4 themes
- [x] App Store metadata (description, keywords, category) — pushed via API
- [x] Update website landing page for V2 features
- [x] Developer enrollment activation — confirmed
- [x] Create app in App Store Connect — metadata, screenshots, categories, age rating, review notes
- [x] Upload build via Xcode — v1.0.0 (build 1), archived and uploaded via xcodebuild
- [x] TestFlight build — distributed to 3 testers (Family Testers group)
- [x] iPad layout refinements — larger default font size (22pt vs 18pt)
- [x] Update help screen for V2 features
- [x] V3 audio — 701 verse recordings bundled at 96kbps
- [x] App Store submission — v1.0.0 (build 3) submitted for review

### Known Issues
- 2 verses (BG12.3, BG12.18) have only 1 translation each — correct, other scholars did not comment
- 2 commentaries have U+FFFD encoding issues (BG8.1, BG12.18) — upstream data, commentaries are V2

### Data Stats

| Metric | Value |
|--------|-------|
| Chapters | 18 |
| Verses | 701 |
| Hindi translations | 2,050 (3 authors) |
| English translations | 6,133 (9 authors) |
| Commentaries | 11,189 (V2) |
| File size | 35.6 MB |

### Feature Status

| Feature | Status |
|---------|--------|
| Data pipeline | Done |
| Chapter list (Home) | Done |
| Chapter detail (Verse list) | Done |
| Verse detail (Reading screen) | Done |
| Sanskrit + transliteration | Done |
| Hindi/English translations | Done |
| Author picker | Done |
| Language-aware chapter titles | Done |
| Theming (4 themes, WCAG compliant) | Done |
| Theme applied to nav bars | Done |
| Font size control (global) | Done |
| Bookmark/resume | Done |
| Swipe navigation | Done |
| Prev/Next buttons | Done |
| Settings screen | Done |
| Settings gear on all screens | Done |
| Onboarding | Done |
| Help screen | Done |
| About/Credits screen | Done |
| App icon | Done |
| Privacy manifest | Done |
| Accessibility labels | Done |
| Async data loading | Done |
| Favorites | Done |
| Search | Done |
| Dynamic Type | Done |
| Share verse | Done |
| Sanskrit verse audio | Done |
| App Store submission | Waiting for Review |
| iPad layout | Done |
