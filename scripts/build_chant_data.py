#!/usr/bin/env python3
"""
Build data/chant_data.json: word-by-word parts and meanings for every verse.

Source: the word list that opens Swami Sivananda's English commentary in
data/gita_data.json, for example
    "2.47 कर्मणि in work? एव only? अधिकारः right? ..."
The "?" separators were commas in the original text.

For each verse the script:
  1. splits the Devanagari verse into lines (speaker lines such as
     "श्रीभगवानुवाच" are kept but get no parts; Sivananda's lists skip them),
  2. parses the word list into parts (Devanagari, Roman form, meaning),
  3. lines the parts up with the chanted text, so each part lands on its line,
  4. runs checks. A verse that fails any check is marked unavailable, with
     the reason, so a wrong meaning never reaches a reader.

Overrides (hand review) live in scripts/data/chant_overrides.json.
Gloss repairs (damaged words such as "eanimity") live in
scripts/data/sivananda_gloss_fixes.json.

Usage:
    python3 scripts/build_chant_data.py            # build, report, copy to apps
    python3 scripts/build_chant_data.py --report   # build and report, no copy
    python3 scripts/build_chant_data.py --suggest-gloss-fixes
"""

import json
import os
import re
import shutil
import sys
import unicodedata

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
SRC = os.path.join(ROOT, "data", "gita_data.json")
OUT = os.path.join(ROOT, "data", "chant_data.json")
OVERRIDES = os.path.join(ROOT, "scripts", "data", "chant_overrides.json")
GLOSS_FIXES = os.path.join(ROOT, "scripts", "data", "sivananda_gloss_fixes.json")
APP_COPIES = [
    os.path.join(ROOT, "ios", "GitaVani", "GitaVani", "Resources", "chant_data.json"),
    os.path.join(ROOT, "android", "GitaVani", "app", "src", "main", "assets", "chant_data.json"),
]

# Check limits. A verse must pass all of them to be shown.
MIN_VERSE_COVERAGE = 0.85   # share of the verse's letters matched by parts
MIN_LIST_COVERAGE = 0.85    # share of the list's letters matched in the verse
MAX_COST_RATIO = 0.30       # alignment cost per verse letter

# ---------------------------------------------------------------------------
# Devanagari to IAST
# ---------------------------------------------------------------------------

VOWELS = {"अ": "a", "आ": "ā", "इ": "i", "ई": "ī", "उ": "u", "ऊ": "ū", "ऋ": "ṛ", "ॠ": "ṝ",
          "ऌ": "ḷ", "ए": "e", "ऐ": "ai", "ओ": "o", "औ": "au"}
SIGNS = {"ा": "ā", "ि": "i", "ी": "ī", "ु": "u", "ू": "ū", "ृ": "ṛ", "ॄ": "ṝ", "ॢ": "ḷ",
         "े": "e", "ै": "ai", "ो": "o", "ौ": "au"}
CONSONANTS = {"क": "k", "ख": "kh", "ग": "g", "घ": "gh", "ङ": "ṅ", "च": "c", "छ": "ch", "ज": "j",
              "झ": "jh", "ञ": "ñ", "ट": "ṭ", "ठ": "ṭh", "ड": "ḍ", "ढ": "ḍh", "ण": "ṇ", "त": "t",
              "थ": "th", "द": "d", "ध": "dh", "न": "n", "प": "p", "फ": "ph", "ब": "b", "भ": "bh",
              "म": "m", "य": "y", "र": "r", "ल": "l", "व": "v", "श": "ś", "ष": "ṣ", "स": "s",
              "ह": "h", "ळ": "ḷ"}
VIRAMA, ANUSVARA, CHANDRABINDU, VISARGA, AVAGRAHA, NUKTA = "्", "ं", "ँ", "ः", "ऽ", "़"


def deva_to_iast(text):
    """Convert Devanagari to IAST. The avagraha becomes an apostrophe."""
    out, i, n = [], 0, len(text)
    while i < n:
        ch = text[i]
        if ch in CONSONANTS:
            base = CONSONANTS[ch]
            i += 1
            if i < n and text[i] == NUKTA:
                i += 1
            if i < n and text[i] in SIGNS:
                out.append(base + SIGNS[text[i]])
                i += 1
            elif i < n and text[i] == VIRAMA:
                out.append(base)
                i += 1
            else:
                out.append(base + "a")
            continue
        if ch in VOWELS:
            out.append(VOWELS[ch])
        elif ch in (ANUSVARA, CHANDRABINDU):
            out.append("ṃ")
        elif ch == VISARGA:
            out.append("ḥ")
        elif ch == AVAGRAHA:
            out.append("'")
        elif ch == "ॐ":
            out.append("oṃ")
        elif ch in (NUKTA, "‌", "‍"):
            pass
        else:
            out.append(ch)
        i += 1
    return "".join(out)


# ---------------------------------------------------------------------------
# Sound key and alignment (same folding as the Learn scorer in the spec, 7.3)
# ---------------------------------------------------------------------------

IAST_FOLD = [("ṛ", "ri"), ("ṝ", "ri"), ("ḷ", "li"), ("ṃ", "m"), ("ḥ", "h"), ("ṅ", "n"), ("ñ", "n"),
             ("ṇ", "n"), ("ṭ", "t"), ("ḍ", "d"), ("ś", "sh"), ("ṣ", "sh"), ("ā", "a"), ("ī", "i"),
             ("ū", "u")]
SOUND_FOLD = [("ksh", "ks"), ("sh", "s"), ("th", "t"), ("dh", "d"), ("bh", "b"), ("ph", "p"),
              ("kh", "k"), ("gh", "g"), ("jh", "j"), ("ch", "c"), ("ri", "r"), ("ru", "r")]


def sound_key(iast):
    s = iast.lower()
    for a, b in IAST_FOLD:
        s = s.replace(a, b)
    s = "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))
    s = re.sub(r"[^a-z]", "", s)
    for a, b in SOUND_FOLD:
        s = s.replace(a, b)
    return re.sub(r"(.)\1+", r"\1", s)


def _sub(a, b):
    if a == b:
        return 0.0
    return 0.5 if a in "aeiou" and b in "aeiou" else 1.0


def _indel(c):
    return 0.3 if c in "ahmy" else 1.0   # sandhi often adds or drops these


def align(exp, got):
    """Weighted edit distance. Returns (cost, pairs) with pairs of (exp_index or None, got_index or None)."""
    n, m = len(exp), len(got)
    D = [[0.0] * (m + 1) for _ in range(n + 1)]
    for i in range(1, n + 1):
        D[i][0] = D[i - 1][0] + _indel(exp[i - 1])
    for j in range(1, m + 1):
        D[0][j] = D[0][j - 1] + _indel(got[j - 1])
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            D[i][j] = min(D[i - 1][j - 1] + _sub(exp[i - 1], got[j - 1]),
                          D[i - 1][j] + _indel(exp[i - 1]),
                          D[i][j - 1] + _indel(got[j - 1]))
    pairs, i, j = [], n, m
    while i > 0 or j > 0:
        if i > 0 and j > 0 and abs(D[i][j] - (D[i - 1][j - 1] + _sub(exp[i - 1], got[j - 1]))) < 1e-9:
            pairs.append((i - 1, j - 1)); i -= 1; j -= 1
        elif i > 0 and abs(D[i][j] - (D[i - 1][j] + _indel(exp[i - 1]))) < 1e-9:
            pairs.append((i - 1, None)); i -= 1
        else:
            pairs.append((None, j - 1)); j -= 1
    return D[n][m], pairs[::-1]


# ---------------------------------------------------------------------------
# Verse lines
# ---------------------------------------------------------------------------

def verse_lines(slok):
    """Split the Devanagari verse into lines, dropping dandas and the verse number."""
    lines = []
    for raw in slok.split("\n"):
        text = re.sub(r"\|\|[^|]*\|\|", "", raw)       # ||२-४७||
        text = re.sub(r"\((?:or|var)\s*[^)]*\)", "", text)   # alternate readings: "(or वियुक्तैस्तु)", "(var अशेषाणि)"
        text = re.sub(r"[|।॥]", "", text).strip()
        text = fix_deva(text)
        if not text:
            continue
        words = text.split()
        speaker = len(words) <= 3 and words[-1].endswith("वाच")
        lines.append({"deva": text, "roman": deva_to_iast(text), "speaker": speaker})
    return lines


# ---------------------------------------------------------------------------
# Sivananda word lists
# ---------------------------------------------------------------------------

DEVA_RUN = r"[ऀ-ॿ‌‍]+(?:\s+[ऀ-ॿ‌‍]+)*"

VOWEL_SIGN_TO_VOWEL = {"ा": "आ", "ि": "इ", "ी": "ई", "ु": "उ", "ू": "ऊ", "ृ": "ऋ", "े": "ए", "ै": "ऐ",
                       "ो": "ओ", "ौ": "औ"}


def fix_deva(text):
    """Repair Devanagari spelling slips in the source.
    - a vowel sign typed before its consonant: अश्िवनौ -> अश्विनौ (11.6, 11.22)
    - a word that starts with a vowel sign: ुच्यते -> उच्यते (18.23)
    - श्रृ for शृ, the Hindi spelling (श्रृणु -> शृणु)
    - ृ plus nukta for ॄ (पितृ़न् -> पितॄन्)"""
    text = re.sub("्([ा-ौ])([क-ह])", "्\\2\\1", text)
    text = re.sub(r"(^|\s)([ा-ौ])", lambda m: m.group(1) + VOWEL_SIGN_TO_VOWEL.get(m.group(2), ""), text)
    return text.replace("श्रृ", "शृ").replace("ृ़", "ॄ")


def sivananda_text(verse):
    for c in verse["commentaries"]:
        if c["author"] == "Swami Sivananda" and c["language"] == "english":
            return c["text"]
    return None


def parse_list(text):
    """Return (header_verses, parts) or (None, []) when there is no usable list."""
    text = re.sub(r"^\s*English Commentary By Swami Sivananda", "", text)
    head = re.split(r"commentary", text, maxsplit=1, flags=re.I)[0]   # 12.16 spells it "CommentarY"
    m = re.match(r"\s*(\d+)\.(\d+)(?:\s*-+\s*(\d+)\.(\d+))?\s*", head)
    if not m:
        return None, []
    first = (int(m.group(1)), int(m.group(2)))
    last = (int(m.group(3)), int(m.group(4))) if m.group(3) else first
    body = head[m.end():]
    # Every Devanagari run starts a part; the text up to the next run is its meaning.
    # "?" was a comma in the original, both between parts and inside meanings.
    parts = []
    for mm in re.finditer(r"(" + DEVA_RUN + r")([^ऀ-ॿ]*)", body):
        parts.append({"deva": fix_deva(mm.group(1).strip()), "meaning": mm.group(2).replace("?", ",")})
    for p in parts:
        g = re.sub(r"\s+", " ", p["meaning"]).strip()
        g = re.sub(r"\s*-{2,}\s*", ", ", g)       # "nectar -- the remnants"
        g = re.sub(r"(,\s*)+,", ",", g).strip(" ,;")
        g = re.split(r"\.(?=[A-Z][a-z])", g)[0]    # prose glued on: "sorrow.Thus in the Upanishads..."
        g = re.sub(r"\.?\s*No\s*$", "", g)        # "...battle.No " before "No Commentary"
        g = g.rstrip(" .,;")
        p["meaning"] = g
        p["roman"] = deva_to_iast(p["deva"])
    parts = [p for p in parts if p["meaning"] and sound_key(p["roman"])]
    verses = [first] if first == last else [first, last]
    return verses, parts


def apply_verse_fixes(parts, ov, vid):
    """Hand fixes for one verse from chant_overrides.json. Warns when a fix no longer matches."""
    for old, new in ov.get("deva_fixes", []):
        hits = [p for p in parts if p["deva"] == old]
        for p in hits:
            p["deva"], p["roman"] = new, deva_to_iast(new)
        if not hits:
            print("  warning: %s deva fix %s did not match" % (vid, old))
    for deva, old, new in ov.get("meaning_fixes", []):
        hits = [p for p in parts if p["deva"] == deva and p["meaning"] == old]
        for p in hits:
            p["meaning"] = new
        if not hits:
            print("  warning: %s meaning fix %s / %s did not match" % (vid, deva, old))


def apply_gloss_fixes(parts, fixes):
    """Repair damaged words. Each fix also applies to the capitalized form ("Selfrestrained")."""
    for p in parts:
        for wrong, right in fixes.items():
            for w, r in {(wrong, right), (wrong[:1].upper() + wrong[1:], right[:1].upper() + right[1:])}:
                p["meaning"] = re.sub(r"(?<![A-Za-z])" + re.escape(w) + r"(?![A-Za-z])", r, p["meaning"])


# ---------------------------------------------------------------------------
# Lining up parts with the verse
# ---------------------------------------------------------------------------

def place_parts(lines_by_verse, parts):
    """Assign each part to a (verse_id, line_index). Returns placement, metrics."""
    exp, owner = [], []            # owner[k] = (verse_id, line_index) for each expected letter
    for vid, lines in lines_by_verse:
        for li, line in enumerate(lines):
            if line["speaker"]:
                continue
            k = sound_key(line["roman"])
            exp.append(k)
            owner.extend([(vid, li)] * len(k))
    exp = "".join(exp)
    got, part_of = [], []
    for pi, p in enumerate(parts):
        k = sound_key(p["roman"])
        got.append(k)
        part_of.extend([pi] * len(k))
    got = "".join(got)
    cost, pairs = align(exp, got)
    matched = [[] for _ in parts]
    exp_hit = got_hit = 0
    for ei, gi in pairs:
        if ei is not None and gi is not None:
            matched[part_of[gi]].append(ei)
            exp_hit += 1
            got_hit += 1
    placement = []
    for pi, hits in enumerate(matched):
        placement.append(owner[sorted(hits)[len(hits) // 2]] if hits else None)
    metrics = {
        "verse_coverage": exp_hit / max(len(exp), 1),
        "list_coverage": got_hit / max(len(got), 1),
        "cost_ratio": cost / max(len(exp), 1),
    }
    return placement, metrics


# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------

def load_json(path, default):
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    return default


def build():
    data = load_json(SRC, None)
    verses = {v["id"]: v for v in data["verses"]}
    fixes = load_json(GLOSS_FIXES, {})
    overrides = load_json(OVERRIDES, {})
    out, report = {}, []

    for vid, v in verses.items():
        out[vid] = {"available": False, "lines": [dict(l, parts=[]) for l in verse_lines(v["slok"])]}

    done = set()
    for vid, v in verses.items():
        if vid in done:
            continue
        text = sivananda_text(v)
        reason = None
        ov = overrides.get(vid, {})
        if not text:
            reason = "no Sivananda commentary"
        else:
            header, parts = parse_list(text)
            if "merge_with" in ov:
                # Hand review: these verses' lists belong together (see chant_overrides.json).
                parts = [p for i in ov["merge_with"] for p in parse_list(sivananda_text(verses[i]))[1]]
                header = [tuple(int(n) for n in i[2:].split(".")) for i in ov["merge_with"]]
            if "parts" in ov:
                parts = [{"deva": d, "roman": deva_to_iast(d), "meaning": m} for d, m in ov["parts"]]
            if header is None or not parts:
                reason = "no word list found"
            else:
                ids = ["BG%d.%d" % h for h in header]
                if "merge_with" in ov:
                    ids = list(ov["merge_with"])
                elif len(ids) == 2:
                    # A shared list such as "1.21 -- 1.22": place parts across both verses.
                    ch, a, b = header[0][0], header[0][1], header[1][1]
                    ids = ["BG%d.%d" % (ch, n) for n in range(a, b + 1)]
                if vid not in ids:
                    reason = "list header says %s" % ", ".join(i[2:] for i in ids)
                else:
                    apply_gloss_fixes(parts, fixes)
                    apply_verse_fixes(parts, ov, vid)
                    lines_by_verse = [(i, out[i]["lines"]) for i in ids]
                    placement, mx = place_parts(lines_by_verse, parts)
                    problems = []
                    if mx["verse_coverage"] < MIN_VERSE_COVERAGE:
                        problems.append("list covers %.0f%% of the verse" % (100 * mx["verse_coverage"]))
                    if mx["list_coverage"] < MIN_LIST_COVERAGE:
                        problems.append("%.0f%% of the list isn't in the verse" % (100 * (1 - mx["list_coverage"])))
                    if mx["cost_ratio"] > MAX_COST_RATIO:
                        problems.append("poor alignment (%.2f)" % mx["cost_ratio"])
                    if any(p is None for p in placement):
                        problems.append("a part matched nothing")
                    for i in ids:
                        for li, line in enumerate(out[i]["lines"]):
                            if not line["speaker"] and not any(pl == (i, li) for pl in placement if pl):
                                problems.append("%s line %d got no parts" % (i[2:], li + 1))
                    if problems:
                        reason = "; ".join(problems)
                    else:
                        for p, pl in zip(parts, placement):
                            out[pl[0]]["lines"][pl[1]]["parts"].append(
                                {"deva": p["deva"], "roman": p["roman"], "meaning": p["meaning"]})
                        for i in ids:
                            out[i]["available"] = True
                            done.add(i)
                        report.append((ids, mx, None))
                        continue
                    for i in ids:
                        done.add(i)
                        report.append(([i], mx if text else None, reason))
                    continue
        report.append(([vid], None, reason))
        done.add(vid)

    for vid, ov in overrides.items():
        if ov.get("available") is False:
            out[vid]["available"] = False
            for line in out[vid]["lines"]:
                line["parts"] = []
            report.append(([vid], None, "override: " + ov.get("reason", "hand review")))

    for vid in out:
        if not out[vid]["available"]:
            for line in out[vid]["lines"]:
                line["parts"] = []
    return out, report, verses


def suggest_gloss_fixes():
    words = set()
    with open("/usr/share/dict/words") as f:
        for w in f:
            words.add(w.strip().lower())
    data = load_json(SRC, None)
    seen = {}
    for v in data["verses"]:
        text = sivananda_text(v)
        if not text:
            continue
        _, parts = parse_list(text)
        for p in parts:
            for tok in re.findall(r"[A-Za-z]+", p["meaning"]):
                t = tok.lower()
                if tok[0].isupper() or t in words or len(t) < 4:
                    continue
                for k in range(len(t) + 1):
                    cand = t[:k] + "qu" + t[k:]
                    if cand in words:
                        seen.setdefault(t, set()).add(cand)
                for k in range(3, len(t) - 2):
                    if t[:k] in words and t[k:] in words and len(t[k:]) >= 3:
                        seen.setdefault(t, set()).add(t[:k] + "-" + t[k:])
                if t in ("dont", "doesnt", "cant", "wont", "isnt", "didnt"):
                    seen.setdefault(t, set()).add(t[:-1] + "'" + t[-1])
    for t in sorted(seen):
        print("%-24s -> %s" % (t, ", ".join(sorted(seen[t]))))


def main():
    if "--suggest-gloss-fixes" in sys.argv:
        suggest_gloss_fixes()
        return
    out, report, verses = build()
    payload = {
        "source": "Word lists from Swami Sivananda's English commentary in gita_data.json",
        "verses": out,
    }
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
    available = sum(1 for v in out.values() if v["available"])
    parts = sum(len(l["parts"]) for v in out.values() for l in v["lines"])
    print("chant_data.json: %d of %d verses available, %d parts, %.0f KB"
          % (available, len(out), parts, os.path.getsize(OUT) / 1024))
    flagged = [(ids, r) for ids, mx, r in report if r]
    print("unavailable (%d):" % len(flagged))
    for ids, r in sorted(flagged, key=lambda x: [int(n) for n in x[0][0][2:].split(".")]):
        print("  %-8s %s" % (",".join(i[2:] for i in ids), r))
    if "--report" not in sys.argv:
        for path in APP_COPIES:
            shutil.copyfile(OUT, path)
            print("copied to", os.path.relpath(path, ROOT))


if __name__ == "__main__":
    main()
