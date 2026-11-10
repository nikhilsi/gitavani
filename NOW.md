# Now: Current Priorities

**Last Updated**: November 9, 2026

## Phase: Word by Word (1.1) in Testing, Learn Next

### Where It Stands

| Channel | Status | Notes |
|---------|--------|-------|
| iOS App Store | Live | v1.0.0, released Feb 18, 2026 |
| F-Droid | Live | v1.0.0. [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) merged Apr 27, 2026. Picks up new `v*` tags on its own |
| GitHub Releases | Live | v1.0.0 APK + AAB, built by GitHub Actions on tag push |
| Google Play | Not public | Internal testing since Feb 19, 2026. Production needs identity verification and 12 closed-test testers. Play Console not rechecked since March |

### Active

- **Word by word (version 1.1).** Built on iOS and Android, Phase 1 of [docs/chant-coach-spec.md](docs/chant-coach-spec.md).
  - Next: TestFlight and Play internal test builds, then Nikhil checks 10 verses on each phone.
  - App Store, Play production and F-Droid releases wait for his go.
- **Learn (Abhyāsa), the chant coach.** Phase 2 of the same spec. Starts after 1.1 ships.

### Before the Next Release

- **iOS:** nothing blocking. EU trader status was already set: non-trader, Digital Services Act "Active" since Nov 6, 2026 (checked in App Store Connect by the ClearNews session on Nov 9). An earlier note here said it was unset; that was wrong.
- **Google Play:** check identity verification in Play Console. Then recruit 12 testers for the closed test (the README has the callout).
