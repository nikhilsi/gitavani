# Now — Current Priorities

**Last Updated**: February 10, 2026

## Phase: Data Pipeline

### Goal
Fetch all Bhagavad Gita data from the Vedic Scriptures API and produce a clean, normalized JSON file to bundle with the iOS app.

### Tasks

1. **Run `scripts/fetch_gita_data.py`** — Fetches all 18 chapters and 700 verses
2. **Verify `data/gita_data.json`** — Check data integrity:
   - All 18 chapters present with summaries in Hindi and English
   - All 700 verses present
   - Each verse has Sanskrit slok, transliteration
   - Hindi translations extracted and normalized
   - English translations extracted and normalized
   - Commentaries extracted (for V2, but capture now)
3. **Spot check** — Manually verify a few verses against the live API

### Next Phase: Xcode Project Setup
After data pipeline is validated:
1. Create Xcode project (iOS App, SwiftUI, iOS 17+)
2. Set up folder structure per architecture doc
3. Copy gita_data.json into Resources/
4. Create Swift model structs
5. Create GitaDataService to load JSON
