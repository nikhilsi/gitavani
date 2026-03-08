# Now — Current Priorities

**Last Updated**: March 7, 2026

## Phase: Android Distribution — Multi-Channel Release

### iOS Status
V1.0.0 (build 3) submitted for App Store review. Waiting.

### Android Status
Feature complete. Distributed via GitHub Releases. F-Droid and Play Store pending.

### Distribution Channels

| Channel | Status | Notes |
|---------|--------|-------|
| GitHub Releases | Live | APK + AAB auto-built on tag push via GitHub Actions |
| F-Droid | Pending review | [MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390) submitted Mar 7 — expect 2-4 week review |
| Google Play Store | Internal testing | Blocked on 12 testers for closed testing + identity verification |

### What's Done
- Native Kotlin + Jetpack Compose app — full feature parity with iOS
- Release signing with conditional config (works without keystore for F-Droid)
- GitHub Actions workflow (`.github/workflows/release.yml`) — auto-builds on `v*` tags
- GitHub Secrets configured (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
- v1.0.0 tag pushed — triggered automated release build
- Fastlane metadata structure in repo (`fastlane/metadata/android/en-US/`)
- F-Droid metadata submitted to fdroiddata ([MR #34390](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/34390))
- Play Store listing, screenshots, and internal testing release
- Android distribution playbook (`docs/android-playbook.md`)

### Waiting On
1. **F-Droid review** — 2-4 weeks, may request changes via GitLab
2. **Play Store identity verification** — pending from Google
3. **12 testers for closed testing** — recruiting via README callout and community

### Completed Phases
- ~~Phase 1-10: Android app development~~
- ~~Phase 11: Documentation~~
- ~~Phase 12: Distribution setup (GitHub + F-Droid + Play Store)~~
