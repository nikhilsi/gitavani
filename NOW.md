# Now: Current Priorities

**Last Updated**: November 10, 2026

## Phase: Finishing 1.1 (Word by word and Learn)

### Decided

- Learn passed Nikhil's check on iOS (Nov 10, verse 1.1). A clean take from memory marked the verse Learned. A take with a word left out was caught and not marked Learned.
- Nikhil is not testing Android himself (Nov 10). Android stands on the session's Pixel 8a checks.
- Family Testers in both apps keep two old, never-accepted invites (Nikhil's kids). Leave them.
- 1.1 on iOS first (Nov 10): store text, build 8, App Review. Android (the v1.1.0 tag, GitHub Releases, F-Droid, Play) waits until Nikhil finishes exploring Android with the ClearNews session.
- iOS 1.1 releases automatically when Apple approves it (Nikhil, Nov 10). It doesn't wait for Urmila's family test.
- Store text for 1.1: Nikhil approved the warmer What's New and promotional text (Nov 10).
- F-Droid: no anti-feature note for Learn (Nov 10).

### In Flight

- **iOS 1.1.0 is in App Review** (submitted Nov 10, 12:42 PM): build 8, new text, keywords and review notes, ten screenshots per device with Learn and Word by word first. It releases automatically when Apple approves it. Apple says review can take up to 48 hours.
- Age rating: Apple's new social media question answered No (Nov 10). The rating stays 4+.
- TestFlight: Apple approved 1.1 (7) for Family Testers (Nov 10). Build 8, the one in App Review, is in the internal group.
- Urmila's iPhone now runs iOS 27. Learn needs it.
- Build 5 (Word by word only) was taken out of review, not expired. It won't ship.

### Next

1. Labelled take set (spec section 11): recordings from Nikhil (and Urmila if willing) to check the scoring thresholds.
2. Android 1.1 release when Nikhil is ready (see Decided). Read the Android store text in `playstore/metadata` and `fastlane/metadata` first.
3. gitavani.app loads Google Analytics. Check the privacy page's wording against it.
4. Optional: hand-fix timings for the 13 verses without Learn (1.20, 1.21, 1.47, 2.1, 2.63, 2.72, 8.16, 10.42, 11.1, 11.55, 13.1, 16.1, 18.78).

### Where It Stands

| Channel | Status | Notes |
|---------|--------|-------|
| iOS App Store | Live | v1.0.0, released Feb 18, 2026. 1.1.0 in App Review since Nov 10, 2026 |
| F-Droid | Live | v1.0.0. [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. Picks up new `v*` tags on its own |
| GitHub Releases | Live | v1.0.0 APK + AAB, built by GitHub Actions on tag push |
| Google Play | Not public | Internal testing since Feb 19, 2026. Production needs identity verification and 12 closed-test testers. Play Console not rechecked since March |

### Active

- **Version 1.1 is Word by word plus Learn, released together** (Nikhil, Nov 9).
  - Word by word: built on both platforms. Android was checked on the Pixel.
  - Learn (Abhyāsa): built on both platforms, in device testing. Phase 2 of [docs/chant-coach-spec.md](docs/chant-coach-spec.md). CenterEye tasks #494 to #500. What's left is under Next.
  - TestFlight: 1.1 (7) for Family Testers. 1.1 (8) in the internal group.
  - iOS 1.1.0 is in App Review. Play and F-Droid wait for Nikhil's go.

### Before the Next Release

- **iOS:** nothing blocking. EU trader status was already set: non-trader, Digital Services Act "Active" since Nov 6, 2026 (checked in App Store Connect by the ClearNews session on Nov 9). An earlier note here said it was unset; that was wrong.
- **iOS submission checklist** (from ClearNews's 2.1 submission, Nov 9):
  - Apple's new age-rating question about social media must be answered first: App Information > Age Ratings > Edit. GitaVani's answer is No (sharing only opens the system share sheet). Left blank, adding the version to a submission fails with a 409.
  - Any new screenshots must finish processing before submitting. The web page can keep showing them as processing after Apple has finished (Nov 10: six looked stuck while Apple's records said done). Try "Add for Review" again before uploading anything twice.
  - Store text to paste is in `appstore/metadata/`.
- **F-Droid:** Learn uses Android's on-device speech recognizer. On most phones that's Google's Android System Intelligence. It isn't free software. Decided Nov 10: no anti-feature note. On phones without an on-device recognizer, the Learn button doesn't show.
- **Privacy page (gitavani.app/privacy):** done. The Microphone (Learn) paragraph is live (Nov 10).
- **Google Play:** check identity verification in Play Console. Then recruit 12 testers for the closed test (the README has the callout).
