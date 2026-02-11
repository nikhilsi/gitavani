# Now — Current Priorities

**Last Updated**: February 11, 2026

## Phase: Xcode Project Setup

### Goal
Create the iOS project, set up the folder structure, bundle the data, and build the foundation layer (models + data service + theming).

### Tasks

1. **Create Xcode project** — iOS App, SwiftUI, iOS 17+, universal (iPhone + iPad)
2. **Set up folder structure** — Models/, Views/, Services/, State/, Theme/, Extensions/, Resources/
3. **Bundle gita_data.json** — Copy into Resources/
4. **Swift models** — Chapter, Verse, Translation, Commentary, Language, LocalizedText
5. **GitaDataService** — Load and parse bundled JSON
6. **ThemeManager + AppSettings** — 4 themes, persistent settings

### Previous Phase: Data Pipeline (Done)
- Data pipeline complete and validated
- 701 verses, 8183 translations, 11189 commentaries
- Two scripts: fetch (API) and parse (local repo) — both produce identical output
