# Current State

**Last Updated**: February 11, 2026

## Status: V1 Complete — App Store Prep In Progress

All V1 features built, tested on iPhone 15 Pro. Now hardening for App Store submission.

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
- [x] App Store screenshots (iPhone 6.9", 6.5", iPad 13") — 6 each, all 4 themes
- [x] App Store metadata (description, keywords, category) — saved in appstore/metadata/
- [ ] Developer enrollment activation (waiting for Apple email)
- [ ] Create app in App Store Connect
- [ ] Upload build via Xcode
- [ ] TestFlight build for wife's testing
- [ ] iPad layout refinements based on feedback
- [ ] Update App Store content for V2 features (description, screenshots, website, help screen)
- [ ] V2: share verse, widget

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
| iPad layout | Basic (needs polish) |
