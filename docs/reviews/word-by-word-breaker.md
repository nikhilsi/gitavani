# Breaker Pass: Word by Word (Phase 1)

**Scope:** commits b0a55c7 (data), 000317c (iOS), 080c688 (Android), Nov 9, 2026  
**Breaker:** independent agent, read-only. It checked 60+ verses by hand and ran scripts over all 693.  
**Answers:** Claude Code, Nov 9, 2026. Every data finding was confirmed in the source before fixing.

Result: no blockers. Line placement held up under every test (no part fits a neighbouring line better; no neighbouring-verse list slipped through; no off-by-one shifts; none of the 144 gloss fixes damaged a correct word). The findings below were mostly source typos that reached readers.

---

| ID | Severity | Finding (short) | Answer |
|----|----------|-----------------|--------|
| B1 | Major | Broken Devanagari in the source: अश्िवनौ (11.6, 11.22) and ुच्यते (18.23). The Roman forms shipped with a raw Devanagari sign. | **Fixed.** `fix_deva()` moves a vowel sign typed before its consonant and turns a leading vowel sign into its vowel. The validator now rejects non-IAST Roman forms and broken Devanagari. |
| B2 | Major | 8.12 showed "ट = and". | **Fixed.** Hand fix ट to च in chant_overrides.json. |
| B3 | Major | 17.24: ओम् missing from the source list, so "Om" was glued onto तस्मात्. | **Fixed.** Hand list for 17.24 with ओम् = Om. |
| B4 | Minor | Sivananda's spelling slips shown as the word: श्रृ for शृ (16 parts), ृ plus nukta for ॄ (3), and single words in 1.12, 2.1, 3.18, 4.4, 6.9, 13.28, 18.1. | **Fixed.** `fix_deva()` handles the two patterns. Single words are hand fixes. The suggested per-word "inside matches the line" check waits for Phase 2 spans. |
| B5 | Minor | Meaning typos ("of hi", "be Me", "them" for then, "Karana"), leftover "--", lost apostrophes, 13.9 leaving out death. | **Fixed.** Per-verse meaning fixes match the whole meaning on one word, so "them" elsewhere is untouched. "--" becomes a comma. Five apostrophe repairs join the global map. |
| B6 | Minor | Built differently from spec 5.1: speaker lines get no parts, words stack in a column, no span check. | **Spec changed to match the build.** Speaker lines have no source parts and are already shown above. Spans arrive with Phase 2. |
| B7 | Minor | At 28 pt the Roman form gets squeezed next to long Devanagari words, most on Android. | **Fixed.** Devanagari and Roman share a line when they fit and stack when they don't (ViewThatFits on iOS, FlowRow on Android, baseline aligned). Checked on iOS at 28 pt with 6.9. |
| B8 | Minor | Header toggle tap area under 44 pt / 48 dp. Android row missing button role and state. | **Fixed.** Minimum 44 pt and 48 dp. Android gets Role.Button and an expanded/collapsed state. |
| B9 | Nit | "(var अशेषाणि)" and "(orसंशयम्)" left in two verse lines. | **Fixed.** The alternate-reading pattern covers "var" and a missing space. |
| B10 | Nit | iOS dropped a decode error silently. Phase 2 will push the file past the 1 MB check. | **Fixed** (debug log). The 1 MB limit gets revisited in Phase 2. |
| B11 | Nit | Parchment's chevron contrast is 2.9:1. | **Not changed.** These are the app-wide theme colours, used the same way across every screen. |
| B12 | Nit | `drop_repeated_runs` was a general tool for one verse. | **Fixed.** Replaced by a hand list for 16.14. The DEBUG-only screenshot argument stays. |

Data after fixes: 693 of 701 verses, 8,840 parts (ओम् added), 959 KB.
