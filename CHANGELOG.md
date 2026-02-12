# Changelog

## [1.2.1] - 2026-02-11

App Store submission and TestFlight distribution.

### Added
- App Store Connect listing — metadata, screenshots, categories, age rating, review notes pushed via API
- TestFlight beta distribution — "Family Testers" group with 3 testers
- iPad default font size increased to 22pt (vs 18pt on iPhone)
- Help screen updated with V2 features (commentaries, search, favorites, share)

### Changed
- Screenshots updated to 8 per device (iPhone 6.9", iPad 13") covering all V2 features and 4 themes

## [1.2.0] - 2026-02-11

V2: Scholar commentaries, favorites, and persistence improvements.

### Added
- Commentaries UI on verse reading screen — 11,189 commentaries from 17 scholars
- Commentary language toggle (English, Hindi, Sanskrit) with author picker
- Text truncation at 1,500 characters with Read more/Show less for long commentaries
- Persistent language and author selections for both translations and commentaries
- Favorites: heart icon on verse detail to save/unsave verses
- Favorites list accessible from home screen and chapter detail toolbar
- Favorites sort toggle (Recent vs Chapter Order)
- Empty state with instructions when no favorites saved
- Search: pull-down search bar on home screen (all verses) and chapter detail (chapter-scoped)
- Search across Sanskrit, transliteration, and all translations with 300ms debounce
- Smart match snippets showing context around the matched text
- Dynamic Type support — respects system text size setting alongside custom font slider
- Share verse — themed image card via iOS share sheet (Sanskrit, translation, branding)
- Share card renders at 3x resolution with current theme colors

### Fixed
- Commentary encoding issues in BG8.1 and BG12.18 (U+FFFD characters removed)
- VerseDetailView toolbar now honors theme background (was showing white border on Dusk)
- Translation author selection now persists across verse navigation

## [1.1.0] - 2026-02-11

App Store preparation: code review, hardening, licensing, and accessibility.

### Added
- About/Credits screen with app version, data attribution, license info, privacy statement
- PrivacyInfo.xcprivacy privacy manifest (UserDefaults declaration)
- Accessibility labels on all icon-only buttons (transliteration, settings, help, nav)
- Settings gear icon accessible from chapter detail and verse detail screens
- Chapter titles/summaries now honor default language setting (transliteration for English, Sanskrit for Hindi)
- LICENSE file (MIT for app code) and LICENSE-LGPL-3.0 (data attribution)
- App Store submission prep doc (docs/submission_prep.md)

### Fixed
- Data loading moved to background thread (was blocking main thread with 35.6 MB JSON)
- Error state when data fails to load (was stuck on "Loading..." forever)
- TranslationView state now resets properly when swiping between verses
- Lotus theme secondary text and accent color adjusted for WCAG 4.5:1 contrast compliance
- All 4 themes verified for WCAG AA color contrast

### Changed
- Verse/chapter lookups now O(1) via dictionaries (was O(n) linear search)
- Print statements gated behind #if DEBUG
- License changed from GPL-3.0 to MIT (app code) + LGPL-3.0 (data, via Kaggle source)

## [1.0.0] - 2026-02-11

V1 complete. All features built, tested on iPhone 15 Pro, docs updated.

### Added
- Book cover header on home screen (app name in serif, tagline, chapter/verse count)
- Read-more summary snippets on chapter detail (3-line preview with expand/collapse)

### Fixed
- Navigation bars now fully honor active theme (background color, title color, tint)
- Status bar adapts to dark themes via preferredColorScheme
- Transliteration defaults to on for new installs (was off)

### Changed
- Architecture docs rewritten to match V1 as built (data models, view hierarchy, technical decisions)
- All project docs updated for V1 completion

## [0.2.0] - 2026-02-11

### Added
- First-launch onboarding: 4 swipeable pages introducing features (skip/get started)
- Help screen: accessible via ? icon on home screen, explains all app features for older users
- App icon: lotus + open book with saffron gradient (AI-generated via Recraft)

### Fixed
- Settings controls (font size slider, transliteration toggle) now work correctly using @Bindable
- Font size setting now applies globally to chapter list, verse list, and chapter detail (was only on reading screen)

## [0.1.0] - 2026-02-11

### Added
- Complete V1 iOS app with full Bhagavad Gita reader functionality
- Xcode project (iOS 17+, universal iPhone + iPad, SwiftUI)
- Swift models: Chapter, Verse, Translation, Commentary, Language, LocalizedText, GitaData
- GitaDataService: loads and parses bundled 35.6 MB JSON (701 verses)
- Chapter list home screen with resume reading banner
- Chapter detail with collapsible summary and verse list
- Verse detail reading screen with Sanskrit shlok display
- Transliteration toggle for romanized Sanskrit
- Hindi/English language toggle with author picker (12 authors)
- Swipe gesture + prev/next button verse navigation (cross-chapter)
- 4 themes: Sattva (white), Parchment (cream), Dusk (dark), Lotus (saffron)
- Settings screen: theme picker, font size slider (14-28pt), language, transliteration
- Persistent reading progress with resume banner on home screen
- All settings persisted via UserDefaults

### Changed
- Deployment target set to iOS 17.0 (from default 26.2)
- App display name set to "GitaVani"

## [0.0.2] - 2026-02-11

### Added
- Data pipeline: parse_gita_data.py (reads from local repo clone, no API calls)
- Data validation: validate_gita_data.py (8-section comprehensive check)
- Generated gita_data.json — 18 chapters, 701 verses, 8183 translations, 11189 commentaries
- .gitignore

### Changed
- Removed dead code (unreachable return) in fetch_gita_data.py

### Validated
- All data verified: structure, chapter metadata, verse continuity, content quality, author coverage, Swift compatibility
- Local-parsed data matches API-fetched data (byte-identical)
- 2 known upstream encoding issues in commentaries (V2 scope)

## [0.0.1] - 2026-02-10

### Added
- Initial project documentation (architecture.md, CLAUDE.md, README.md)
- Data pipeline script (fetch_gita_data.py)
- Project structure and development workflow
