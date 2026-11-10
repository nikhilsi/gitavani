#!/usr/bin/env python3
"""
Reference scorer for Learn (docs/chant-coach-spec.md, section 7.3).

The Swift scorer (iOS) and the Kotlin scorer (Android) port this file rule for rule.
scripts/data/scorer_fixtures.json holds shared cases that all three must agree on
(built by scripts/build_scorer_fixtures.py).

    score_line(line, heard, not_checked=())
        line:        one line from chant_data.json: {"key": ..., "parts": [{"roman": ..., "k": [s, e]}, ...]}
        heard:       what the recognizer wrote for the take, in Roman letters
                     (Android converts its Devanagari with deva_to_iast first)
        not_checked: indexes of parts the phone can't hear even from the reciter (calibration, D9)
        returns      {"marks": [...], "scores": [...], "tip": {"kind": ..., "part": index or None}}

    marks: "right", "slipped" or "not_checked", one per part.
    tip kinds, in priority order: "nothing_heard", "skipped", "slipped", "unfinished", "all_right".
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from build_chant_data import deva_to_iast, sound_key  # noqa: E402,F401  (deva_to_iast is re-exported for Android parity)

THRESHOLD = 0.6        # a part scoring at or above this is Right
SKIP_SHARE = 0.7       # a low part with at least this share of its letters unheard was skipped
UNFINISHED_PARTS = 2   # this many skipped parts at the very end means the take stopped early
GAP_OPEN = 1.0         # extra cost to start a run of unheard or extra letters (tuned Nov 9 on the trial takes)
EPS = 1e-9
INF = float("inf")


def sub_cost(a, b):
    if a == b:
        return 0.0
    if a in "aeiou" and b in "aeiou":
        return 0.5
    return 1.0


def indel_cost(c):
    return 0.3 if c in "ah" else 1.0


def align_take(exp, got):
    """Line up the expected key with what was heard.

    Affine gaps: each run of unheard or extra letters pays GAP_OPEN once, plus each letter's own cost.
    Without it, cheap "a"/"h" gaps let heard letters smear across words that were never said.
    Extra speech before or after the line is free (semi-global alignment).

    Three tables: M (expected letter lined up with a heard letter), X (expected letter unheard),
    Y (extra heard letter). Ties break M, then X, then Y, in both the fill and the backtrace.
    Returns (cost, missed): per expected letter, the cost charged to it and whether it went unheard."""
    n, m = len(exp), len(got)
    M = [[INF] * (m + 1) for _ in range(n + 1)]
    X = [[INF] * (m + 1) for _ in range(n + 1)]
    Y = [[INF] * (m + 1) for _ in range(n + 1)]
    M[0][0] = 0.0
    for j in range(1, m + 1):
        Y[0][j] = 0.0                                   # speech before the line is free
    for i in range(1, n + 1):
        X[i][0] = (X[i - 1][0] if i > 1 else GAP_OPEN) + indel_cost(exp[i - 1])
    for i in range(1, n + 1):
        e = exp[i - 1]
        for j in range(1, m + 1):
            g = got[j - 1]
            M[i][j] = sub_cost(e, g) + min(M[i - 1][j - 1], X[i - 1][j - 1], Y[i - 1][j - 1])
            X[i][j] = indel_cost(e) + min(M[i - 1][j] + GAP_OPEN, X[i - 1][j], Y[i - 1][j] + GAP_OPEN)
            Y[i][j] = indel_cost(g) + min(M[i][j - 1] + GAP_OPEN, X[i][j - 1] + GAP_OPEN, Y[i][j - 1])

    # Speech after the line is free: end at the first column (and state) with the lowest cost.
    best, j, state = INF, 0, "M"
    for jj in range(m + 1):
        for s, table in (("M", M), ("X", X)):
            if table[n][jj] < best - EPS:
                best, j, state = table[n][jj], jj, s

    cost, missed = [0.0] * n, [False] * n
    i = n
    while i > 0:
        e = exp[i - 1]
        if state == "M":
            c = sub_cost(e, got[j - 1])
            cost[i - 1] += c
            prev = M[i][j] - c
            state = "M" if abs(M[i - 1][j - 1] - prev) < EPS else ("X" if abs(X[i - 1][j - 1] - prev) < EPS else "Y")
            i, j = i - 1, j - 1
        elif state == "X":
            c = indel_cost(e)
            missed[i - 1] = True
            prev = X[i][j] - c
            if abs(M[i - 1][j] + GAP_OPEN - prev) < EPS:
                cost[i - 1] += c + GAP_OPEN
                state = "M"
            elif abs(X[i - 1][j] - prev) < EPS:
                cost[i - 1] += c
                state = "X"
            else:
                cost[i - 1] += c + GAP_OPEN
                state = "Y"
            i -= 1
        else:
            # An extra heard letter inside the line: half its cost goes to the expected letter before it.
            c = indel_cost(got[j - 1])
            prev = Y[i][j] - c
            if abs(M[i][j - 1] + GAP_OPEN - prev) < EPS:
                cost[i - 1] += 0.5 * (c + GAP_OPEN)
                state = "M"
            elif abs(X[i][j - 1] + GAP_OPEN - prev) < EPS:
                cost[i - 1] += 0.5 * (c + GAP_OPEN)
                state = "X"
            else:
                cost[i - 1] += 0.5 * c
                state = "Y"
            j -= 1
    return cost, missed


def score_line(line, heard, not_checked=()):
    exp = line["key"]
    got = sound_key(heard or "")
    parts = line["parts"]
    if not got:
        return {"marks": ["not_checked" if i in not_checked else "slipped" for i in range(len(parts))],
                "scores": [None if i in not_checked else 0.0 for i in range(len(parts))],
                "tip": {"kind": "nothing_heard", "part": None}}
    cost, missed = align_take(exp, got)
    marks, scores, skipped = [], [], []
    for i, p in enumerate(parts):
        s, e = p["k"]
        if i in not_checked or e <= s:
            marks.append("not_checked")
            scores.append(None)
            skipped.append(False)
            continue
        score = max(0.0, 1.0 - sum(cost[s:e]) / (e - s))
        miss = sum(1 for x in missed[s:e] if x) / (e - s)
        scores.append(round(score, 3))
        marks.append("right" if score >= THRESHOLD - EPS else "slipped")
        skipped.append(score < THRESHOLD - EPS and miss >= SKIP_SHARE)

    # Skipped parts at the very end (ignoring Not checked ones) mean the take stopped early.
    trailing = []
    for i in reversed(range(len(parts))):
        if marks[i] == "not_checked":
            continue
        if not skipped[i]:
            break
        trailing.append(i)
    unfinished = len(trailing) >= UNFINISHED_PARTS
    early = set(trailing) if unfinished else set()

    tip = {"kind": "all_right", "part": None}
    first_skip = next((i for i in range(len(parts)) if skipped[i] and i not in early), None)
    first_slip = next((i for i in range(len(parts)) if marks[i] == "slipped" and i not in early), None)
    if first_skip is not None:
        tip = {"kind": "skipped", "part": first_skip}
    elif first_slip is not None:
        tip = {"kind": "slipped", "part": first_slip}
    elif unfinished:
        tip = {"kind": "unfinished", "part": None}
    return {"marks": marks, "scores": scores, "tip": tip}


def join_lines(lines):
    """One line made of several (for the Join and From memory steps). Speaker lines are left out."""
    key, parts = "", []
    for line in lines:
        if line.get("speaker"):
            continue
        offset = len(key)
        key += line["key"]
        for p in line["parts"]:
            parts.append(dict(p, k=[p["k"][0] + offset, p["k"][1] + offset]))
    return {"key": key, "parts": parts}


def not_checked_from_reference(line, heard_from_reciter):
    """Calibration (D9): parts the phone can't hear even from the reciter are Not checked."""
    result = score_line(line, heard_from_reciter)
    return [i for i, m in enumerate(result["marks"]) if m != "right"]
