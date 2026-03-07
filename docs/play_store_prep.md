# GitaVani — Play Store Submission Prep

> Android app submission checklist and configuration reference
> Created: February 19, 2026
> Updated: March 7, 2026

---

## 1. Play Store Console Setup

### Developer Account
- Google Play Developer account: $25 one-time fee
- Account ID: 7407386146617056591
- Device verification: Pixel 8a
- Phone verification: Completed
- Identity verification: Pending (1-7 days)

### App Creation
- App name: GitaVani - Bhagavad Gita
- Package: com.nikhilsi.gitavani
- Default language: English (United States)
- Type: App (not Game)
- Pricing: Free
- All declarations accepted (Developer Program Policies, Play App Signing, US export laws)

---

## 2. App Content Declarations

| Declaration | Answer |
|-------------|--------|
| Privacy policy | https://gitavani.app/privacy |
| App access | All functionality available without special access |
| Ads | No ads |
| Content rating | Everyone (IARC — Reference/Educational, no objectionable content) |
| Target audience | 18+ (avoids COPPA/child safety requirements) |
| Data safety | No data collected (fully offline, SharedPreferences only) |
| Government app | No |
| Financial features | No |
| Health app | No |

---

## 3. Store Listing

### Metadata
- **App name**: GitaVani - Bhagavad Gita (24/30 chars)
- **Short description**: Ad-free Bhagavad Gita reader with Sanskrit audio, translations & commentaries (77/80 chars)
- **Full description**: See `playstore/metadata/description.txt`
- **Category**: Books & Reference
- **Contact email**: thegitavani@gmail.com
- **Website**: https://gitavani.app

### Graphics

| Asset | Size | File |
|-------|------|------|
| App icon | 512x512 px | playstore/app-icon-512.png |
| Feature graphic | 1024x500 px | playstore/feature-graphic-1024x500.png |

### Screenshots

| Device | Resolution | Count | Location |
|--------|-----------|-------|----------|
| Phone (Pixel 7) | 1080x2400 | 6 | playstore/screenshots/phone/ |
| 7-inch tablet (Nexus 7) | 1200x1920 | 4 | playstore/screenshots/tablet-7inch/ |
| 10-inch tablet (Pixel Tablet) | 2560x1600 | 4 | playstore/screenshots/tablet-10inch/ |

Phone screenshots:
1. Home screen — Sattva theme
2. Verse reading (English) — Sattva theme
3. Chapter detail — Parchment theme
4. Verse reading (Hindi) — Lotus theme
5. Settings — Dusk theme
6. Favorites — Dusk theme

Tablet screenshots (same sequence, 4 screens):
1. Home screen — Sattva
2. Verse reading — Sattva
3. Chapter detail — Parchment
4. Settings — Dusk

---

## 4. Release Signing

- **Keystore**: `android/GitaVani/keystore/gitavani-release.jks` (gitignored)
- **Credentials**: `android/GitaVani/keystore.properties` (gitignored)
- **Key alias**: gitavani-release
- **Algorithm**: RSA 2048-bit, 10,000-day validity (~2053)
- **DN**: CN=Nikhil Singhal, O=GitaVani, C=US
- **Play App Signing**: Enabled — Google manages the app signing key; our keystore is the upload key
- **Password**: Stored in keystore.properties. Back up in password manager.

---

## 5. Build Configuration

- **AAB size**: 134 MB (under 150 MB limit — Play Asset Delivery not needed)
- **R8 minification**: Enabled (isMinifyEnabled + isShrinkResources)
- **ProGuard rules**: kotlinx.serialization model classes preserved
- **Build command**: `./gradlew bundleRelease`
- **AAB output**: `app/build/outputs/bundle/release/app-release.aab`
- **Version**: 1.0.0 (versionCode 1)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

---

## 6. Testing Tracks

### Internal Testing
- Released: Feb 19, 2026 — version 1 (1.0.0)
- Status: Available to internal testers
- Testers: TBD (add via Testers tab)

### Closed Testing (Required for Production)
- Must complete before publishing to production
- Requires 12 opted-in testers
- Countries/regions: TBD

### Production
- Requires completed closed test with 12+ testers
- Identity verification must be approved

---

## 7. Submission Checklist

### Before Submission
| Item | Status |
|------|--------|
| Google Play Developer account ($25) | Done |
| Device verification (Pixel 8a) | Done |
| Phone verification | Done |
| Identity verification | Pending |
| App icon 512x512 | Done |
| Feature graphic 1024x500 | Done |
| Phone screenshots (6) | Done |
| 7-inch tablet screenshots (4) | Done |
| 10-inch tablet screenshots (4) | Done |
| App description + short description | Done |
| Privacy policy URL | Done |
| Content rating questionnaire | Done |
| Data safety declaration | Done |
| All content declarations | Done |
| Store settings (category, contact) | Done |
| Release signing (keystore) | Done |
| Signed AAB uploaded | Done |
| Internal testing release | Done |
| GitHub Actions CI/CD | Done |
| GitHub Releases (APK download) | Done |
| F-Droid submission (MR #34390) | Pending review |
| Closed testing (12 testers) | Not started |
| Production release | Blocked on identity verification + closed test |

---

## 8. iOS vs Android Store Comparison

| Aspect | App Store (iOS) | Play Store (Android) |
|--------|----------------|---------------------|
| Developer fee | $99/year | $25 one-time |
| App name limit | 30 chars | 30 chars |
| Subtitle/Short desc | 30 chars (subtitle) | 80 chars (short description) |
| Description limit | 4,000 chars | 4,000 chars |
| Keywords | 100 chars (separate field) | N/A (extracted from description) |
| Screenshots | iPhone + iPad sizes | Phone + 7" tablet + 10" tablet |
| Icon size | 1024x1024 | 512x512 |
| Feature graphic | N/A | 1024x500 (required) |
| Review process | Manual review (1-3 days) | Automated + manual (hours to days) |
| Testing | TestFlight | Internal/Closed/Open testing tracks |
| Production requirement | None (submit directly) | Closed test with 12 testers |
| Bundle format | .ipa (via Xcode) | .aab (Android App Bundle) |
| Size limit | 4 GB | 150 MB base AAB |
| Signing | Apple-managed | Play App Signing (upload key + signing key) |
