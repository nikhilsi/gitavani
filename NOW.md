# Now: Current Priorities

**Last Updated**: November 9, 2026

## Phase: Building Learn for 1.1

### Where It Stands

| Channel | Status | Notes |
|---------|--------|-------|
| iOS App Store | Live | v1.0.0, released Feb 18, 2026 |
| F-Droid | Live | v1.0.0. [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. Picks up new `v*` tags on its own |
| GitHub Releases | Live | v1.0.0 APK + AAB, built by GitHub Actions on tag push |
| Google Play | Not public | Internal testing since Feb 19, 2026. Production needs identity verification and 12 closed-test testers. Play Console not rechecked since March |

### Active

- **Version 1.1 is Word by word plus Learn, released together** (Nikhil, Nov 9).
  - Word by word: built on both platforms. iOS 1.1 (5) is in TestFlight (Nikhil's internal group; Family Testers after beta review). Android was checked on the Pixel.
  - Learn (Abhyāsa): built on both platforms, in device testing. Phase 2 of [docs/chant-coach-spec.md](docs/chant-coach-spec.md). CenterEye tasks #494 to #500. Left: lesson check on the iPhone, the labelled take set (needs recordings from Nikhil and family), Nikhil's 4.7 test on each phone, privacy page, store text.
  - App Store, Play and F-Droid releases wait for Nikhil's go.

### Before the Next Release

- **iOS:** nothing blocking. EU trader status was already set: non-trader, Digital Services Act "Active" since Nov 6, 2026 (checked in App Store Connect by the ClearNews session on Nov 9). An earlier note here said it was unset; that was wrong.
- **iOS submission checklist** (from ClearNews's 2.1 submission, Nov 9):
  - Apple's new age-rating question about social media must be answered first: App Information > Age Ratings > Edit. GitaVani's answer is No (sharing only opens the system share sheet). Left blank, adding the version to a submission fails with a 409.
  - Any new screenshots must finish processing before submitting.
  - Store text to paste is in `appstore/metadata/`.
- **F-Droid:** Learn uses Android's on-device speech recognizer. On most phones that's Google's Android System Intelligence. It isn't free software. Check whether F-Droid wants an anti-feature note before tagging 1.1.0. On phones without an on-device recognizer, the Learn button doesn't show.
- **Privacy page (gitavani.app/privacy):** add one paragraph about the microphone before release (spec section 9). Publishing the site needs Nikhil's go.
- **Google Play:** check identity verification in Play Console. Then recruit 12 testers for the closed test (the README has the callout).
