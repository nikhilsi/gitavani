# GitaVani V1 — iOS App Build Plan

## Context

Xcode project is created and running on the simulator (Hello World). Data pipeline is complete with 701 verified verses. Now we need to build the actual app — from data models through to a polished reading experience.

The user has zero iOS/Swift experience (last touched iOS dev in 2013 with Objective-C). Code will be written entirely from the CLI; Xcode's file-system-sync means new files appear automatically.

## Key Technical Notes

- **Xcode uses `PBXFileSystemSynchronizedRootGroup`** — files added to `ios/GitaVani/GitaVani/` on disk auto-sync to the project. No pbxproj editing needed for new files.
- **Deployment target** is currently iOS 26.2 — must change to iOS 17.0 in pbxproj.
- **JSON keys are snake_case**, Swift models use camelCase — use `keyDecodingStrategy = .convertFromSnakeCase`.
- **JSON `"id"` field** in verses needs a CodingKey since the architecture doc uses `verseId` but simpler to just use `id` as stored property.
- **Full 35.6 MB data** will be bundled (commentaries kept for V2).
- **No external dependencies** — SwiftUI + Foundation only.

## Build Steps

Each step produces a buildable, runnable app. We build and verify at each step before moving on.

### Step 1: Project Config & Data Bundle
**Files to modify:**
- `ios/GitaVani/GitaVani.xcodeproj/project.pbxproj` — change `IPHONEOS_DEPLOYMENT_TARGET` from 26.2 to 17.0

**Files to create:**
- `ios/GitaVani/GitaVani/Resources/gita_data.json` — copy from `data/gita_data.json`

**Verify:** Build succeeds (Cmd+R). JSON is included in app bundle.

### Step 2: Swift Models
**Files to create:**
- `ios/GitaVani/GitaVani/Models/Chapter.swift` — Chapter struct (Codable, Identifiable)
- `ios/GitaVani/GitaVani/Models/Verse.swift` — Verse struct (Codable, Identifiable)
- `ios/GitaVani/GitaVani/Models/Translation.swift` — Translation struct + Commentary struct
- `ios/GitaVani/GitaVani/Models/Language.swift` — Language enum (hindi, english, sanskrit)
- `ios/GitaVani/GitaVani/Models/LocalizedText.swift` — LocalizedText struct (en, hi)
- `ios/GitaVani/GitaVani/Models/GitaData.swift` — Top-level wrapper (metadata, chapters, verses)

**Key detail:** Use `keyDecodingStrategy = .convertFromSnakeCase` so JSON `chapter_number` maps to Swift `chapterNumber` automatically. The verse `id` field maps directly since both JSON and Swift use `id`.

**Verify:** Build succeeds.

### Step 3: GitaDataService
**Files to create:**
- `ios/GitaVani/GitaVani/Services/GitaDataService.swift` — @Observable class that loads gita_data.json from bundle, parses it, exposes `chapters: [Chapter]` and `verses: [Verse]`, plus helpers like `verses(forChapter:)` and `verse(id:)`.

**Files to modify:**
- `ios/GitaVani/GitaVani/ContentView.swift` — Temporarily show chapter count and first verse text to verify data loads.

**Verify:** Build and run. Screen shows "18 chapters, 701 verses" or similar.

### Step 4: Theme System & App Settings
**Files to create:**
- `ios/GitaVani/GitaVani/Theme/AppTheme.swift` — Theme struct (colors, font names)
- `ios/GitaVani/GitaVani/Theme/ThemeManager.swift` — @Observable ThemeManager with 4 themes (Sattva, Parchment, Dusk, Lotus)
- `ios/GitaVani/GitaVani/State/AppSettings.swift` — @AppStorage wrapper for all persisted settings (theme, fontSize, language, transliteration, preferred authors)
- `ios/GitaVani/GitaVani/State/ReadingProgress.swift` — @AppStorage wrapper for last read chapter/verse

**Verify:** Build succeeds. Themes defined, settings persist.

### Step 5: Chapter List (Home Screen)
**Files to create:**
- `ios/GitaVani/GitaVani/Views/Chapters/ChapterListView.swift` — ScrollView/List of 18 chapters
- `ios/GitaVani/GitaVani/Views/Chapters/ChapterRowView.swift` — Single chapter row (number, Sanskrit name, English meaning, verse count)
- `ios/GitaVani/GitaVani/Views/Common/ResumeReadingBanner.swift` — Banner showing last read verse (if any)

**Files to modify:**
- `ios/GitaVani/GitaVani/ContentView.swift` — Replace temp content with NavigationStack + ChapterListView
- `ios/GitaVani/GitaVani/GitaVaniApp.swift` — Inject GitaDataService and ThemeManager into environment

**Verify:** Build and run. See list of 18 chapters. Scrollable. Themed.

### Step 6: Chapter Detail (Verse List)
**Files to create:**
- `ios/GitaVani/GitaVani/Views/Chapters/ChapterDetailView.swift` — Chapter summary (collapsible) + list of verses
- `ios/GitaVani/GitaVani/Views/Verses/VerseListRowView.swift` — Single verse row (verse number, first line of Sanskrit)

**Verify:** Tap a chapter → see list of verses for that chapter. Back button works.

### Step 7: Verse Detail (Main Reading Screen)
**Files to create:**
- `ios/GitaVani/GitaVani/Views/Verses/VerseDetailView.swift` — Main reading view (Sanskrit, transliteration, translation)
- `ios/GitaVani/GitaVani/Views/Verses/ShlokView.swift` — Sanskrit text display + transliteration toggle
- `ios/GitaVani/GitaVani/Views/Verses/TranslationView.swift` — Language toggle (Hindi/English) + author picker + translation text

**Verify:** Tap a verse → see Sanskrit text + translation. Language toggle works. Author picker works.

### Step 8: Verse Navigation
**Files to create:**
- `ios/GitaVani/GitaVani/Views/Verses/VerseNavigationView.swift` — Prev/Next buttons

**Files to modify:**
- `ios/GitaVani/GitaVani/Views/Verses/VerseDetailView.swift` — Add swipe gestures + navigation buttons + auto-save reading progress

**Verify:** Swipe left/right between verses. Prev/Next buttons work. Navigates across chapter boundaries.

### Step 9: Settings Screen
**Files to create:**
- `ios/GitaVani/GitaVani/Views/Settings/SettingsView.swift` — Main settings screen
- `ios/GitaVani/GitaVani/Views/Settings/ThemePickerView.swift` — Visual theme selector (4 themes with preview)
- `ios/GitaVani/GitaVani/Views/Settings/FontSizeView.swift` — Font size slider

**Files to modify:**
- `ios/GitaVani/GitaVani/Views/Chapters/ChapterListView.swift` — Add gear icon to navigate to Settings

**Verify:** Settings accessible from home. Theme changes apply immediately. Font size adjusts. Language preference persists.

### Step 10: Resume Reading & Bookmarks
**Files to modify:**
- `ios/GitaVani/GitaVani/Views/Common/ResumeReadingBanner.swift` — Wire up to reading progress
- `ios/GitaVani/GitaVani/Views/Verses/VerseDetailView.swift` — Auto-save last read verse

**Verify:** Read a verse → close app → reopen → banner shows "Continue: Chapter X, Verse Y". Tap banner → jumps to that verse.

### Step 11: Polish
- iPad layout adjustments (wider reading area)
- Smooth animations for theme switching
- Loading state while JSON parses (should be fast, but just in case)
- App display name in Info.plist

## File Structure (Final)

```
ios/GitaVani/GitaVani/
├── GitaVaniApp.swift
├── ContentView.swift
├── Models/
│   ├── Chapter.swift
│   ├── Verse.swift
│   ├── Translation.swift
│   ├── Language.swift
│   ├── LocalizedText.swift
│   └── GitaData.swift
├── Views/
│   ├── Chapters/
│   │   ├── ChapterListView.swift
│   │   ├── ChapterRowView.swift
│   │   └── ChapterDetailView.swift
│   ├── Verses/
│   │   ├── VerseListRowView.swift
│   │   ├── VerseDetailView.swift
│   │   ├── ShlokView.swift
│   │   ├── TranslationView.swift
│   │   └── VerseNavigationView.swift
│   ├── Settings/
│   │   ├── SettingsView.swift
│   │   ├── ThemePickerView.swift
│   │   └── FontSizeView.swift
│   └── Common/
│       └── ResumeReadingBanner.swift
├── Services/
│   └── GitaDataService.swift
├── State/
│   ├── AppSettings.swift
│   └── ReadingProgress.swift
├── Theme/
│   └── AppTheme.swift
│   └── ThemeManager.swift
├── Resources/
│   └── gita_data.json
└── Assets.xcassets/
```

## Workflow

I'll implement each step, build from the CLI (`xcodebuild`), and verify it compiles before moving to the next. At key milestones (Steps 5, 7, 9), you should Cmd+R in Xcode to see the app on the simulator and give feedback.

## Verification

After all steps:
1. Full navigation: Chapters → Verses → Reading screen
2. All 4 themes work and persist
3. Font size adjustable and persists
4. Hindi/English toggle with author picker
5. Sanskrit text with transliteration toggle
6. Swipe + button verse navigation
7. Resume reading banner on home screen
8. Works on both iPhone and iPad simulators
