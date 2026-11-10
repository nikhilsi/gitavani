# Now: Current Priorities

**Last Updated**: November 9, 2026

## Phase: Building Learn for 1.1

### Morning Brief (end of Nov 9, 2026)

**Done today**
- Learn is built on iOS and Android and pushed to main. Learn is offered on 680 verses.
- Device checks: the iPhone streaming path gives the same marks as the earlier file trial (14 of 14 recordings). A full take works on the Pixel. Nikhil tried 1.1 on the iPhone (build 6) and it looked right.
- An independent code review found 9 bugs. All are fixed (crash on Next mid-take, reciter cut off at lesson start, Android mic left open, rotation restart, double taps).
- Docs, Help, spec and store text drafts are updated. Nothing has been submitted to any store.

**In flight**
- iOS 1.1 (7), Learn plus the fixes: in Nikhil's internal TestFlight group now. It is "Waiting for Review" for Family Testers, so Urmila gets it once Apple approves.
- Build 5 (Word by word only) was taken out of review, not expired. It won't ship.
- Urmila is updating her iPhone to iOS 27, which Learn needs. ClearNews: she already has 2.1 (6).
- Nikhil's tester entry had dropped out of his internal group (cause unknown). Re-added Nov 9.
- Family Testers in both apps also hold two old, never-accepted invites (Nikhil's kids). Left in place on purpose.

**To do (talk through in the morning)**
1. Nikhil's step-11 test on each phone: chant 4.7 from memory and get Learned. A take with a word left out must not get Learned. Restart: Learned stays.
2. Labelled take set (spec section 11): recordings from Nikhil (and Urmila if willing) to check the thresholds.
3. Privacy page paragraph (draft in the Nov 9 chat). Publishing the site needs Nikhil's go.
4. Read the store text drafts in `appstore/metadata`, `playstore/metadata` and `fastlane/metadata`.
5. F-Droid: decide whether Learn needs an anti-feature note (the speech recognizer is Google's on most phones).
6. Optional: hand-fix timings for the 13 verses without Learn (1.20, 1.21, 1.47, 2.1, 2.63, 2.72, 8.16, 10.42, 11.1, 11.55, 13.1, 16.1, 18.78).
7. Release 1.1 on all channels: only on Nikhil's go.

### Where It Stands

| Channel | Status | Notes |
|---------|--------|-------|
| iOS App Store | Live | v1.0.0, released Feb 18, 2026 |
| F-Droid | Live | v1.0.0. [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. Picks up new `v*` tags on its own |
| GitHub Releases | Live | v1.0.0 APK + AAB, built by GitHub Actions on tag push |
| Google Play | Not public | Internal testing since Feb 19, 2026. Production needs identity verification and 12 closed-test testers. Play Console not rechecked since March |

### Active

- **Version 1.1 is Word by word plus Learn, released together** (Nikhil, Nov 9).
  - Word by word: built on both platforms. Android was checked on the Pixel.
  - Learn (Abhyāsa): built on both platforms, in device testing. Phase 2 of [docs/chant-coach-spec.md](docs/chant-coach-spec.md). CenterEye tasks #494 to #500. What's left is in the Morning Brief above.
  - TestFlight: iOS 1.1 (7) has both features. Internal group now, Family Testers after beta review.
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
