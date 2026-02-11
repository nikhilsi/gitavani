# GitaVani — App Store Submission Prep

> Full code review and App Store readiness analysis
> Created: February 11, 2026

---

## 1. Licensing

### Data Sources & Licenses

| Source | License | What it covers |
|--------|---------|---------------|
| `vedicscriptures/bhagavad-gita` (GitHub data repo) | GPL-3.0 | The compiled dataset |
| `vedicscriptures/bhagavad-gita-api` (GitHub API server) | GPL-3.0 | The Node.js API code |
| Kaggle dataset (same author: Pt. Prashant Tripathi) | **LGPL-3.0** | Same data, published separately |
| Original Sanskrit shlokas | **Public domain** | 2000+ year old scripture |

### Decision: MIT for App Code + LGPL-3.0 Attribution for Data

The same author (Pt. Prashant Tripathi) published the data on Kaggle under LGPL-3.0, which is significantly more permissive than GPL-3.0. LGPL allows using the data in apps without requiring the entire app to be open-sourced.

**App code**: MIT License
**Data (gita_data.json)**: LGPL-3.0 attribution to vedicscriptures project + Kaggle dataset

### Action Items
- [x] Create `LICENSE` file (MIT for app code)
- [x] Create `LICENSE-LGPL-3.0` file for data attribution
- [x] Update `README.md` license section
- [x] Add About/Credits screen in app with attribution

### GPL vs App Store Context

Historically, FSF argued Apple's App Store DRM terms conflict with GPL freedoms. In practice, dozens of GPL apps exist on the App Store (Signal, WordPress, Bitwarden, Telegram, Mastodon). Apple does not reject based on license — risk is only if a copyright holder files a complaint. With LGPL-3.0 (Kaggle source), this concern is largely eliminated.

---

## 2. Code Quality Issues

### 2.1 Data Loading on Main Thread (MUST FIX)
**File**: `GitaDataService.swift:19`
**Issue**: Loads and parses 35.6 MB JSON synchronously in `init()`. Blocks the main thread during launch. On older devices, could cause delay or trigger iOS watchdog kill.
**Fix**: Load data on a background thread using `Task { }`.

### 2.2 Linear Search Performance (NICE TO HAVE)
**File**: `GitaDataService.swift:37-43`
**Issue**: `verse(id:)` uses `verses.first { ... }` — O(n) over 701 verses, called on every swipe. Same for `verses(forChapter:)`.
**Fix**: Build `[String: Verse]` and `[Int: [Verse]]` dictionaries at load time.

### 2.3 Print Statements in Production (MUST FIX)
**File**: `GitaDataService.swift:27,29`
**Issue**: `print()` statements visible in device Console. Not production quality.
**Fix**: Remove or gate behind `#if DEBUG`.

### 2.4 No Error State for Data Load Failure (MUST FIX)
**File**: `ContentView.swift:59-61`
**Issue**: Shows `ProgressView("Loading...")` when `isLoaded` is false. If loading fails, user stuck forever.
**Fix**: Add error state to GitaDataService, show error message in ContentView.

### 2.5 Navigation Route Collision Risk (LOW PRIORITY)
**File**: `ContentView.swift:41-57`
**Issue**: String-based routing with magic strings ("settings", "help"). Anything else = verse ID. Works fine with "BG1.1" format but fragile.
**Assessment**: Low risk, works in practice.

### 2.6 Swipe Gesture Conflicts (SHOULD FIX)
**File**: `VerseDetailView.swift:92-101`
**Issue**: Custom DragGesture could conflict with system back-swipe (left edge). 50pt minimum helps.
**Fix**: Test thoroughly. Consider adding `simultaneousGesture` or limiting gesture to non-edge areas.

### 2.7 TranslationView State Reset (SHOULD FIX)
**File**: `TranslationView.swift:11-12`
**Issue**: `@State` properties for language/author may not reset when SwiftUI reuses the view during verse swipe.
**Fix**: Verify behavior; may need `.id(currentVerseId)` on the view to force recreation.

---

## 3. App Store Requirements — Missing Items

### 3.1 Privacy Policy (REQUIRED)
Apple requires a privacy policy URL for all apps, even zero-data-collection apps.
- Host a simple static page (GitHub Pages works)
- Link from inside the app (Settings or About screen)
- Enter URL in App Store Connect

### 3.2 Privacy Manifest File (REQUIRED since 2024)
App uses `UserDefaults` — Apple requires `PrivacyInfo.xcprivacy` declaring the reason.

### 3.3 Accessibility (SHOULD FIX)
No `.accessibilityLabel()` modifiers in codebase:
- Transliteration toggle (book icon) — no label for VoiceOver
- Theme picker color previews — no labels
- Swipe navigation needs VoiceOver alternatives (prev/next buttons exist, good)
- Sanskrit/Devanagari text — test with VoiceOver

### 3.4 Dynamic Type (NICE TO HAVE)
App has custom font size slider (14-28pt) but does NOT respond to system Dynamic Type setting. Apple reviewers sometimes test with accessibility text sizes. Custom slider is good as additional control, but should also respect system setting.

### 3.5 iPad Layout Polish (SHOULD FIX)
Already noted as "Basic (needs polish)". Apple reviewers test on iPad. Broken layout = rejection risk.

### 3.6 About/Credits Screen (MUST FIX)
No attribution screen exists. Needed for:
- Legal compliance (data source attribution)
- App Store polish
- App version, data source, license info, privacy policy link

### 3.7 Color Contrast Verification (SHOULD FIX)
Need to verify all 4 themes meet WCAG 4.5:1 contrast ratio. Dusk (light on dark) and Parchment (brown tones) most at risk.

---

## 4. App Store Submission Checklist

### Before Submission

| Item | Status |
|------|--------|
| Apple Developer Program ($99/year) | Not started |
| App icon 1024x1024 | Done |
| Privacy policy URL | Done — gitavani.app/privacy |
| PrivacyInfo.xcprivacy | Done |
| Screenshots (iPhone 6.9", 6.5", iPad 13") | Not done |
| App description (4000 chars max) | Not done |
| Keywords (100 chars max) | Not done |
| Support URL | Done — gitavani.app/support |
| Age rating questionnaire | Not done (will be 4+) |
| About/Credits screen | Done |
| Accessibility labels | Done |
| License files (MIT + LGPL attribution) | Done |
| Build with latest Xcode SDK | Need to verify |

### Screenshot Requirements

| Device | Resolution |
|--------|-----------|
| iPhone 6.9" | 1320 x 2868 px (portrait) |
| iPhone 6.5" | 1284 x 2778 px (portrait) |
| iPad 13" | 2064 x 2752 px (portrait) |

5-6 screenshots showing: chapter list, verse reading, theme variants, settings, transliteration.

### App Store Metadata

- **Name**: "GitaVani" or "GitaVani - Bhagavad Gita"
- **Subtitle**: "Voice of the Gita" (max 30 chars)
- **Category**: Books or Reference
- **Age Rating**: 4+ (no objectionable content)
- **Keywords**: bhagavad gita,gita,sanskrit,hindu,scripture,verse,translation,hindi,english,spiritual
- **Notes for Review**: Explain interactive features (swipe nav, themes, language toggle, author picker, transliteration, bookmarking) to address Guideline 4.2 minimum functionality

### Religious Content (Guideline 1.1.5)
- Scripture text presented accurately — fine
- Translations attributed to scholars — fine
- No inflammatory commentary — fine
- Many Bhagavad Gita apps already on App Store — category is accepted
- Consider excluding China mainland (NRAA permit requirement for religious content)

---

## 5. Fix Priority

### Must Fix (Blockers)
1. ~~Background thread data loading~~ — Done
2. ~~Privacy policy~~ — Done (gitavani.app/privacy). Link in app About screen still needed.
3. ~~PrivacyInfo.xcprivacy manifest~~ — Done
4. ~~About/Credits screen with attribution~~ — Done
5. ~~Accessibility labels on key controls~~ — Done
6. ~~Remove print statements~~ — Done
7. ~~Error state for data load failure~~ — Done
8. ~~License files (MIT + LGPL attribution)~~ — Done

### Should Fix (Review Risk)
9. iPad layout polish — **Remaining** (needs device testing)
10. ~~Color contrast verification across themes~~ — Done (Lotus fixed)
11. Test swipe gesture vs. back gesture conflict — **Needs device testing**
12. ~~Verify TranslationView state on verse swipe~~ — Done (.id modifier)

### Nice to Have (Quality)
13. ~~Dictionary-based verse lookup~~ — Done
14. Dynamic Type system setting support — **Remaining** (V2 candidate)
