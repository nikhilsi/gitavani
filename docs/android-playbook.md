# Android Distribution Playbook

> A practical, opinionated guide for iOS developers shipping their first Android apps.
> Based on lessons learned building and distributing GitaVani (com.nikhilsi.gitavani).
>
> Created: March 7, 2026

---

## 1. Android Project Setup (from iOS Port)

### Project Structure

Android projects live alongside iOS in the same repo:

```
your-app/
├── ios/YourApp/          # Xcode project
├── android/YourApp/      # Android Studio project
├── data/                 # Shared data files
├── scripts/              # Shared tooling
└── playstore/            # Store assets (separate from android/)
```

The parts of the Android project you actually care about:

```
android/YourApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/yourname/yourapp/   # Kotlin source
│   │   │   ├── model/                   # Data classes
│   │   │   ├── data/                    # Data loading services
│   │   │   ├── state/                   # Settings, preferences
│   │   │   ├── theme/                   # AppTheme + MaterialTheme wrapper
│   │   │   ├── ui/                      # Compose screens
│   │   │   ├── viewmodel/              # ViewModels
│   │   │   └── MainActivity.kt
│   │   ├── assets/                      # Bundled files (JSON, audio, etc.)
│   │   └── res/                         # Icons, strings, themes.xml
│   ├── build.gradle.kts                 # App-level build config (THE IMPORTANT ONE)
│   └── proguard-rules.pro              # R8 keep rules
├── build.gradle.kts                     # Project-level (rarely touched)
├── gradle/libs.versions.toml           # Version catalog (dependency versions)
├── gradlew                              # Gradle wrapper (commit this)
└── local.properties                     # SDK path (gitignored, auto-generated)
```

### iOS to Android Mental Model

| iOS Concept | Android Equivalent |
|---|---|
| SwiftUI View | @Composable function |
| @Observable class | ViewModel + StateFlow |
| UserDefaults | SharedPreferences |
| NavigationStack | Navigation Compose (NavHost + routes) |
| Bundle.main.url(forResource:) | `assets/` folder + `context.assets.open()` |
| AVAudioPlayer | MediaPlayer |
| DragGesture | HorizontalPager |
| UINavigationBarAppearance | TopAppBar + MaterialTheme |
| UIActivityViewController | `Intent.ACTION_SEND` |
| .searchable() | SearchBar composable |

### Data Bundling: assets/ vs res/

- **`assets/`** — Raw files accessed by filename at runtime. Use for JSON data, audio, anything you'd put in the iOS bundle. Accessed via `context.assets.open("data.json")`.
- **`res/`** — Structured resources (icons, strings, themes). Use for app icon mipmaps, `strings.xml`, `themes.xml`.

Rule of thumb: if you'd use `Bundle.main.url(forResource:)` on iOS, put it in `assets/`.

---

## 2. Building & Signing

### Setting JAVA_HOME

Android Studio bundles its own JDK. Add to `~/.zshrc`:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

### Debug vs Release Builds

```bash
cd android/YourApp

# Debug APK (no signing needed)
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release APK (requires signing config)
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk

# Release AAB (for Play Store)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

APK = installable directly on devices. AAB = upload format for Play Store.

### Keystore Generation

A keystore is Android's signing certificate. Create it once, use it forever. **If you lose it, you cannot update your app on the Play Store.**

```bash
mkdir -p android/YourApp/keystore

keytool -genkeypair \
  -v \
  -keystore android/YourApp/keystore/yourapp-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias yourapp-release \
  -dname "CN=Your Name, O=YourApp, C=US"
```

**Immediately save the password in your password manager.**

### keystore.properties Pattern

Never hardcode passwords. Create a gitignored properties file:

```properties
# android/YourApp/keystore.properties  (GITIGNORED)
storeFile=../keystore/yourapp-release.jks
storePassword=your-keystore-password
keyAlias=yourapp-release
keyPassword=your-key-password
```

Add to `.gitignore`:
```
android/YourApp/keystore.properties
android/YourApp/keystore/
```

### build.gradle.kts Signing Config

```kotlin
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile") ?: "")
            storePassword = keystoreProperties.getProperty("storePassword") ?: ""
            keyAlias = keystoreProperties.getProperty("keyAlias") ?: ""
            keyPassword = keystoreProperties.getProperty("keyPassword") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Conditional: allows F-Droid builds without keystore
            signingConfig = if (keystorePropertiesFile.exists()) signingConfigs.getByName("release") else null
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### ProGuard/R8 Rules for kotlinx.serialization

Without these, release builds crash while debug works fine:

```proguard
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.yourname.yourapp.model.**$$serializer { *; }
-keepclassmembers class com.yourname.yourapp.model.** { *** Companion; }
-keepclasseswithmembers class com.yourname.yourapp.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
```

---

## 3. Testing on Emulators

### Creating Virtual Devices

In Android Studio: **Tools > Device Manager > Create Virtual Device**

| Purpose | Device | System Image |
|---|---|---|
| Phone | Pixel 7 | API 35 |
| 7" Tablet | Nexus 7 (obsolete list) | API 35 |
| 10" Tablet | Pixel Tablet | API 35 |

### Key adb Commands

```bash
# List connected devices
~/Library/Android/sdk/platform-tools/adb devices

# Install APK
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Take screenshot
~/Library/Android/sdk/platform-tools/adb exec-out screencap -p > screenshot.png

# Target specific emulator (when multiple running)
~/Library/Android/sdk/platform-tools/adb -s emulator-5554 exec-out screencap -p > phone.png

# Uninstall (when install fails due to signature mismatch)
~/Library/Android/sdk/platform-tools/adb uninstall com.yourname.yourapp
```

### Common Issues

- **"JAVA_HOME is not set"** — Export JAVA_HOME (see above)
- **Emulator won't start** — Kill stale processes: `pkill -f emulator`
- **Multiple emulators** — Use `adb devices` to list, target with `-s emulator-XXXX`
- **"INSTALL_FAILED_UPDATE_INCOMPATIBLE"** — Uninstall first: `adb uninstall com.yourname.yourapp`

---

## 4. Distribution Channel 1: GitHub Releases (Fastest)

No gatekeepers, no 12-tester requirement. Users download the APK directly.

### GitHub Actions Workflow

Create `.github/workflows/android-release.yml`:

```yaml
name: Android Release

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Decode keystore
        run: |
          mkdir -p android/YourApp/keystore
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > android/YourApp/keystore/yourapp-release.jks

      - name: Create keystore.properties
        run: |
          cat > android/YourApp/keystore.properties << EOF
          storePassword=${{ secrets.KEYSTORE_PASSWORD }}
          keyPassword=${{ secrets.KEY_PASSWORD }}
          keyAlias=${{ secrets.KEY_ALIAS }}
          storeFile=../keystore/yourapp-release.jks
          EOF

      - name: Build release APK
        working-directory: android/YourApp
        run: ./gradlew assembleRelease

      - name: Build release AAB
        working-directory: android/YourApp
        run: ./gradlew bundleRelease

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: |
            android/YourApp/app/build/outputs/apk/release/app-release.apk
            android/YourApp/app/build/outputs/bundle/release/app-release.aab
          generate_release_notes: true
```

### GitHub Secrets to Set

```bash
base64 -i android/YourApp/keystore/yourapp-release.jks | gh secret set KEYSTORE_BASE64
gh secret set KEYSTORE_PASSWORD  # paste password
gh secret set KEY_ALIAS           # e.g., yourapp-release
gh secret set KEY_PASSWORD        # paste password
```

### Triggering a Release

```bash
git tag v1.0.0
git push origin v1.0.0
# GitHub Actions builds and publishes automatically
```

---

## 5. Distribution Channel 2: F-Droid (Open Source)

F-Droid is an app store for free, open-source Android apps. Perfect for apps with no ads, no tracking, no proprietary dependencies.

### Requirements

- Source code publicly available
- FOSS license (MIT, Apache, GPL, etc.)
- No proprietary dependencies (no Firebase, no Google Play Services)
- No tracking/analytics

### Fastlane Metadata (in your repo)

F-Droid auto-picks up metadata from this structure:

```
fastlane/metadata/android/en-US/
├── title.txt                    # App name
├── short_description.txt        # 80 chars max
├── full_description.txt         # 4000 chars max
├── changelogs/
│   └── 1.txt                   # Changelog for versionCode 1
└── images/
    ├── icon.png                 # 512x512
    ├── featureGraphic.png       # 1024x500
    ├── phoneScreenshots/
    │   ├── 1.png
    │   └── ...
    ├── sevenInchScreenshots/
    └── tenInchScreenshots/
```

### Submission Process

1. Create a **GitLab account** at https://gitlab.com
2. Fork https://gitlab.com/fdroid/fdroiddata
3. Create `metadata/com.yourname.yourapp.yml`:

```yaml
Categories:
  - Reading
License: MIT
AuthorName: Your Name
SourceCode: https://github.com/yourname/yourapp
IssueTracker: https://github.com/yourname/yourapp/issues

Summary: One-line description of your app
Description: |
  Multi-line description here.

RepoType: git
Repo: https://github.com/yourname/yourapp.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: android/YourApp/app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
```

4. Submit a merge request
5. Wait 2-4 weeks for review
6. After merge, app appears in F-Droid within 24-48 hours
7. Future updates are **automatic** — F-Droid detects new tags

### Important: Make signing config conditional

F-Droid builds from source with their own keys. Your `build.gradle.kts` must not fail without a keystore:

```kotlin
signingConfig = if (keystorePropertiesFile.exists()) signingConfigs.getByName("release") else null
```

---

## 6. Distribution Channel 3: Google Play Store

### Setup Sequence

1. Create developer account ($25 one-time) at play.google.com/console
2. **Device verification** — requires a physical Android phone (since 2024)
3. **Identity verification** — 1-7 business days
4. Create app, complete content declarations, upload store listing
5. Internal testing → Closed testing (12 testers!) → Production

### Content Declarations (for free, offline, no-ads apps)

| Declaration | Answer |
|---|---|
| Privacy policy | Your privacy URL |
| App access | All functionality available without special access |
| Ads | No |
| Content rating | Complete IARC questionnaire (usually "Everyone") |
| Target audience | **18+** (avoids COPPA requirements) |
| Data safety | No data collected |
| Government app | No |
| Financial features | No |
| Health app | No |

### Graphics Requirements

| Asset | Size |
|---|---|
| App icon | 512x512 px |
| Feature graphic | 1024x500 px |
| Phone screenshots | Min 2, recommended 6 |
| 7-inch tablet screenshots | Min 2, recommended 4 |
| 10-inch tablet screenshots | Min 2, recommended 4 |

### The 12-Tester Wall

Google requires 12 opted-in testers in a closed test before you can publish to production. Strategies:

1. **Personal network** — family, friends, coworkers with Android phones
2. **GitHub README callout** — ask your open-source community
3. **Reddit** — r/AndroidDev, r/AppTesting, topic-specific subreddits
4. **Ship via GitHub/F-Droid first** — recruit testers from existing users

### AAB Size Limits

Base AAB limit is **150 MB**. Check early:
```bash
./gradlew bundleRelease
ls -lh app/build/outputs/bundle/release/app-release.aab
```

If over 150 MB, you need Play Asset Delivery to split assets into packs.

---

## 7. Releasing Updates

```bash
# 1. Bump versionCode (integer, must increment) and versionName in build.gradle.kts
# 2. Update changelogs
# 3. Commit and tag
git commit -am "Release v1.1.0"
git tag v1.1.0
git push origin main --tags
```

- **GitHub**: Automatic release via Actions
- **F-Droid**: Automatic detection of new tag (1-2 week build cycle)
- **Play Store**: Manual — upload new AAB in Play Console

---

## 8. Quick Reference

### Commands

```bash
# Build
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Signed release APK
./gradlew bundleRelease          # Signed release AAB

# Device
adb devices                      # List devices
adb install -r app.apk           # Install APK
adb uninstall com.yourname.app   # Uninstall
adb exec-out screencap -p > s.png  # Screenshot

# Release
git tag v1.0.0 && git push origin v1.0.0

# Keystore
keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
base64 -i release.jks | gh secret set KEYSTORE_BASE64
```

### File Locations

| What | Where |
|---|---|
| App source | `android/YourApp/app/src/main/java/com/yourname/yourapp/` |
| Bundled assets | `android/YourApp/app/src/main/assets/` |
| Build config | `android/YourApp/app/build.gradle.kts` |
| Version catalog | `android/YourApp/gradle/libs.versions.toml` |
| Keystore (gitignored) | `android/YourApp/keystore/` |
| Debug APK | `android/YourApp/app/build/outputs/apk/debug/app-debug.apk` |
| Release AAB | `android/YourApp/app/build/outputs/bundle/release/app-release.aab` |

---

## Appendix: App Porting Checklist

```
[ ] Create Android Studio project (Empty Compose Activity)
[ ] Set up build.gradle.kts (namespace, SDK versions, dependencies)
[ ] Port data models (Swift structs → Kotlin @Serializable data classes)
[ ] Port data service (Bundle loading → assets/ loading)
[ ] Port theme system (SwiftUI colors → Compose Color + MaterialTheme)
[ ] Port navigation (NavigationStack → Navigation Compose)
[ ] Port screens one by one (SwiftUI Views → @Composable functions)
[ ] Port state management (@Observable → ViewModel + StateFlow)
[ ] Port settings (UserDefaults → SharedPreferences)
[ ] Test on phone + tablet emulators
[ ] Generate keystore + keystore.properties
[ ] Add ProGuard rules
[ ] Build release APK/AAB and verify size (< 150 MB?)
[ ] Create app icon (512x512) + feature graphic (1024x500)
[ ] Take screenshots (phone + 7" + 10")
[ ] Write store metadata
[ ] Set up GitHub Actions for automated releases
[ ] Tag and push for GitHub Release
[ ] Add Fastlane metadata and submit to F-Droid
[ ] Create Play Console listing
[ ] Upload to internal testing → closed testing → production
```

---

*Written after shipping GitaVani to GitHub Releases, F-Droid (pending), and Google Play (pending closed testing).*
