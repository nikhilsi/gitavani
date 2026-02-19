# Now — Current Priorities

**Last Updated**: February 19, 2026

## Phase: Android Feature Complete — Ready for Play Store Prep

### iOS Status
V1.0.0 (build 3) submitted for App Store review. Waiting.

### Android Status
Feature complete. Tested on emulator (Pixel 7, API 35). Full feature parity with iOS including themed image share card.

### Android — What's Built
- Native Kotlin + Jetpack Compose app
- All 701 verses, 18 chapters, translations, commentaries
- 701 audio files bundled (126 MB) with lifecycle-aware playback
- 4 themes, settings, search, favorites, onboarding, help, about
- Themed image share card (matching iOS design)
- App icon matching iOS (lotus + open book)
- Code reviewed and hardened (MediaPlayer safety, try/catch, release minification)

### Next Steps
1. ~~Create Google Play Developer account ($25 one-time)~~ — Done
2. ~~Set up signing keys for release build~~ — Done (keystore generated, build.gradle.kts configured)
3. ~~Play Asset Delivery~~ — Not needed! Release AAB is 134 MB (under 150 MB limit) thanks to R8 minification
4. Play Console device verification (using Pixel 8a)
5. Create app listing on Play Console
6. Internal testing track — upload signed AAB
7. Play Store listing (screenshots, description, categories)

### Completed
- ~~Phase 1: Project setup & data loading~~
- ~~Phase 2: Theme system~~
- ~~Phase 3: Navigation & chapter list~~
- ~~Phase 4: Chapter detail & verse list~~
- ~~Phase 5: Verse detail (reading screen)~~
- ~~Phase 6: Audio playback~~
- ~~Phase 7: Search, favorites, share~~
- ~~Phase 8: Onboarding, help, about, settings~~
- ~~Phase 9: Polish & platform conventions~~
- ~~Phase 10: Code review & bug fixes~~
- ~~Phase 11: Documentation~~
