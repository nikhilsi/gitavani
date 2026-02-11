#!/usr/bin/env python3
"""
GitaVani Data Pipeline
Fetches all Bhagavad Gita data from the Vedic Scriptures API
and normalizes it into a clean JSON file for bundling with the iOS app.

Source API: https://vedicscriptures.github.io
License: GPL-3.0

Usage:
    python fetch_gita_data.py

Output:
    ../data/gita_data.json
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = "https://vedicscriptures.github.io"
OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "data")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "gita_data.json")

# Delay between API calls to be respectful to the free API
REQUEST_DELAY_SECONDS = 0.3

# Author key mapping: API key -> (full name, available field types)
# Field types: ht=Hindi translation, et=English translation,
#              hc=Hindi commentary, ec=English commentary, sc=Sanskrit commentary
AUTHOR_MAP = {
    "tej":     {"name": "Swami Tejomayananda"},
    "siva":    {"name": "Swami Sivananda"},
    "purohit": {"name": "Shri Purohit Swami"},
    "chinmay": {"name": "Swami Chinmayananda"},
    "san":     {"name": "Dr. S. Sankaranarayan"},
    "adi":     {"name": "Swami Adidevananda"},
    "gambir":  {"name": "Swami Gambirananda"},
    "madhav":  {"name": "Sri Madhavacharya"},
    "anand":   {"name": "Sri Anandgiri"},
    "rams":    {"name": "Swami Ramsukhdas"},
    "raman":   {"name": "Sri Ramanuja"},
    "abhinav": {"name": "Sri Abhinav Gupta"},
    "sankar":  {"name": "Sri Shankaracharya"},
    "jaya":    {"name": "Sri Jayatritha"},
    "vallabh": {"name": "Sri Vallabhacharya"},
    "ms":      {"name": "Sri Madhusudan Saraswati"},
    "srid":    {"name": "Sri Sridhara Swami"},
    "dhan":    {"name": "Sri Dhanpati"},
    "venkat":  {"name": "Vedantadeshikacharya Venkatanatha"},
    "puru":    {"name": "Sri Purushottamji"},
    "neel":    {"name": "Sri Neelkanth"},
    "prabhu":  {"name": "A.C. Bhaktivedanta Swami Prabhupada"},
}


def fetch_json(url: str) -> dict | list:
    """Fetch JSON from a URL with basic error handling and retry."""
    max_retries = 3
    for attempt in range(max_retries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "GitaVani/1.0"})
            with urllib.request.urlopen(req, timeout=30) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            print(f"  HTTP Error {e.code} for {url}")
            if attempt < max_retries - 1:
                time.sleep(2)
            else:
                raise
        except urllib.error.URLError as e:
            print(f"  URL Error: {e.reason} for {url}")
            if attempt < max_retries - 1:
                time.sleep(2)
            else:
                raise
        except Exception as e:
            print(f"  Error: {e} for {url}")
            if attempt < max_retries - 1:
                time.sleep(2)
            else:
                raise


def extract_translations(verse_data: dict) -> list[dict]:
    """Extract and normalize translations from raw API verse data."""
    translations = []

    for key, author_info in AUTHOR_MAP.items():
        if key not in verse_data:
            continue

        author_data = verse_data[key]
        author_name = author_info["name"]

        # Hindi translation (ht field)
        if "ht" in author_data and author_data["ht"]:
            text = author_data["ht"].strip()
            if text and "did not comment" not in text.lower():
                translations.append({
                    "author": author_name,
                    "language": "hindi",
                    "text": text,
                })

        # English translation (et field)
        if "et" in author_data and author_data["et"]:
            text = author_data["et"].strip()
            if text and "did not comment" not in text.lower():
                translations.append({
                    "author": author_name,
                    "language": "english",
                    "text": text,
                })

    return translations


def extract_commentaries(verse_data: dict) -> list[dict]:
    """Extract and normalize commentaries from raw API verse data."""
    commentaries = []

    for key, author_info in AUTHOR_MAP.items():
        if key not in verse_data:
            continue

        author_data = verse_data[key]
        author_name = author_info["name"]

        # Hindi commentary (hc field)
        if "hc" in author_data and author_data["hc"]:
            text = author_data["hc"].strip()
            if text and "did not comment" not in text.lower():
                commentaries.append({
                    "author": author_name,
                    "language": "hindi",
                    "text": text,
                })

        # English commentary (ec field)
        if "ec" in author_data and author_data["ec"]:
            text = author_data["ec"].strip()
            if text and "did not comment" not in text.lower():
                commentaries.append({
                    "author": author_name,
                    "language": "english",
                    "text": text,
                })

        # Sanskrit commentary (sc field)
        if "sc" in author_data and author_data["sc"]:
            text = author_data["sc"].strip()
            if text and "did not comment" not in text.lower():
                commentaries.append({
                    "author": author_name,
                    "language": "sanskrit",
                    "text": text,
                })

    return commentaries


def fetch_chapters() -> list[dict]:
    """Fetch and return all chapter metadata."""
    print("Fetching chapters...")
    raw_chapters = fetch_json(f"{BASE_URL}/chapters")
    print(f"  Found {len(raw_chapters)} chapters")

    chapters = []
    for ch in raw_chapters:
        chapters.append({
            "chapter_number": ch["chapter_number"],
            "verses_count": ch["verses_count"],
            "name": ch.get("name", ""),
            "translation": ch.get("translation", ""),
            "transliteration": ch.get("transliteration", ""),
            "meaning": ch.get("meaning", {"en": "", "hi": ""}),
            "summary": ch.get("summary", {"en": "", "hi": ""}),
        })

    return chapters


def fetch_all_verses(chapters: list[dict]) -> list[dict]:
    """Fetch all verses for all chapters."""
    all_verses = []
    total_verses = sum(ch["verses_count"] for ch in chapters)
    fetched = 0

    for chapter in chapters:
        ch_num = chapter["chapter_number"]
        num_verses = chapter["verses_count"]
        print(f"\nChapter {ch_num} ({chapter.get('translation', '')}) — {num_verses} verses")

        for verse_num in range(1, num_verses + 1):
            url = f"{BASE_URL}/slok/{ch_num}/{verse_num}"
            try:
                raw = fetch_json(url)
            except Exception as e:
                print(f"  ❌ Failed to fetch {ch_num}.{verse_num}: {e}")
                continue

            verse = {
                "id": raw.get("_id", f"BG{ch_num}.{verse_num}"),
                "chapter": ch_num,
                "verse": verse_num,
                "slok": raw.get("slok", ""),
                "transliteration": raw.get("transliteration", ""),
                "translations": extract_translations(raw),
                "commentaries": extract_commentaries(raw),
            }

            all_verses.append(verse)
            fetched += 1

            # Progress indicator
            if verse_num % 10 == 0 or verse_num == num_verses:
                print(f"  {verse_num}/{num_verses} verses fetched ({fetched}/{total_verses} total)")

            time.sleep(REQUEST_DELAY_SECONDS)

    return all_verses


def validate_data(chapters: list[dict], verses: list[dict]) -> bool:
    """Run basic validation checks on the fetched data."""
    print("\n" + "=" * 60)
    print("VALIDATION")
    print("=" * 60)

    errors = []

    # Check chapter count
    if len(chapters) != 18:
        errors.append(f"Expected 18 chapters, got {len(chapters)}")

    # Check total verse count
    expected_total = sum(ch["verses_count"] for ch in chapters)
    if len(verses) != expected_total:
        errors.append(f"Expected {expected_total} verses, got {len(verses)}")

    # Check each chapter has the right number of verses
    for chapter in chapters:
        ch_num = chapter["chapter_number"]
        ch_verses = [v for v in verses if v["chapter"] == ch_num]
        expected = chapter["verses_count"]
        if len(ch_verses) != expected:
            errors.append(f"Chapter {ch_num}: expected {expected} verses, got {len(ch_verses)}")

    # Check verses have translations
    verses_without_translations = [v for v in verses if len(v["translations"]) == 0]
    if verses_without_translations:
        errors.append(f"{len(verses_without_translations)} verses have no translations")

    # Check verses have Sanskrit text
    verses_without_slok = [v for v in verses if not v["slok"]]
    if verses_without_slok:
        errors.append(f"{len(verses_without_slok)} verses have no Sanskrit text")

    # Stats
    total_translations = sum(len(v["translations"]) for v in verses)
    total_commentaries = sum(len(v["commentaries"]) for v in verses)
    hindi_translations = sum(1 for v in verses for t in v["translations"] if t["language"] == "hindi")
    english_translations = sum(1 for v in verses for t in v["translations"] if t["language"] == "english")

    print(f"\nChapters: {len(chapters)}")
    print(f"Verses: {len(verses)}")
    print(f"Total translations: {total_translations}")
    print(f"  Hindi: {hindi_translations}")
    print(f"  English: {english_translations}")
    print(f"Total commentaries: {total_commentaries}")
    print(f"Avg translations per verse: {total_translations / len(verses):.1f}")
    print(f"Avg commentaries per verse: {total_commentaries / len(verses):.1f}")

    # Unique authors
    translation_authors = set()
    for v in verses:
        for t in v["translations"]:
            translation_authors.add(f"{t['author']} ({t['language']})")
    print(f"\nUnique translator-language pairs: {len(translation_authors)}")
    for author in sorted(translation_authors):
        print(f"  - {author}")

    if errors:
        print(f"\n❌ VALIDATION FAILED — {len(errors)} errors:")
        for err in errors:
            print(f"  - {err}")
        return False
    else:
        print("\n✅ VALIDATION PASSED")
        return True


def main():
    print("=" * 60)
    print("GitaVani Data Pipeline")
    print("Fetching from: " + BASE_URL)
    print("=" * 60)

    # Fetch chapters
    chapters = fetch_chapters()

    # Fetch all verses
    verses = fetch_all_verses(chapters)

    # Validate
    is_valid = validate_data(chapters, verses)

    if not is_valid:
        print("\n⚠️  Data has issues. Review errors above.")
        print("    Saving anyway — you may want to investigate and re-run.")

    # Build final output
    output = {
        "metadata": {
            "source": "Vedic Scriptures Bhagavad Gita API",
            "source_url": BASE_URL,
            "license": "GPL-3.0",
            "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "total_chapters": len(chapters),
            "total_verses": len(verses),
        },
        "chapters": chapters,
        "verses": verses,
    }

    # Write output
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    file_size_mb = os.path.getsize(OUTPUT_FILE) / (1024 * 1024)
    print(f"\n📁 Output: {OUTPUT_FILE}")
    print(f"📏 Size: {file_size_mb:.1f} MB")
    print("\nDone! ✅")


if __name__ == "__main__":
    main()
