# Learn (Abhyāsa): Chant Coach Spec

**Status:** Reviewed. Revised after the independent review (docs/reviews/chant-coach-spec-review.md) and the Android trial. Phase 1 is built. Phase 2 is being built.  
**Written:** November 9, 2026  
**Release:** both phases ship together as version 1.1 (D8). Phase 1 (Word by word) is built and in TestFlight. Phase 2 (Learn) is being built.

---

## 1. Summary

**Phase 1, Word by word.** Every verse gets a "Word by word" section: each word in Devanagari and Roman letters, with its meaning. No speech code. It works on every device GitaVani supports today.

**Phase 2, Learn.** A practice mode on every verse. GitaVani teaches you to chant the verse one line at a time. You say each line back. The phone listens on the device and shows which words were right and which slipped, with one tip for the next try. The lesson ends when you chant the whole verse from memory.

None of the Gita apps we surveyed listens back. Several play recordings to imitate.

---

## 2. Why

Review research on Nov 9, 2026 covered 1,324 written iOS reviews across 57 Gita apps in the US and India stores. All 419 low-star or request reviews were read in full.

- 19 reviews in 11 apps are about learning to chant, pronounce or memorize a verse. People already use recordings as a teacher: "We can listen to each verse and try to repeat along with audio."
- 14 reviews mention word-by-word meaning. About half praise apps that have it. The other half ask for it.
- The most common complaints (ads, paywalls, forced login, needing internet) are ones GitaVani already avoids.
- The app's first user is part of a Gita study group (architecture.md 1.2).

---

## 3. Decisions

| # | Decision | Date |
|---|----------|------|
| D1 | An app feature, not a website. | Nov 9, 2026 |
| D2 | Listening uses each platform's built-in on-device recognizer with its Hindi model: Apple SpeechAnalyzer (hi-IN) and Android's on-device SpeechRecognizer (hi-IN). No speech model ships in the app. Whisper was ruled out (1.5 GB). | Nov 9, 2026 |
| D3 | Word meanings come from Swami Sivananda's word lists, already in gita_data.json, with checks and a hand review (8.2). | Nov 9, 2026 |
| D4 | Needing a connection once, for the platform's speech model, is fine. The reading app itself stays offline. | Nov 9, 2026 |
| D5 | The button says "Learn". The lesson screen is titled "Abhyāsa". The Help screen explains the name with 6.35. | Nov 9, 2026 |
| D6 | Version 1 checks skipped and wrong words only. No pause check, no pronunciation, no melody. | Nov 9, 2026 (pause check cut after review R2) |
| D7 | No third-party libraries. Free, no ads, no login. Audio never leaves the phone. | Nov 9, 2026 |
| D8 | Two phases, built in order: Word by word first (review R14), then Learn. Both ship together in one release, 1.1. Nikhil: Word by word alone is too small a release. | Nov 9, 2026 |
| D9 | "Not checked" is decided on the phone when a lesson opens, by running the reciter's lines through the phone's own recognizer. | Nov 9, 2026 (review R5) |
| D10 | Expected text and line breaks come from the Devanagari, not the transliteration field. | Nov 9, 2026 (review R7) |
| D11 | Android ships Learn together with iOS. The Pixel 8a trial matched Apple's results (section 15). | Nov 9, 2026 |
| D12 | Learn on iOS needs iOS 27 or later. Step 0 confirmed hi-IN on an iPhone 15 Pro running iOS 27.0.1. No iOS 26 device was available, and iOS 26 would also need extra audio conversion code. | Nov 9, 2026 |

---

## 4. Who It's For

- People with little or no Sanskrit. They can't tell on their own where they slipped.
- Families teaching children at home.
- Anyone getting ready to recite at a puja, a class or a study group.

---

## 5. Phase 1: Word by Word

### 5.1 What the Reader Sees

- A **Word by word** section under the Sanskrit card on the verse screen. Closed by default. Its open or closed state is remembered, like the other reading settings.
- Each verse line shows as a group of word parts, one under another, with a divider between lines. Each part shows its Devanagari and Roman form on one line (stacked when they don't fit) and its meaning below.
- Speaker lines ("śrībhagavān uvāca", 60 verses) have no parts. Sivananda's lists skip them, and the Sanskrit card above already shows them.
- Credit line at the bottom: "Word meanings: Swami Sivananda."
- Verses without a clean list show "Word meanings aren't available for this verse yet."

### 5.2 Platforms

iOS 17 and later, Android 8 and later. No permission prompts, nothing new to download.

### 5.3 Why It Goes First

- It answers the 14 reviews about meanings, for every user.
- It builds and proves the data pipeline (section 8) that Learn depends on.
- It ships with no speech code and no new risk.

---

## 6. Phase 2: Learn

### 6.1 Entry

- A fifth button, **Learn**, in the verse screen's action bar, after Play, Romanize, Save and Share.
- Shown only when the device can do it and the verse's line timings passed the per-line check (8.4): on iOS 27 and later, `SpeechTranscriber.isAvailable` is true and hi-IN resolves through `supportedLocale(equivalentTo:)`. On Android 13 and later, the phone has an on-device recognizer. (Android 13, not 12: feeding audio through a pipe, segmented sessions and the language check all need API 33.) Whether hi-IN is installed is checked when the lesson opens (6.2).
- Tapping it opens the lesson full screen. Verse swiping is off inside. A Close button returns to the verse.

### 6.2 First Use and Every Lesson Open

1. **Microphone permission**, with a plain reason: "GitaVani listens while you chant so it can show where you slipped. Audio stays on your phone."
2. **Speech model check, at every lesson open.** The system can remove an unused model. If it's missing: "Learn needs a download from Apple" (or "from Google"), with a button and progress. Android shows its own approval prompt.
3. **If something fails** (permission denied, no connection, download error): a calm message saying what to do. The rest of the app is unaffected.

### 6.3 The Lesson (Abhyāsa)

1. **Listen.** The reciter chants the whole verse. Meanwhile, the phone runs each reciter line through its own recognizer and marks the parts it can't hear as Not checked (D9).
2. **Line by line.** For each line:
   - The reciter's line plays.
   - The line shows in Devanagari and Roman letters. Under it, its word parts, one per row as in Word by word, each with its meaning and, after a take, its mark.
   - The learner taps the mic and chants. Recording stops on a second tap, 1.5 s after the last expected word is heard, or after 4 s of silence.
   - Each part in the row gets a mark: Right, Slipped or Not checked. The verse line itself stays unmarked (review R11).
   - One tip shows.
   - Buttons: Hear reciter, Hear my try, Try again, Next line. Next line is always available.
3. **Join.** Both lines together, same marks.
4. **From memory**, in two takes. First only the first letter of each word shows, then nothing. The word parts show only after a take. Shown earlier, they would give the verse away. Recording never stops on silence here, only on a tap or after the last expected word. If every checkable part is Right in the take with nothing shown, the verse is marked Learned.

The lesson shows "n of m words checked" so a verse with many Not checked parts doesn't look tested when it isn't (review R13).

### 6.4 Marks

- **Right:** the theme's accent color and a check mark.
- **Slipped:** a warm color, contrast-checked in all four themes, and a dot.
- **Not checked:** the secondary text color and a hollow circle. A one-line note explains it the first time: "Not checked: the phone can't hear this word reliably yet."
- Color is never the only signal. Each mark has a symbol and a VoiceOver label.

### 6.5 Tips

One tip per try, in this order:

1. **Skipped part:** "You skipped adharmasya."
2. **Slipped part:** "Listen again to sṛjāmyaham." Tapping it replays the reciter's line.
3. **Take ended early:** "That take didn't finish. Try the whole line." Never reported as skipped words.
4. **All right:** "All words right. Next line when you're ready."

### 6.6 Progress

- Each verse is New, Learning or Learned. Stored on the phone.
- Shown on the verse screen only, as a small mark on the Learn button. No list, no marks on verse rows, no streaks or reminders in version 1 (review R14).

### 6.7 States

| Situation | Behavior |
|-----------|----------|
| Phone call, Siri or alarm during a take | Stop and discard the take. |
| Headphones or AirPods connect or disconnect | Pause the step. Continue on the new route. |
| App goes to the background | Stop and discard the take. GitaVani has no background audio mode. |
| VoiceOver speaking while recording | Recording starts after VoiceOver finishes its announcement. Recommend headphones in Help. |
| iPad | The parts row wraps. Same flow. |
| Device or OS can't do it | No Learn button. Nothing else changes. |
| The phone's recognizer stops answering | The take ends, keeping what was heard. If nothing was heard, a calm message asks to try again. A language check that doesn't answer in 10 s says so. (Seen on the Pixel 8a, Nov 9: once the recognizer never reported the end of a session, then stopped answering until Android System Intelligence restarted.) |

---

## 7. How It Works

### 7.1 iOS Parts

| Part | Folder | Job |
|------|--------|-----|
| `ChantData` | Models/ | Decodes `chant_data.json`. |
| `LearnProgress` | State/ | @Observable with UserDefaults, like ReadingProgress. Key `learnProgress` holds JSON `{ verseId: "learning" or "learned" }`. |
| `ChantListener` | Services/ | Records with AVAudioEngine. Uses iOS 27's built-in format conversion for the analyzer. Streams to SpeechAnalyzer. Calls `prepareToAnalyze` before the first take. Releases each analyzer after use. Keeps the take in memory for "Hear my try". |
| `ChantScorer` | Services/ | Plain Swift. Input: expected parts, transcript. Output: marks and one tip. Unit tested. |
| Lesson views | Views/Learn/ | The lesson screens. |
| `WordByWordView` | Views/Verses/ | Phase 1 section. |
| `AudioService` change | Services/ | Gains `play(verseId:from:to:)` for one line. |

Audio session: AudioService uses `.playback`. The lesson switches to `.playAndRecord` with A2DP Bluetooth allowed (good playback, phone mic) and switches back on close. Tested with AirPods during the build.

Info.plist keys go in build settings, since the project generates its Info.plist: `INFOPLIST_KEY_NSMicrophoneUsageDescription` and `INFOPLIST_KEY_NSSpeechRecognitionUsageDescription` (SpeechAnalyzer doesn't prompt, but Apple's key page says to include it).

### 7.2 Android Parts

Same shape in Kotlin: ChantData, LearnProgress (SharedPreferences), ChantListener, ChantScorer (a port of the Swift rules), Compose views.

Learned on the Pixel 8a trial (section 15):

- The on-device recognizer is Android System Intelligence. It allows one session at a time.
- Use `createOnDeviceSpeechRecognizer` (Android 12+). Check hi-IN with `checkRecognitionSupport` and fetch it with `triggerModelDownload` (Android 13+). Android shows its own approval prompt.
- Keep one recognizer for the whole lesson. Opening a second session right after the support check failed with a dead connection.
- Use a segmented session so the recognizer doesn't stop at the first pause.
- The app records the mic itself (AudioRecord, VOICE_RECOGNITION source, 16 kHz) and feeds the recognizer through a pipe. The take stays in memory for "Hear my try". Calibration lines go through the same pipe (MP3 decoded and resampled to 16 kHz).
- Feed the pipe from a plain thread and watch for signs of life (audio taken in, any callback). After 5 s with none, end the session (section 6.7).
- Output is Devanagari. A small Devanagari to Roman converter (also used in section 8) feeds the scorer.

### 7.3 Scoring

Starting rules. All thresholds get tuned on the labelled take set (section 11) before they ship.

1. **Expected text** is built from the verse's Devanagari with the converter. Vowels swallowed at an avagraha (ऽ) are dropped, so the expected text matches the chant (review R7).
2. **Sound key.** Expected and heard text go through the same folding: diacritics removed; bh, dh, th, kh, gh, jh and ph fold to their plain letters; ch to c; sh, ś and ṣ to s; f to p; w to v; ri and ru to r; ee to i; oo to u; doubled letters collapse.
3. **Alignment.** Weighted edit distance. A vowel swapped for a vowel costs 0.5. Dropping or adding "a" or "h" costs 0.3. A transposition-aware cost gets tried for slips like "smarajyaham" (review R4).
4. **Score per part.** Each part from the word list scores 1 minus its cost divided by its length. The starting threshold is 0.6. The trial shows real slips at 0.64 and 0.65, so expect it to move up.
5. **Extra speech** before or after the line costs nothing.
6. **Not checked** parts never count against the learner (D9).

Known limits in version 1:
- A dropped word-initial "a" (adharmasya heard as dharmasya) can't be caught reliably. Recognizers drop it on correct chants too (review R3).
- Parts of one or two letters pass only about 90% of the time on correct chants. Many will end up Not checked.

---

## 8. Data Prepared Ahead of Time

`scripts/build_chant_data.py` writes `data/chant_data.json` (target under 1 MB). It gets copied into the iOS resources and the Android assets. It runs on a Mac. Its helper tools (ffmpeg, a Swift transcription tool) never ship.

### 8.1 Shape

```json
{
  "BG2.47": {
    "lines": [
      {
        "deva": "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
        "roman": "karmaṇyevādhikāraste mā phaleṣu kadācana",
        "audio": [0.00, 6.40],
        "parts": [
          { "deva": "कर्मणि", "roman": "karmaṇi", "meaning": "in work", "span": [0, 7] },
          { "deva": "एव", "roman": "eva", "meaning": "only", "span": [7, 10] }
        ]
      }
    ],
    "skip_audio": []
  }
}
```

- `span` is the character range of the part inside `roman`.
- `skip_audio` lists stretches that aren't practiced: speaker lines and chapter-closing lines.

### 8.2 Word Parts and Meanings (Phase 1)

- **Source:** the word list at the start of Swami Sivananda's English commentary. About 692 of 701 verses have one.
- **Lining up parts with the chant:** the list gives dictionary forms (karmaṇi, eva). The chant joins them (karmaṇyeva). The script aligns parts to the line with the scorer's alignment.
- **Checks that must pass:**
  - The list's header number matches the verse (18.45 carries 5.10's list today).
  - Alignment cost under a cap, and at least one letter per part.
  - Spans in order, no gaps, no overlaps. (Phase 2. Phase 1 places parts on lines without spans.)
  - The list covers only this verse (1.21 also carries 1.22).
- **Cleanup map** for damaged glosses: missing "qu" ("eanimity" for equanimity), lost hyphens and apostrophes ("mightyarmed", "dont").
- **Hand review** of every verse a check flags. Fix by hand or mark the verse "not available yet".
- **Language:** English meanings only in version 1.

### 8.3 Lines

Lines come from the Devanagari, which marks quarter-verse breaks in long meters such as chapter 11. Version 1 practices by printed line. Chapter 11's long lines split at their quarter-verse break if testing shows they're too long.

### 8.4 Line Timings (Phase 2)

- The break is the longest quiet stretch between the start of a line's last word and the end of the next line's first word, at least 0.3 s long (review R8).
- **Check:** each line's audio, run through the recognizer alone, matches its words. Outliers get listened to by hand.

### 8.5 Validation

`scripts/validate_gita_data.py` gains a chant_data.json section: every verse present, every check in 8.2 passing or the verse marked unavailable, timings inside each recording.

---

## 9. Privacy, Licensing and Store Review

- **Privacy manifest:** no collected data types added. The App Store label stays "Data Not Collected".
- **gitavani.app privacy page:** one paragraph about the microphone (Phase 2).
- **App Review notes:** say why the app uses the microphone.
- **License:** no change (Nikhil, Nov 9, 2026). chant_data.json follows gita_data.json. Credit Sivananda in the Word by word section.

---

## 10. Constraints Kept

- No third-party libraries on either platform.
- Reading, audio, search, favorites and Word by word work with no internet. Learn needs internet once per speech model.
- The app grows by chant_data.json only.
- Free. No ads. No login.

---

## 11. Testing

- **Labelled take set, before any threshold is set (review R4).** Several voices (Nikhil, Urmila if she's willing, a child if possible, a few accents) on at least 20 verses. Each verse gets a correct take and takes with planted mistakes, labelled by part. Half the set is held back for the final check. Voices stay outside the repo.
- **Targets on the held-back half:**
  - Correct takes: under 2% of checked parts marked Slipped.
  - Planted mistakes: at least 90% caught, with the tip naming the right word.
- **Unit tests** for ChantScorer on both platforms. Both check the shared cases in `scripts/data/scorer_fixtures.json`, made from the Python reference by `scripts/build_scorer_fixtures.py`.
  - iOS: `cd ios/ChantScorerTests && swift test`. A small Swift package compiles the app's own ChantScorer.swift through a symlink. The Xcode project stays as it is.
  - Android: `cd android/GitaVani && ./gradlew testDebugUnitTest`. JUnit 4 (`testImplementation` only), as the Android testing guide recommends for local tests. It never ships in the app.
- **Devices.** iPhone 15 Pro and iPad on iOS 27. Pixel 8a, plus one non-Pixel Android phone if one is available.
- **Accessibility.** Marks in all four themes, Dynamic Type, VoiceOver.

---

## 12. Build Steps

Each step becomes a task when work starts. Each ends with a check.

**Step 0, device checks: done Nov 9, 2026**
1. iPhone 15 Pro, iOS 27.0.1: hi-IN available. Gate set to iOS 27 (D12).
2. Pixel 8a, Android 17: hi-IN available after a one-time download (section 15).

**Phase 1, Word by word**
3. Approval line, given Nov 9, 2026: "Build Phase 1 on iOS and Android. Commit and push to main as you go. Test builds to TestFlight and Play internal testing are OK. App Store, Play production and F-Droid releases need my go."
4. `build_chant_data.py` word parts with every check in 8.2. Done when validation passes and flagged verses are hand-reviewed.
5. Word by word view on iOS and Android. Done when Nikhil checks 10 verses on each phone.
6. Ships with Phase 2 as 1.1 (D8), on Nikhil's go. EU trader status is already set (non-trader, since Nov 6, 2026), so it doesn't block.

**Phase 2, Learn**

Approval line, given Nov 9, 2026: "Build Learn (Phase 2) on iOS and Android. Commit and push to main as you go. Test builds to TestFlight are OK. App Store, Play and F-Droid releases need my go." Tasks #494 to #500.

7. Line timings (8.4). Done when every line passes the per-line check.
8. Labelled take set (section 11).
9. ChantScorer on both platforms with tests. Done when the held-back targets pass.
10. ChantListener on both platforms. Done when a recording fed through the live streaming path gives the same marks as the file trial.
11. Lesson UI. Done when Nikhil chants 4.7 from memory on each phone and gets Learned, and a take with a planted mistake does not get Learned.
12. Progress mark. Done when Learned survives an app restart.
13. Docs and pages: Help (including the 6.35 note), About, privacy page, README, architecture docs, CURRENT_STATE, CHANGELOG. Soften the "works without internet, always" line to match D4.
14. Family test round, then release.

---

## 13. Risks

- **iOS 26 users don't get Learn** until they update to iOS 27. Word by word still works for them.
- **Voices vary more than the trial covered.** The labelled take set and its targets catch that.
- **Recognizer updates can change results.** On-device calibration (D9) adjusts on its own. The take set gets re-run after each major OS release.
- **Sivananda lists have errors.** The checks in 8.2 and the hand review handle them. A wrong meaning is worse than a missing one, so unsure verses show "not available yet".
- **Android variety.** Phones without Google's on-device recognizer get no Learn button.

---

## 14. Open Questions

1. **Hindi word meanings.** Lean: English only in version 1.
2. **Keeping a take after the lesson closes.** Lean: no.

---

## 15. Trial Record (Nov 9, 2026)

- **Mac, Apple SpeechAnalyzer hi-IN (macOS 27.2):** 701 reference recordings in 6 minutes, 0 errors. Per whole word, 97.5% verified. Per word part (the reviewer's simulation), 96.9% of parts checkable and 503 of 693 verses fully checkable.
- **Planted mistakes (Apple):** a skipped line (1.1) and a wrong second line (4.7) were caught. Speed 1.4x, a higher voice and room noise gave no false marks.
- **Learner takes:** three iPhone recordings of 4.7 by Nikhil. Apple heard "samrajya, hum", "shramajyam" and "smaraj" for sṛjāmyaham. Whisper agreed on every slip but the "a" of "adharmasya".
- **Pixel 8a (Android 17), on-device hi-IN:** the same 14 recordings fed through a pipe. Results matched Apple's: the skipped and wrong lines caught, no false marks on speed, voice, noise or pause. It also flagged sṛjāmyaham in take 2, which Apple's recognizer passed. Hindi needed a one-time download, approved on the phone.
- **iPhone 15 Pro (iOS 27.0.1), Apple SpeechTranscriber hi-IN:** `isAvailable` true. The same 16 Indian locales as the Mac. hi-IN wasn't installed. It downloaded inside the test app with no prompt. All 14 recordings transcribed, about 0.8 s each. 13 of 14 transcripts were identical to the Mac's. The 14th differed by one letter (w for v), which the scorer folds together.
- **Scratch tools** (temporary session folder): score.py, deva.py, the Swift transcription tool, the Android test app. The useful parts move into scripts/ with step 4.
