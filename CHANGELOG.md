# Changelog

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
