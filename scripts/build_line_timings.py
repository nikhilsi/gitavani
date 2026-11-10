#!/usr/bin/env python3
"""
Find where each verse line starts and ends in the reciter's recordings (Learn, spec 8.3 and 8.4).

Writes scripts/data/line_timings.json:
    { "BG2.47": { "lines": [[0.00, 6.42], [6.71, 13.80]], "skip": [] }, ... }
Times are seconds, one pair per line of chant_data.json (speaker lines included).
"skip" holds stretches that aren't practiced: the chapter-closing line after a chapter's last verse.
build_chant_data.py merges these into chant_data.json.

Method:
  1. Decode the MP3 to 16 kHz mono with ffmpeg and measure loudness every 20 ms.
  2. Predict each line break from the lines' syllable counts.
  3. Snap each break to the longest quiet stretch near the prediction.

With --verify, every line is cut out and transcribed alone with Apple's recognizer
(scripts/chant_tools/transcribe.swift, macOS 26 or later). A line passes when its transcript
matches its own text better than its neighbours' and scores at least MIN_LINE_SCORE.

Dev tools only (ffmpeg, Swift, macOS). Nothing here ships in the apps.

Usage:
    python3 scripts/build_line_timings.py            # compute timings
    python3 scripts/build_line_timings.py --verify   # compute, then check every line
"""

import array
import hashlib
import json
import os
import re
import subprocess
import sys
import tempfile
import wave

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_chant_data as chant  # noqa: E402  (reuses verse lines, sound key and alignment)

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
AUDIO_DIR = os.path.join(ROOT, "ios", "GitaVani", "GitaVani", "Resources", "audio")
OUT = os.path.join(ROOT, "scripts", "data", "line_timings.json")
TOOLS = os.path.join(ROOT, "scripts", "chant_tools")
CACHE = os.path.join(tempfile.gettempdir(), "gitavani_chant_cache")

RATE = 16000
FRAME = 320                  # 20 ms
QUIET_RATIO = 0.12           # a frame is quiet below this share of the typical voiced loudness
MIN_QUIET_FRAMES = 3         # 60 ms
WINDOW_SHARE = 0.4           # search this share of an average line on each side of a predicted break
MIN_LINE_SCORE = 0.6

# Chapter announcements the reciter chants around the verses (sized in syllables, plus the chapter name):
# before the first verse of chapters 2 to 18: "atha <name>o nāma <ordinal> 'dhyāyaḥ"
# after the last verse of every chapter: "iti śrīmadbhagavadgītāsūpaniṣatsu brahmavidyāyāṃ yogaśāstre
#   śrīkṛṣṇārjunasaṃvāde <name>o nāma <ordinal> 'dhyāyaḥ"
OPENING_SYLLABLES = 10
CLOSING_SYLLABLES = 38


def syllables(deva):
    """Count syllables: independent vowels plus consonants not silenced by a virama."""
    n = 0
    for i, ch in enumerate(deva):
        if ch in chant.VOWELS:
            n += 1
        elif ch in chant.CONSONANTS:
            nxt = deva[i + 1] if i + 1 < len(deva) else ""
            if nxt == chant.NUKTA:
                nxt = deva[i + 2] if i + 2 < len(deva) else ""
            if nxt != chant.VIRAMA:
                n += 1
    return max(n, 1)


def decode(vid):
    os.makedirs(CACHE, exist_ok=True)
    wav_path = os.path.join(CACHE, vid + ".wav")
    if not os.path.exists(wav_path):
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i",
                        os.path.join(AUDIO_DIR, vid + ".mp3"), "-ac", "1", "-ar", str(RATE), wav_path],
                       check=True)
    with wave.open(wav_path) as w:
        samples = array.array("h", w.readframes(w.getnframes()))
    return samples


def frame_loudness(samples):
    out = []
    for i in range(0, len(samples) - FRAME + 1, FRAME):
        chunk = samples[i:i + FRAME]
        out.append((sum(x * x for x in chunk) / FRAME) ** 0.5)
    return out


def quiet_runs(loud, threshold):
    runs, start = [], None
    for i, v in enumerate(loud + [threshold + 1]):
        if v < threshold and start is None:
            start = i
        elif v >= threshold and start is not None:
            if i - start >= MIN_QUIET_FRAMES:
                runs.append((start, i))
            start = None
    return runs


def segment(vid, lines, opening, closing):
    """opening and closing are syllable weights of chapter announcements (0 when there is none).
    Return ([[start, end], ...] per line, skip list) in seconds."""
    loud = frame_loudness(decode(vid))
    voiced = sorted(v for v in loud if v > 50) or [1.0]
    typical = voiced[len(voiced) // 2]
    threshold = QUIET_RATIO * typical
    speaking = [i for i, v in enumerate(loud) if v >= threshold]
    first, last = (speaking[0], speaking[-1]) if speaking else (0, len(loud) - 1)

    weights = ([opening] if opening else []) + [syllables(l["deva"]) for l in lines] + ([closing] if closing else [])
    total = sum(weights)
    span = last - first
    avg_line = span / len(weights)
    runs = quiet_runs(loud, threshold)

    cuts, cum = [], 0
    for w in weights[:-1]:
        cum += w
        predicted = first + span * cum / total
        lo, hi = predicted - WINDOW_SHARE * avg_line, predicted + WINDOW_SHARE * avg_line
        near = [r for r in runs if r[1] > lo and r[0] < hi and (not cuts or r[0] > cuts[-1][1])]
        if near:
            # Prefer long pauses. A pause far from the prediction counts for less
            # (the reciter also pauses inside lines, at the quarter-verse).
            reach = WINDOW_SHARE * avg_line
            best = max(near, key=lambda r: (r[1] - r[0]) * (1 - 0.6 * min(abs((r[0] + r[1]) / 2 - predicted) / reach, 1)))
            cuts.append(best)
        else:
            # No pause near the prediction: cut at the quietest moment, always after the previous cut.
            after = cuts[-1][1] + MIN_QUIET_FRAMES if cuts else first + 1
            a, b = int(max(lo, after)), int(min(hi, last - 1))
            smooth = [sum(loud[max(i - 2, 0):i + 3]) for i in range(a, max(b, a + 1))]
            k = a + smooth.index(min(smooth))
            cuts.append((k, k))

    bounds = [first] + [c for cut in cuts for c in cut] + [last + 1]
    secs = [round(f * FRAME / RATE, 2) for f in bounds]
    segments = [[secs[2 * i], secs[2 * i + 1]] for i in range(len(weights))]
    skip = []
    if opening:
        skip.append(segments.pop(0))
    if closing:
        skip.append(segments.pop())
    return segments, skip, {"cuts": cuts, "runs": len(runs)}


def build():
    data = json.load(open(os.path.join(ROOT, "data", "gita_data.json"), encoding="utf-8"))
    last_verse = {}
    for v in data["verses"]:
        last_verse[v["chapter"]] = max(last_verse.get(v["chapter"], 0), v["verse"])
    name = {c["chapter_number"]: syllables(c["name"]) for c in data["chapters"]}
    out = {}
    for v in data["verses"]:
        lines = chant.verse_lines(v["slok"])
        opening = OPENING_SYLLABLES + name[v["chapter"]] if v["verse"] == 1 and v["chapter"] > 1 else 0
        closing = CLOSING_SYLLABLES + name[v["chapter"]] if v["verse"] == last_verse[v["chapter"]] else 0
        segments, skip, _ = segment(v["id"], lines, opening, closing)
        out[v["id"]] = {"lines": segments, "skip": skip, "verified": None}
    return data, out


def write(out):
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=0, sort_keys=True)
    verified = sum(1 for x in out.values() if x["verified"])
    print("line_timings.json: %d verses, %d lines, %d verses verified for Learn"
          % (len(out), sum(len(x["lines"]) for x in out.values()), verified))


# ---------------------------------------------------------------------------
# Verification
# ---------------------------------------------------------------------------

def transcriber():
    binary = os.path.join(TOOLS, ".build", "transcribe")
    source = os.path.join(TOOLS, "transcribe.swift")
    if not os.path.exists(binary) or os.path.getmtime(binary) < os.path.getmtime(source):
        os.makedirs(os.path.dirname(binary), exist_ok=True)
        subprocess.run(["swiftc", "-parse-as-library", "-O", source, "-o", binary], check=True)
    return binary


TRANSCRIPTS = os.path.join(CACHE, "transcripts.json")   # line audio digest -> transcript
BATCH = 100


def transcribe_all(paths):
    """Transcribe line WAVs, reusing earlier transcripts of identical audio. Apple's recognizer
    sometimes stalls. Files go in batches with a time limit. A stalled batch resumes where
    it stopped. A file that stalls twice gets an empty transcript (its line then fails the check)."""
    cache = {}
    if os.path.exists(TRANSCRIPTS):
        with open(TRANSCRIPTS, encoding="utf-8") as f:
            cache = json.load(f)
    digest = {}
    for p in paths:
        with open(p, "rb") as f:
            digest[p] = hashlib.sha1(f.read()).hexdigest()
    for p in paths:
        # Apple's recognizer hangs on an empty clip instead of returning nothing.
        with wave.open(p) as w:
            if w.getnframes() < RATE // 5 and digest[p] not in cache:
                cache[digest[p]] = ""
    todo = [p for p in paths if digest[p] not in cache]
    print("transcribing %d of %d lines (the rest are cached)..." % (len(todo), len(paths)))
    stalls = {}
    while todo:
        batch = todo[:BATCH]
        try:
            out = subprocess.run([transcriber(), "hi-IN"] + batch, capture_output=True, text=True,
                                 timeout=30 + 3 * len(batch)).stdout
        except subprocess.TimeoutExpired as e:
            out = e.stdout.decode() if isinstance(e.stdout, bytes) else (e.stdout or "")
        done = {}
        for line in out.splitlines():
            m = re.match(r"\S+ (\S+)\.wav: (.*)", line)
            if m:
                done[m.group(1)] = m.group(2)
        rest = []
        for p in batch:
            name = os.path.basename(p)[:-4]
            if name in done:
                cache[digest[p]] = done[name]
            else:
                rest.append(p)
        if rest:
            stuck = rest[0]
            stalls[stuck] = stalls.get(stuck, 0) + 1
            print("  transcriber stopped at %s (%d left in batch)" % (os.path.basename(stuck), len(rest)))
            if stalls[stuck] >= 2:
                cache[digest[stuck]] = ""
                rest = rest[1:]
        todo = rest + todo[BATCH:]
        with open(TRANSCRIPTS, "w", encoding="utf-8") as f:
            json.dump(cache, f, ensure_ascii=False)
    return {os.path.basename(p)[:-4]: cache[digest[p]] for p in paths}


def line_score(expected_roman, heard):
    exp, got = chant.sound_key(expected_roman), chant.sound_key(heard)
    if not exp:
        return 1.0
    cost, _ = chant.align(exp, got)
    return max(0.0, 1 - cost / len(exp))


def verify(data, timings):
    cut_dir = os.path.join(CACHE, "lines")
    os.makedirs(cut_dir, exist_ok=True)
    jobs = []
    for v in data["verses"]:
        samples = decode(v["id"])
        for i, (start, end) in enumerate(timings[v["id"]]["lines"]):
            path = os.path.join(cut_dir, "%s_L%d.wav" % (v["id"], i + 1))
            with wave.open(path, "wb") as w:
                w.setnchannels(1)
                w.setsampwidth(2)
                w.setframerate(RATE)
                w.writeframes(samples[int(start * RATE):int(end * RATE)].tobytes())
            jobs.append(path)
    heard = transcribe_all(jobs)
    flagged, scores = [], []
    for v in data["verses"]:
        lines = chant.verse_lines(v["slok"])
        for i, line in enumerate(lines):
            key = "%s_L%d" % (v["id"], i + 1)
            text = heard.get(key, "")
            own = line_score(line["roman"], text)
            others = [line_score(lines[j]["roman"], text) for j in (i - 1, i + 1) if 0 <= j < len(lines)]
            scores.append(own)
            if own < MIN_LINE_SCORE or any(o > own for o in others):
                flagged.append((key, round(own, 2), [round(o, 2) for o in others], text[:70]))
    print("lines checked: %d, passed: %d, flagged: %d" % (len(scores), len(scores) - len(flagged), len(flagged)))
    for f in flagged:
        print("  %-12s own %.2f  neighbours %s  heard: %s" % f)
    return flagged


def main():
    data, timings = build()
    if "--verify" in sys.argv:
        flagged = {f[0].split("_L")[0] for f in verify(data, timings)}
        for vid in timings:
            timings[vid]["verified"] = vid not in flagged   # a verse with any failing line offers no Learn
    write(timings)


if __name__ == "__main__":
    main()
