# Current State

**Last Updated**: November 9, 2026

## Status: iOS Live on the App Store / Android Live on F-Droid and GitHub (Google Play not public yet)

All V1 + V2 + V3 (audio) features built, tested, and shipped. iOS v1.0.0 has been live on the App Store since Feb 18, 2026.

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

**Word by Word (1.1, in testing):**
- [x] chant_data.json built from Swami Sivananda's word lists: 693 of 701 verses, 8,840 word parts
- [x] Word by word card under the action bar, closed by default, state remembered
- [x] Help entry
- [x] TestFlight build 1.1 (5)
- [ ] Nikhil's check of 10 verses
- [ ] App Store release (needs Nikhil's go)

**Learn (1.1, in device testing):**
- [x] Line timings for every verse, checked line by line: 1,549 of 1,572 lines pass; Learn on 680 verses
- [x] Scorer in Python, Swift and Kotlin, with shared test cases
- [x] iOS listener (SpeechAnalyzer, iOS 27+): streaming path matches the file trial on all 14 recordings
- [x] Android listener (on-device recognizer, Android 13+): pipe path checked on the Pixel 8a, with a watchdog for a recognizer that stops answering
- [x] Lesson screens on both platforms: Listen, each line, all lines, from memory; progress on the Learn button
- [x] Help entry
- [x] Lesson checked on the iPhone: opens and calibrates (my check), Nikhil tried 1.1 (build 6)
- [x] Independent code review: 9 bugs found and fixed (build 7)
- [x] TestFlight 1.1 (7): internal group; waiting for beta review for Family Testers
- [ ] Labelled take set and threshold check (spec section 11)
- [ ] Nikhil chants 4.7 from memory on each phone (spec step 11)
- [ ] Privacy page paragraph, store text, App Review note

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
- [x] App Store release: v1.0.0 live Feb 18, 2026

---

## Android App

**Status**: Live on GitHub Releases and F-Droid (since Apr 27, 2026). Google Play not public (closed testing not started as of March 2026)

### What's Done (Android)
- [x] Project setup — Kotlin + Jetpack Compose, min API 26, Gradle 8.11.1
- [x] Data loading — 35.6 MB JSON parsed on Dispatchers.IO with loading spinner
- [x] Models — all 6 model classes ported with kotlinx.serialization
- [x] Theme system — 4 themes with exact iOS color values, MaterialTheme wrapper
- [x] Navigation — Navigation Compose with 7 routes
- [x] Chapter list (Home) — book cover header, search icon, resume reading banner
- [x] Chapter detail — summary card, search, verse list
- [x] Verse detail — HorizontalPager swipe, Sanskrit, transliteration, translations, commentaries
- [x] Audio playback — MediaPlayer with lifecycle awareness, 701 MP3s from assets, play/pause/stop
- [x] Search — global (home) and chapter-scoped with 300ms debounce
- [x] Favorites — heart toggle, favorites screen with Recent/Chapter sort
- [x] Share — themed image card (matching iOS) with text fallback via FileProvider
- [x] Settings — theme picker (2x2 grid), font size slider, language toggle, transliteration switch
- [x] About screen — dynamic version via BuildConfig, credits, license, privacy, external links
- [x] Help screen — feature guide with Material icons
- [x] Onboarding — 4-page HorizontalPager with Skip/Get Started
- [x] Edge-to-edge display with themed status bar
- [x] App icon — iOS lotus + open book design at all mipmap densities
- [x] Code review — MediaPlayer leak fix, lifecycle awareness, try/catch safety, release minification

### Distribution Setup (Android)
- [x] Release signing — keystore + keystore.properties (gitignored), conditional config for F-Droid
- [x] GitHub Actions — automated APK + AAB builds on tag push (`.github/workflows/release.yml`)
- [x] GitHub Releases — v1.0.0 APK available for direct download
- [x] F-Droid: [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. v1.0.0 is live on F-Droid. Fastlane metadata in repo (`fastlane/metadata/android/`). Summary/description pulled from fastlane (not YAML).
- [x] Play Store developer account ($25) — created
- [x] Signed release build (AAB) — 134 MB, under 150 MB limit
- [x] Play Console device + phone verification — done
- [x] Play Store listing (description, screenshots, graphics) — done
- [x] Internal testing release — v1.0.0 published Feb 19
- [ ] Identity verification — pending
- [ ] Closed testing (12 opted-in testers required for production)
- [ ] Production release

### Android Feature Status

| Feature | Status |
|---------|--------|
| Data loading | Done |
| Chapter list (Home) | Done |
| Chapter detail | Done |
| Verse detail (Reading) | Done |
| Sanskrit + transliteration | Done |
| Hindi/English translations | Done |
| Author picker | Done |
| Commentaries (17 scholars) | Done |
| 4 Themes | Done |
| Font size control | Done |
| Bookmark/resume | Done |
| Swipe navigation | Done |
| Prev/Next buttons | Done |
| Settings screen | Done |
| Audio playback | Done |
| Search | Done |
| Favorites | Done |
| Share (image card) | Done |
| Word by word | Done (1.1.0, in testing) |
| Learn | Built (1.1.0, in device testing) |
| Onboarding | Done |
| Help screen | Done |
| About screen | Done |
| App icon | Done |
| Edge-to-edge | Done |
| Release signing | Done |
| Release AAB (134 MB) | Done |
| Play Store listing | Done |
| Internal testing release | Done |
| GitHub Actions CI/CD | Done |
| GitHub Releases (APK) | Done |
| F-Droid | Live (v1.0.0, Apr 27, 2026) |
| Closed testing (12 testers) | Not started |
| Production release | Blocked |

---

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
| Word by word | Done (1.1, in testing) |
| Learn | Built (1.1, in device testing) |
| App Store release | Live (v1.0.0, Feb 18, 2026) |
| iPad layout | Done |
