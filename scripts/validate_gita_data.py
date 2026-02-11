#!/usr/bin/env python3
"""
Comprehensive validation of gita_data.json for the GitaVani iOS app.
Checks structure, schema, content quality, author coverage, commentary,
Swift compatibility, and data size.
"""

import json
import sys
import os
import re
from collections import defaultdict, Counter

DATA_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "data", "gita_data.json")

# Traditional Bhagavad Gita verse counts per chapter
EXPECTED_VERSE_COUNTS = {
    1: 47, 2: 72, 3: 43, 4: 42, 5: 29, 6: 47,
    7: 30, 8: 28, 9: 34, 10: 42, 11: 55, 12: 20,
    13: 35, 14: 27, 15: 20, 16: 24, 17: 28, 18: 78,
}
EXPECTED_TOTAL_VERSES = sum(EXPECTED_VERSE_COUNTS.values())  # 701

# Tracking results
results = {}
all_issues = []


def section(name):
    print(f"\n{'='*80}")
    print(f"  {name}")
    print(f"{'='*80}")


def pass_fail(section_name, passed, issues=None):
    status = "PASS" if passed else "FAIL"
    marker = "[PASS]" if passed else "[FAIL]"
    results[section_name] = passed
    print(f"\n  {marker} {section_name}")
    if issues:
        for issue in issues:
            print(f"    - {issue}")
            all_issues.append(f"[{section_name}] {issue}")


def main():
    print("=" * 80)
    print("  GITAVANI DATA VALIDATION")
    print(f"  File: {DATA_FILE}")
    print("=" * 80)

    # Load JSON
    try:
        with open(DATA_FILE, "r", encoding="utf-8") as f:
            raw = f.read()
        data = json.loads(raw)
        print(f"\n  JSON loaded successfully. File size: {len(raw):,} bytes ({len(raw)/1024/1024:.2f} MB)")
    except Exception as e:
        print(f"\n  FATAL: Could not load JSON: {e}")
        sys.exit(1)

    # =========================================================================
    # 1. STRUCTURE & SCHEMA
    # =========================================================================
    section("1. STRUCTURE & SCHEMA")
    issues = []

    required_top_keys = {"metadata", "chapters", "verses"}
    actual_top_keys = set(data.keys())
    if not required_top_keys.issubset(actual_top_keys):
        missing = required_top_keys - actual_top_keys
        issues.append(f"Missing top-level keys: {missing}")
    extra_top = actual_top_keys - required_top_keys
    if extra_top:
        print(f"  Note: Extra top-level keys: {extra_top}")

    metadata = data.get("metadata", {})
    required_meta = {"source", "license", "total_chapters", "total_verses"}
    actual_meta = set(metadata.keys())
    missing_meta = required_meta - actual_meta
    if missing_meta:
        issues.append(f"Missing metadata keys: {missing_meta}")
    else:
        print(f"  metadata.source: {metadata.get('source', 'N/A')}")
        print(f"  metadata.license: {metadata.get('license', 'N/A')}")
        print(f"  metadata.total_chapters: {metadata.get('total_chapters', 'N/A')}")
        print(f"  metadata.total_verses: {metadata.get('total_verses', 'N/A')}")

    chapters = data.get("chapters", [])
    if not isinstance(chapters, list):
        issues.append(f"'chapters' is not a list, got {type(chapters).__name__}")
    elif len(chapters) != 18:
        issues.append(f"Expected 18 chapters, got {len(chapters)}")
    else:
        print(f"  chapters: list of {len(chapters)} items")

    verses = data.get("verses", [])
    if not isinstance(verses, list):
        issues.append(f"'verses' is not a list, got {type(verses).__name__}")
    else:
        print(f"  verses: list of {len(verses)} items")

    pass_fail("Structure & Schema", len(issues) == 0, issues)

    # =========================================================================
    # 2. CHAPTER METADATA COMPLETENESS
    # =========================================================================
    section("2. CHAPTER METADATA COMPLETENESS")
    issues = []

    chapter_numbers_found = []
    print(f"\n  {'Ch#':<5} {'Name':<25} {'Translation':<40} {'Verses':<8}")
    print(f"  {'-'*5} {'-'*25} {'-'*40} {'-'*8}")

    for i, ch in enumerate(chapters):
        ch_num = ch.get("chapter_number")
        chapter_numbers_found.append(ch_num)

        # Required fields
        if not isinstance(ch_num, int):
            issues.append(f"Chapter index {i}: chapter_number is not int ({type(ch_num).__name__}: {ch_num})")

        vc = ch.get("verses_count")
        if not isinstance(vc, int):
            issues.append(f"Chapter {ch_num}: verses_count is not int")

        name = ch.get("name", "")
        if not name or not isinstance(name, str) or not name.strip():
            issues.append(f"Chapter {ch_num}: 'name' is empty or missing")

        translation = ch.get("translation", "")
        if not translation or not isinstance(translation, str) or not translation.strip():
            issues.append(f"Chapter {ch_num}: 'translation' is empty or missing")

        translit = ch.get("transliteration", "")
        if not translit or not isinstance(translit, str) or not translit.strip():
            issues.append(f"Chapter {ch_num}: 'transliteration' is empty or missing")

        # meaning with en and hi
        meaning = ch.get("meaning", {})
        if not isinstance(meaning, dict):
            issues.append(f"Chapter {ch_num}: 'meaning' is not a dict")
        else:
            for lang in ["en", "hi"]:
                val = meaning.get(lang, "")
                if not val or not isinstance(val, str) or not val.strip():
                    issues.append(f"Chapter {ch_num}: meaning.{lang} is empty or missing")

        # summary with en and hi
        summary = ch.get("summary", {})
        if not isinstance(summary, dict):
            issues.append(f"Chapter {ch_num}: 'summary' is not a dict")
        else:
            for lang in ["en", "hi"]:
                val = summary.get(lang, "")
                if not val or not isinstance(val, str) or not val.strip():
                    issues.append(f"Chapter {ch_num}: summary.{lang} is empty or missing")

        # Print row
        name_display = (name[:22] + "...") if len(str(name)) > 25 else str(name)
        trans_display = (str(translation)[:37] + "...") if len(str(translation)) > 40 else str(translation)
        print(f"  {ch_num:<5} {name_display:<25} {trans_display:<40} {vc:<8}")

    # Check sequential 1-18
    expected_seq = list(range(1, 19))
    if chapter_numbers_found != expected_seq:
        issues.append(f"Chapter numbers not sequential 1-18. Got: {chapter_numbers_found}")

    pass_fail("Chapter Metadata Completeness", len(issues) == 0, issues)

    # =========================================================================
    # 3. VERSE COUNT & CONTINUITY
    # =========================================================================
    section("3. VERSE COUNT & CONTINUITY")
    issues = []

    # Build per-chapter verse lists
    verses_by_chapter = defaultdict(list)
    for v in verses:
        ch = v.get("chapter")
        vn = v.get("verse")
        verses_by_chapter[ch].append(vn)

    # Sum of chapter verse counts
    chapter_verse_sum = sum(ch.get("verses_count", 0) for ch in chapters)
    actual_total = len(verses)
    print(f"  Total verses in file: {actual_total}")
    print(f"  Sum of chapter verses_count: {chapter_verse_sum}")
    print(f"  Expected (traditional): {EXPECTED_TOTAL_VERSES}")
    print(f"  metadata.total_verses: {metadata.get('total_verses', 'N/A')}")

    if actual_total != chapter_verse_sum:
        issues.append(f"Verse count mismatch: {actual_total} verses in list vs {chapter_verse_sum} from chapter sums")

    if metadata.get("total_verses") != actual_total:
        issues.append(f"metadata.total_verses ({metadata.get('total_verses')}) != actual count ({actual_total})")

    if actual_total != EXPECTED_TOTAL_VERSES:
        issues.append(f"Total verse count {actual_total} != expected traditional count {EXPECTED_TOTAL_VERSES}")

    # Per-chapter checks
    print(f"\n  {'Ch#':<5} {'Expected':<10} {'Actual':<10} {'Sequential':<12} {'Status'}")
    print(f"  {'-'*5} {'-'*10} {'-'*10} {'-'*12} {'-'*8}")

    for ch_num in range(1, 19):
        expected_count = EXPECTED_VERSE_COUNTS[ch_num]
        actual_verses = sorted(verses_by_chapter.get(ch_num, []))
        actual_count = len(actual_verses)

        # Check sequential
        expected_seq = list(range(1, expected_count + 1))
        is_sequential = actual_verses == expected_seq

        # Check chapter metadata matches
        ch_meta = next((c for c in chapters if c.get("chapter_number") == ch_num), None)
        ch_meta_count = ch_meta.get("verses_count", 0) if ch_meta else 0

        status = "OK"
        if actual_count != expected_count:
            status = "COUNT MISMATCH"
            issues.append(f"Ch{ch_num}: expected {expected_count} verses, got {actual_count}")
        if ch_meta_count != actual_count:
            issues.append(f"Ch{ch_num}: chapter metadata says {ch_meta_count} but found {actual_count}")
        if not is_sequential:
            status = "GAP/DUP"
            # Find gaps and duplicates
            counter = Counter(verses_by_chapter.get(ch_num, []))
            dups = [v for v, c in counter.items() if c > 1]
            all_expected = set(range(1, expected_count + 1))
            missing = all_expected - set(actual_verses)
            extra = set(actual_verses) - all_expected
            if dups:
                issues.append(f"Ch{ch_num}: duplicate verse numbers: {dups}")
            if missing:
                issues.append(f"Ch{ch_num}: missing verse numbers: {sorted(missing)}")
            if extra:
                issues.append(f"Ch{ch_num}: extra verse numbers: {sorted(extra)}")

        print(f"  {ch_num:<5} {expected_count:<10} {actual_count:<10} {str(is_sequential):<12} {status}")

    # Verify all verse IDs match pattern BG{chapter}.{verse}
    bad_ids = []
    id_pattern = re.compile(r"^BG(\d+)\.(\d+)$")
    for v in verses:
        vid = v.get("id", "")
        match = id_pattern.match(str(vid))
        if not match:
            bad_ids.append(vid)
        else:
            ch_from_id = int(match.group(1))
            vn_from_id = int(match.group(2))
            if ch_from_id != v.get("chapter") or vn_from_id != v.get("verse"):
                issues.append(f"ID mismatch: {vid} has chapter={v.get('chapter')}, verse={v.get('verse')}")

    if bad_ids:
        issues.append(f"Verse IDs not matching BG{{ch}}.{{v}} pattern: {bad_ids[:10]}{'...' if len(bad_ids) > 10 else ''}")
        print(f"\n  Bad IDs found: {len(bad_ids)}")

    pass_fail("Verse Count & Continuity", len(issues) == 0, issues)

    # =========================================================================
    # 4. VERSE CONTENT QUALITY
    # =========================================================================
    section("4. VERSE CONTENT QUALITY")
    issues = []

    html_pattern = re.compile(r"<[a-zA-Z/][^>]*>")
    replacement_char = "\ufffd"
    placeholder_values = {"did not comment", "null", "undefined", "n/a", "none", "no commentary", "not available"}

    empty_slok = []
    empty_translit = []
    no_hindi_trans = []
    no_english_trans = []
    short_translations = []
    html_in_text = []
    encoding_issues = []
    placeholder_texts = []
    whitespace_only = []
    low_translation_count = []

    for v in verses:
        vid = v.get("id", f"Ch{v.get('chapter')}V{v.get('verse')}")

        # Non-empty slok
        slok = v.get("slok", "")
        if not slok or not slok.strip():
            empty_slok.append(vid)

        # Non-empty transliteration
        translit = v.get("transliteration", "")
        if not translit or not translit.strip():
            empty_translit.append(vid)

        # Translations
        translations = v.get("translations", [])
        has_hindi = False
        has_english = False

        for t in translations:
            lang = t.get("language", "")
            text = t.get("text", "")
            author = t.get("author", "unknown")

            if lang == "hindi":
                has_hindi = True
            if lang == "english":
                has_english = True

            # Whitespace only
            if text and not text.strip():
                whitespace_only.append(f"{vid} ({author}/{lang})")

            # Short translations
            if text and len(text.strip()) < 10 and text.strip():
                short_translations.append(f"{vid} ({author}/{lang}): '{text.strip()}'")

            # HTML tags
            if html_pattern.search(text or ""):
                html_in_text.append(f"{vid} translation ({author}/{lang})")

            # Placeholder text
            if text and text.strip().lower() in placeholder_values:
                placeholder_texts.append(f"{vid} ({author}/{lang}): '{text.strip()}'")

            # Encoding issues
            if replacement_char in (text or ""):
                encoding_issues.append(f"{vid} ({author}/{lang}): contains U+FFFD replacement character")

        if not has_hindi:
            no_hindi_trans.append(vid)
        if not has_english:
            no_english_trans.append(vid)
        if len(translations) < 5:
            low_translation_count.append(f"{vid}: only {len(translations)} translations")

        # Check slok and transliteration for HTML / encoding
        for field_name, field_val in [("slok", slok), ("transliteration", translit)]:
            if html_pattern.search(field_val or ""):
                html_in_text.append(f"{vid} {field_name}")
            if replacement_char in (field_val or ""):
                encoding_issues.append(f"{vid} {field_name}: contains U+FFFD")

        # Check commentaries for HTML / encoding / placeholders
        commentaries = v.get("commentaries", [])
        for c in commentaries:
            text = c.get("text", "")
            author = c.get("author", "unknown")
            lang = c.get("language", "")
            if html_pattern.search(text or ""):
                html_in_text.append(f"{vid} commentary ({author}/{lang})")
            if replacement_char in (text or ""):
                encoding_issues.append(f"{vid} commentary ({author}/{lang}): U+FFFD")
            if text and text.strip().lower() in placeholder_values:
                placeholder_texts.append(f"{vid} commentary ({author}/{lang}): '{text.strip()}'")

    # Report
    if empty_slok:
        issues.append(f"Verses with empty 'slok': {empty_slok}")
    else:
        print(f"  All {len(verses)} verses have non-empty 'slok' (Sanskrit)")

    if empty_translit:
        issues.append(f"Verses with empty 'transliteration': {empty_translit}")
    else:
        print(f"  All {len(verses)} verses have non-empty 'transliteration'")

    if no_hindi_trans:
        issues.append(f"Verses missing Hindi translation ({len(no_hindi_trans)}): {no_hindi_trans[:10]}...")
    else:
        print(f"  All verses have at least 1 Hindi translation")

    if no_english_trans:
        issues.append(f"Verses missing English translation ({len(no_english_trans)}): {no_english_trans[:10]}...")
    else:
        print(f"  All verses have at least 1 English translation")

    if short_translations:
        print(f"\n  WARNING: {len(short_translations)} suspiciously short translations (<10 chars):")
        for s in short_translations[:20]:
            print(f"    {s}")
        if len(short_translations) > 20:
            print(f"    ... and {len(short_translations) - 20} more")
        issues.append(f"{len(short_translations)} translations shorter than 10 characters")

    if html_in_text:
        print(f"\n  WARNING: {len(html_in_text)} fields contain HTML tags:")
        for h in html_in_text[:20]:
            print(f"    {h}")
        if len(html_in_text) > 20:
            print(f"    ... and {len(html_in_text) - 20} more")
        issues.append(f"{len(html_in_text)} fields contain HTML tags")

    if encoding_issues:
        print(f"\n  WARNING: {len(encoding_issues)} encoding issues found:")
        for e in encoding_issues[:20]:
            print(f"    {e}")
        issues.append(f"{len(encoding_issues)} encoding issues")

    if placeholder_texts:
        print(f"\n  WARNING: {len(placeholder_texts)} placeholder/null texts found:")
        for p in placeholder_texts[:20]:
            print(f"    {p}")
        issues.append(f"{len(placeholder_texts)} placeholder texts found")

    if whitespace_only:
        print(f"\n  WARNING: {len(whitespace_only)} whitespace-only translations:")
        for w in whitespace_only[:10]:
            print(f"    {w}")
        issues.append(f"{len(whitespace_only)} whitespace-only translations")

    if low_translation_count:
        print(f"\n  WARNING: {len(low_translation_count)} verses with fewer than 5 translations:")
        for l in low_translation_count[:10]:
            print(f"    {l}")
        if len(low_translation_count) > 10:
            print(f"    ... and {len(low_translation_count) - 10} more")
        issues.append(f"{len(low_translation_count)} verses with fewer than 5 translations")

    if not html_in_text:
        print(f"  No HTML tags found in any text fields")
    if not encoding_issues:
        print(f"  No encoding issues detected")
    if not placeholder_texts:
        print(f"  No placeholder/null texts detected")
    if not whitespace_only:
        print(f"  No whitespace-only translations")

    pass_fail("Verse Content Quality", len(issues) == 0, issues)

    # =========================================================================
    # 5. AUTHOR COVERAGE
    # =========================================================================
    section("5. AUTHOR COVERAGE")
    issues = []

    author_lang_coverage = defaultdict(set)  # (author, lang) -> set of verse IDs
    all_authors = set()

    for v in verses:
        vid = v.get("id", "")
        for t in v.get("translations", []):
            author = t.get("author", "unknown")
            lang = t.get("language", "unknown")
            all_authors.add(author)
            author_lang_coverage[(author, lang)].add(vid)

    total = len(verses)
    print(f"\n  Unique translation authors: {len(all_authors)}")
    print(f"\n  {'Author':<35} {'Language':<10} {'Count':<8} {'Coverage':<10}")
    print(f"  {'-'*35} {'-'*10} {'-'*8} {'-'*10}")

    low_coverage_authors = []
    for (author, lang), verse_ids in sorted(author_lang_coverage.items(), key=lambda x: (-len(x[1]), x[0])):
        count = len(verse_ids)
        pct = count / total * 100
        marker = "" if pct >= 50 else " LOW"
        print(f"  {author:<35} {lang:<10} {count:<8} {pct:>6.1f}%{marker}")
        if pct < 50:
            low_coverage_authors.append(f"{author}/{lang}: {count}/{total} ({pct:.1f}%)")

    if low_coverage_authors:
        print(f"\n  Authors with <50% coverage:")
        for a in low_coverage_authors:
            print(f"    {a}")
        print(f"\n  Note: Low coverage may be expected for some translators.")

    # Check for potential author name variants (simple heuristic)
    author_list = sorted(all_authors)
    print(f"\n  All unique author names ({len(author_list)}):")
    for a in author_list:
        print(f"    - {a}")

    # Simple similarity check: lowercase comparison
    lower_map = defaultdict(list)
    for a in author_list:
        lower_map[a.lower().strip()].append(a)
    variants = {k: v for k, v in lower_map.items() if len(v) > 1}
    if variants:
        issues.append(f"Potential author name variants: {variants}")
        print(f"\n  WARNING: Potential duplicate author names:")
        for k, v in variants.items():
            print(f"    {v}")

    pass_fail("Author Coverage", len(issues) == 0, issues)

    # =========================================================================
    # 6. COMMENTARY QUALITY
    # =========================================================================
    section("6. COMMENTARY QUALITY")
    issues = []

    commentary_counts = []
    zero_commentary_verses = []
    commentary_author_lang = defaultdict(set)  # (author, lang) -> set of verse IDs

    for v in verses:
        vid = v.get("id", "")
        comms = v.get("commentaries", [])
        commentary_counts.append(len(comms))

        if len(comms) == 0:
            zero_commentary_verses.append(vid)

        for c in comms:
            author = c.get("author", "unknown")
            lang = c.get("language", "unknown")
            commentary_author_lang[(author, lang)].add(vid)

    total_comms = sum(commentary_counts)
    verses_with_comms = sum(1 for c in commentary_counts if c > 0)
    avg_comms = total_comms / len(verses) if verses else 0

    print(f"\n  Verses with commentaries: {verses_with_comms}/{len(verses)} ({verses_with_comms/len(verses)*100:.1f}%)")
    print(f"  Total commentaries: {total_comms}")
    print(f"  Average commentaries per verse: {avg_comms:.1f}")
    print(f"  Max commentaries on a verse: {max(commentary_counts) if commentary_counts else 0}")
    print(f"  Min commentaries on a verse: {min(commentary_counts) if commentary_counts else 0}")

    if zero_commentary_verses:
        print(f"\n  Verses with zero commentaries: {len(zero_commentary_verses)}")
        if len(zero_commentary_verses) <= 20:
            for vid in zero_commentary_verses:
                print(f"    {vid}")
        else:
            for vid in zero_commentary_verses[:10]:
                print(f"    {vid}")
            print(f"    ... and {len(zero_commentary_verses) - 10} more")

    print(f"\n  Commentary author/language coverage:")
    print(f"  {'Author':<35} {'Language':<10} {'Count':<8} {'Coverage':<10}")
    print(f"  {'-'*35} {'-'*10} {'-'*8} {'-'*10}")

    for (author, lang), verse_ids in sorted(commentary_author_lang.items(), key=lambda x: (-len(x[1]), x[0])):
        count = len(verse_ids)
        pct = count / total * 100
        print(f"  {author:<35} {lang:<10} {count:<8} {pct:>6.1f}%")

    pass_fail("Commentary Quality", True, None)  # Informational section

    # =========================================================================
    # 7. SWIFT COMPATIBILITY
    # =========================================================================
    section("7. SWIFT COMPATIBILITY")
    issues = []

    # Check field name conventions
    def collect_keys(obj, path=""):
        """Recursively collect all keys in the JSON structure."""
        keys = set()
        if isinstance(obj, dict):
            for k, v in obj.items():
                full = f"{path}.{k}" if path else k
                keys.add(full)
                keys.update(collect_keys(v, full))
        elif isinstance(obj, list) and obj:
            # Sample first item
            keys.update(collect_keys(obj[0], f"{path}[]"))
        return keys

    all_keys = collect_keys(data)
    # Check for camelCase keys (which would be non-snake_case)
    camel_case_pattern = re.compile(r"[a-z][A-Z]")
    non_snake = []
    for k in sorted(all_keys):
        # Get just the leaf key
        leaf = k.split(".")[-1].replace("[]", "")
        if camel_case_pattern.search(leaf):
            non_snake.append(f"{k} (leaf: {leaf})")

    if non_snake:
        print(f"  Note: Keys with camelCase (may need CodingKeys in Swift):")
        for k in non_snake[:20]:
            print(f"    {k}")
    else:
        print(f"  All field names appear to be snake_case compatible")

    # Check null values in required fields
    required_verse_fields = ["id", "chapter", "verse", "slok", "transliteration"]
    null_required = []
    for v in verses:
        vid = v.get("id", "UNKNOWN")
        for field in required_verse_fields:
            val = v.get(field)
            if val is None:
                null_required.append(f"{vid}: {field} is null")

    if null_required:
        issues.append(f"Null values in required fields: {null_required[:10]}")
        for n in null_required[:10]:
            print(f"    {n}")
    else:
        print(f"  No null values in required verse fields ({', '.join(required_verse_fields)})")

    # Check language values
    valid_languages = {"hindi", "english", "sanskrit"}
    bad_languages = set()
    for v in verses:
        vid = v.get("id", "")
        for t in v.get("translations", []):
            lang = t.get("language", "")
            if lang not in valid_languages:
                bad_languages.add(f"{vid}: translation language='{lang}'")
        for c in v.get("commentaries", []):
            lang = c.get("language", "")
            if lang not in valid_languages:
                bad_languages.add(f"{vid}: commentary language='{lang}'")

    if bad_languages:
        issues.append(f"Invalid language values found ({len(bad_languages)}):")
        for b in sorted(bad_languages)[:20]:
            print(f"    {b}")
            issues.append(f"  {b}")
        if len(bad_languages) > 20:
            print(f"    ... and {len(bad_languages) - 20} more")
    else:
        print(f"  All language values are in {valid_languages}")

    # Check types
    type_issues = []
    for v in verses:
        vid = v.get("id", "UNKNOWN")
        if not isinstance(v.get("chapter"), int):
            type_issues.append(f"{vid}: chapter is {type(v.get('chapter')).__name__}, expected int")
        if not isinstance(v.get("verse"), int):
            type_issues.append(f"{vid}: verse is {type(v.get('verse')).__name__}, expected int")
        if not isinstance(v.get("id"), str):
            type_issues.append(f"{vid}: id is {type(v.get('id')).__name__}, expected str")

    if type_issues:
        issues.append(f"Type issues: {type_issues[:10]}")
    else:
        print(f"  All verse.chapter (int), verse.verse (int), verse.id (str) types correct")

    pass_fail("Swift Compatibility", len(issues) == 0, issues)

    # =========================================================================
    # 8. DATA SIZE ANALYSIS
    # =========================================================================
    section("8. DATA SIZE ANALYSIS")

    file_size = os.path.getsize(DATA_FILE)
    print(f"\n  Total file size: {file_size:,} bytes ({file_size/1024/1024:.2f} MB)")

    # Verse sizes
    verse_sizes = []
    translation_total_size = 0
    commentary_total_size = 0

    for v in verses:
        verse_json = json.dumps(v, ensure_ascii=False)
        verse_sizes.append((v.get("id", "?"), len(verse_json)))

        # Translation size
        for t in v.get("translations", []):
            translation_total_size += len(json.dumps(t, ensure_ascii=False))

        # Commentary size
        for c in v.get("commentaries", []):
            commentary_total_size += len(json.dumps(c, ensure_ascii=False))

    avg_size = sum(s for _, s in verse_sizes) / len(verse_sizes) if verse_sizes else 0
    largest = max(verse_sizes, key=lambda x: x[1]) if verse_sizes else ("?", 0)
    smallest = min(verse_sizes, key=lambda x: x[1]) if verse_sizes else ("?", 0)

    print(f"  Average verse size: {avg_size:,.0f} bytes ({avg_size/1024:.1f} KB)")
    print(f"  Largest verse: {largest[0]} at {largest[1]:,} bytes ({largest[1]/1024:.1f} KB)")
    print(f"  Smallest verse: {smallest[0]} at {smallest[1]:,} bytes ({smallest[1]/1024:.1f} KB)")
    print(f"  Total translations size: {translation_total_size:,} bytes ({translation_total_size/1024/1024:.2f} MB)")
    print(f"  Total commentaries size: {commentary_total_size:,} bytes ({commentary_total_size/1024/1024:.2f} MB)")
    print(f"  Translations % of total: {translation_total_size/file_size*100:.1f}%")
    print(f"  Commentaries % of total: {commentary_total_size/file_size*100:.1f}%")

    # Top 5 largest verses
    verse_sizes_sorted = sorted(verse_sizes, key=lambda x: -x[1])
    print(f"\n  Top 5 largest verses:")
    for vid, size in verse_sizes_sorted[:5]:
        print(f"    {vid}: {size:,} bytes ({size/1024:.1f} KB)")

    print(f"\n  Top 5 smallest verses:")
    verse_sizes_sorted_asc = sorted(verse_sizes, key=lambda x: x[1])
    for vid, size in verse_sizes_sorted_asc[:5]:
        print(f"    {vid}: {size:,} bytes ({size/1024:.1f} KB)")

    pass_fail("Data Size Analysis", True, None)  # Informational

    # =========================================================================
    # FINAL SUMMARY
    # =========================================================================
    print(f"\n{'='*80}")
    print(f"  FINAL SUMMARY")
    print(f"{'='*80}")

    total_checks = len(results)
    passed = sum(1 for v in results.values() if v)
    failed = total_checks - passed

    for name, passed_flag in results.items():
        status = "[PASS]" if passed_flag else "[FAIL]"
        print(f"  {status} {name}")

    print(f"\n  Total: {total_checks} sections | {passed} passed | {failed} failed")

    if all_issues:
        print(f"\n  All issues ({len(all_issues)}):")
        for issue in all_issues:
            print(f"    - {issue}")

    if failed == 0:
        print(f"\n  RESULT: ALL CHECKS PASSED")
    else:
        print(f"\n  RESULT: {failed} SECTION(S) FAILED - review issues above")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
