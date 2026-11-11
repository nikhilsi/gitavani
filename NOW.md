# Now: Current Priorities

**Last Updated**: November 11, 2026

## Phase: 1.1 (Word by word and Learn) is live on iOS. Android is next

### Decided

- Nikhil is not testing Android himself (Nov 10). Android stands on the session's Pixel 8a checks.
- Family Testers in both apps keep two old, never-accepted invites (Nikhil's kids). Leave them.
- Android 1.1 (the v1.1.0 tag, GitHub Releases, F-Droid, Play) waits until Nikhil finishes exploring Android with the ClearNews session (Nov 10).
- F-Droid: no anti-feature note for Learn (Nov 10).

### In Flight

- Nothing. iOS 1.1.0 went live on the App Store on Nov 11 (see CHANGELOG).

### Next

1. Labelled take set (spec section 11): recordings from Nikhil (and Urmila if willing) to check the scoring thresholds.
2. Android 1.1 release when Nikhil is ready (see Decided). Read the Android store text in `playstore/metadata` and `fastlane/metadata` first.
3. gitavani.app loads Google Analytics. Check the privacy page's wording against it.
4. Optional: hand-fix timings for the 13 verses without Learn (1.20, 1.21, 1.47, 2.1, 2.63, 2.72, 8.16, 10.42, 11.1, 11.55, 13.1, 16.1, 18.78).

### Where It Stands

| Channel | Status | Notes |
|---------|--------|-------|
| iOS App Store | Live | v1.1.0, released Nov 11, 2026. The first release, v1.0.0, went live Feb 18, 2026 |
| F-Droid | Live | v1.0.0. [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. Picks up new `v*` tags on its own |
| GitHub Releases | Live | v1.0.0 APK + AAB, built by GitHub Actions on tag push |
| Google Play | Not public | Internal testing since Feb 19, 2026. Production needs identity verification and 12 closed-test testers. Play Console not rechecked since March |

### Active

- **Version 1.1 is Word by word plus Learn, released together** (Nikhil, Nov 9).
  - iOS: live on the App Store since Nov 11, 2026.
  - Android: built and checked on the Pixel 8a. Play and F-Droid wait for Nikhil's go.
  - Learn (Abhyāsa) is Phase 2 of [docs/chant-coach-spec.md](docs/chant-coach-spec.md). CenterEye tasks #494 to #500. What's left is under Next.

### Before the Next Release

- **iOS:** nothing blocking. EU trader status was already set: non-trader, Digital Services Act "Active" since Nov 6, 2026 (checked in App Store Connect by the ClearNews session on Nov 9). An earlier note here said it was unset; that was wrong.
- **iOS submission checklist** (from ClearNews's 2.1 submission, Nov 9):
  - Apple's new age-rating question about social media must be answered first: App Information > Age Ratings > Edit. GitaVani's answer is No (sharing only opens the system share sheet). Left blank, adding the version to a submission fails with a 409.
  - Any new screenshots must finish processing before submitting. The web page can keep showing them as processing after Apple has finished (Nov 10: six looked stuck while Apple's records said done). Try "Add for Review" again before uploading anything twice.
  - Store text to paste is in `appstore/metadata/`.
- **F-Droid:** Learn uses Android's on-device speech recognizer. On most phones that's Google's Android System Intelligence. It isn't free software. Decided Nov 10: no anti-feature note. On phones without an on-device recognizer, the Learn button doesn't show.
- **Google Play:** check identity verification in Play Console. Then recruit 12 testers for the closed test (the README has the callout).
