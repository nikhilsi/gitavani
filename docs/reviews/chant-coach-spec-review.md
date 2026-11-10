# Review: Chant Coach Spec

**Spec:** docs/chant-coach-spec.md (draft of Nov 9, 2026)  
**Reviewer:** independent agent, Nov 9, 2026. Read-only; it rebuilt the scorer at word-part level to test the rules.  
**Answers:** Claude Code, Nov 9, 2026. Spot-checked before answering: R6 (18.45 carries 5.10's list), R7 (2.47 transliteration writes "saṅgo.astv"), R15 (gita_data.json says GPL-3.0, README and About say LGPL-3.0), R1 (trial ran on macOS 27.2; no Apple-published hi-IN list for iOS 26 found).

Result: 1 blocker, 8 major, 6 minor, 1 nit. All accepted. Two changed how the plan is staged (R1 and R14). The spec has been rewritten to match.

---

| ID | Severity | Finding (short) | Answer |
|----|----------|-----------------|--------|
| R1 | Blocker | Nobody has shown hi-IN works on an iPhone. It may be iOS 27 only. | **Accepted.** New step 0: on Nikhil's iPhone, list SpeechTranscriber locales and transcribe 4.7. The OS gate is set from that result. At runtime Learn shows only when `isAvailable` is true, hi-IN resolves through `supportedLocale(equivalentTo:)` and the model installs. **Resolved Nov 9:** on an iPhone 15 Pro with iOS 27.0.1, hi-IN is available and gave transcripts identical to the Mac's on 13 of 14 recordings (the 14th differed by one letter). Gate set to iOS 27 (spec D12). |
| R2 | Major | The pause rule flags learners for copying the reciter, who pauses at quarter-verse breaks inside lines. | **Accepted.** The pause check is out of version 1. |
| R3 | Major | The word-start "a" rule doesn't stop "dharmasya" passing for "adharmasya". A stricter rule misfires on correct chants. | **Accepted.** Claim removed. A dropped word-initial "a" is listed as a version 1 limit. |
| R4 | Major | At 0.6, real slips still pass ("sṛjāmi" at 0.65 and 0.64). Three takes can't tune a threshold. Section 6 misquoted what Apple heard. | **Accepted.** A labelled take set (several voices, many verses, correct and planted takes, half held back) is built before tuning. A transposition-aware cost is tried. Section 6 now quotes Apple's actual strings. |
| R5 | Major | Calibrating on the reference is circular, and it calibrates the wrong input (whole verse, Mac model). | **Accepted, with the reviewer's design.** Calibration runs on the phone: the reciter's lines go through the device's own model when a lesson opens, during the Listen step. The `checkable` field and the Mac re-runs are gone. Acceptance moves to learner-side targets. |
| R6 | Major | Some Sivananda lists are wrong (18.45 carries 5.10's list; 1.21 merges 1.22; 10.7 stops after two words). The planned checks can't catch it. Glosses are damaged ("eanimity"). | **Accepted.** Checks added: header verse number must match, a cap on alignment cost, at least one letter per part. A cleanup map fixes damaged glosses. Every flagged verse gets a hand review. |
| R7 | Major | The transliteration field isn't the chanted text (204 avagraha spots), and its lines don't always match the Devanagari (58 verses). | **Accepted.** Expected text and lines come from the Devanagari through a small converter that drops avagraha vowels. |
| R8 | Major | The half-second snap for line breaks is too narrow and can clip a syllable. | **Accepted.** Use the longest quiet stretch between the last word of a line and the first word of the next, at least 0.3 s. Listen to the outliers. |
| R9 | Major | Auto-stop after 1.5 s of silence cuts beginners off and reports "skipped". | **Accepted.** No auto-stop in From memory. Elsewhere, stop 1.5 s after the last expected word is heard, or after 4 s of silence. A cut-off take says "didn't finish", never "skipped". |
| R10 | Minor | Model can be removed by the system; iOS 26 needs AVAudioConverter; time ranges are per run of text; latency target at risk; add the speech usage key anyway; release analyzers; keys go in build settings. | **Accepted, all seven.** |
| R11 | Minor | Parts split inside Devanagari conjuncts, so marks can't sit on the verse line. | **Accepted.** Marks go on a row of parts under the line, each with its Devanagari and meaning. |
| R12 | Minor | Missing states: calls and alarms, AirPods changes, background, VoiceOver into the mic, iPad, unsupported devices. Bluetooth choice. "Not checked" needs explaining. | **Accepted.** All added to the spec. Bluetooth: keep A2DP playback and the phone's mic, then test. |
| R13 | Minor | Figures were per whole word, but scoring is per part: 96.9% of parts and 503 of 693 verses fully checkable. 17.8 is only 29% checkable. | **Accepted.** Figures re-quoted. The lesson shows "n of m words checked". |
| R14 | Minor | Cut for version 1: pause check, syllable tips, Learning list, marks on verse rows. Ship word-by-word meanings first as their own release. | **Accepted.** The plan now has two phases. Phase 1 ships word meanings on iOS 17+ and Android with no speech code. Phase 2 is Learn, trimmed as suggested. |
| R15 | Minor | License story is inconsistent: GPL-3.0 in the data and upstream, LGPL-3.0 in README and About. | **Accepted.** This predates the feature and touches public claims, so it's raised with Nikhil as its own item. chant_data.json will carry the same license as gita_data.json. **Nikhil, Nov 9: no change needed.** |
| R16 | Nit | 60 speaker-line verses, not 61. Fold list incomplete. Step 7 lacks a negative check. Step 8 lacks a done check. Step 6 should say "file through the streaming path". | **Accepted.** All fixed. |

---

## What the Reviewer Said to Keep

Scoring per word part, "Not checked" never shown as a mistake, free extra speech before and after, Next line always available with no streaks, marks with symbols as well as color, takes kept in memory only, Learn hidden on older iOS, ChantScorer as plain tested Swift, and chant_data.json as its own file. All kept.

## Reviewer's Working Files

Scripts used to test the rules: sim.py, run_all.py, rms.py and pauses.py, in the session scratchpad (temporary). The useful parts move into scripts/ with build step 2.
