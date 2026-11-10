package com.nikhilsi.gitavani.learn

import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Learn's scorer (docs/chant-coach-spec.md, section 7.3).
 *
 * A rule-for-rule port of scripts/chant_scorer.py. scripts/data/scorer_fixtures.json holds shared
 * cases that the Python, Swift and Kotlin scorers must agree on. Change the Python reference first,
 * regenerate the fixtures, then mirror the change here and in ChantScorer.swift.
 */
object ChantScorer {
    const val THRESHOLD = 0.6        // a part scoring at or above this is Right
    const val SKIP_SHARE = 0.7       // a low part with at least this share of its letters unheard was skipped
    const val UNFINISHED_PARTS = 2   // this many skipped parts at the very end means the take stopped early
    const val GAP_OPEN = 1.0         // extra cost to start a run of unheard or extra letters
    private const val EPS = 1e-9

    enum class Mark(val code: String) { RIGHT("right"), SLIPPED("slipped"), NOT_CHECKED("not_checked") }

    enum class TipKind(val code: String) {
        NOTHING_HEARD("nothing_heard"), SKIPPED("skipped"), SLIPPED("slipped"),
        UNFINISHED("unfinished"), ALL_RIGHT("all_right")
    }

    data class Result(val marks: List<Mark>, val scores: List<Double?>, val tipKind: TipKind, val tipPart: Int?)

    /** A line to score: its sound key and one stretch [start, end) of the key per word part. */
    data class ScoringLine(val key: String, val stretches: List<Pair<Int, Int>>)

    // ---------------------------------------------------------------------------------------------
    // Devanagari to IAST (Android's recognizer writes Devanagari)
    // ---------------------------------------------------------------------------------------------

    // ऑ ॉ and ऍ ॅ are Hindi letters for English sounds. Sanskrit never uses them. The recognizer does.
    private val VOWELS = mapOf(
        'अ' to "a", 'आ' to "ā", 'इ' to "i", 'ई' to "ī", 'उ' to "u", 'ऊ' to "ū", 'ऋ' to "ṛ", 'ॠ' to "ṝ",
        'ऌ' to "ḷ", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au", 'ऑ' to "o", 'ऍ' to "e"
    )
    private val SIGNS = mapOf(
        'ा' to "ā", 'ि' to "i", 'ी' to "ī", 'ु' to "u", 'ू' to "ū", 'ृ' to "ṛ", 'ॄ' to "ṝ", 'ॢ' to "ḷ",
        'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au", 'ॉ' to "o", 'ॅ' to "e"
    )
    private val CONSONANTS = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ṅ", 'च' to "c", 'छ' to "ch", 'ज' to "j",
        'झ' to "jh", 'ञ' to "ñ", 'ट' to "ṭ", 'ठ' to "ṭh", 'ड' to "ḍ", 'ढ' to "ḍh", 'ण' to "ṇ", 'त' to "t",
        'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n", 'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh",
        'म' to "m", 'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'श' to "ś", 'ष' to "ṣ", 'स' to "s",
        'ह' to "h", 'ळ' to "ḷ"
    )
    private const val VIRAMA = '्'
    private const val NUKTA = '़'

    fun devaToIast(text: String): String {
        val out = StringBuilder()
        var i = 0
        val n = text.length
        while (i < n) {
            val ch = text[i]
            val base = CONSONANTS[ch]
            if (base != null) {
                i += 1
                if (i < n && text[i] == NUKTA) i += 1
                if (i < n && SIGNS.containsKey(text[i])) {
                    out.append(base).append(SIGNS[text[i]])
                    i += 1
                } else if (i < n && text[i] == VIRAMA) {
                    out.append(base)
                    i += 1
                } else {
                    out.append(base).append('a')
                }
                continue
            }
            when {
                VOWELS.containsKey(ch) -> out.append(VOWELS[ch])
                ch == 'ं' || ch == 'ँ' -> out.append("ṃ")
                ch == 'ः' -> out.append("ḥ")
                ch == 'ऽ' -> out.append("'")
                ch == 'ॐ' -> out.append("oṃ")
                ch == NUKTA || ch == '‌' || ch == '‍' -> Unit
                else -> out.append(ch)
            }
            i += 1
        }
        return out.toString()
    }

    // ---------------------------------------------------------------------------------------------
    // Sound key
    // ---------------------------------------------------------------------------------------------

    private val IAST_FOLD = listOf(
        "ṛ" to "ri", "ṝ" to "ri", "ḷ" to "li", "ṃ" to "m", "ḥ" to "h", "ṅ" to "n", "ñ" to "n",
        "ṇ" to "n", "ṭ" to "t", "ḍ" to "d", "ś" to "sh", "ṣ" to "sh", "ā" to "a", "ī" to "i", "ū" to "u"
    )
    private val SOUND_FOLD = listOf(
        "x" to "ks", "ksh" to "ks", "sh" to "s", "th" to "t", "dh" to "d", "bh" to "b", "ph" to "p",
        "kh" to "k", "gh" to "g", "jh" to "j", "ch" to "c", "f" to "p", "w" to "v", "ee" to "i",
        "oo" to "u", "ri" to "r", "ru" to "r"
    )

    /** Coarse sound key for IAST and for Hindi-style Roman letters. */
    fun soundKey(text: String): String {
        var s = text.lowercase()
        for ((a, b) in IAST_FOLD) s = s.replace(a, b)
        // Drop remaining accents (compatibility decomposition), keep a to z only.
        s = Normalizer.normalize(s, Normalizer.Form.NFKD).filter { it in 'a'..'z' }
        for ((a, b) in SOUND_FOLD) s = s.replace(a, b)
        val out = StringBuilder()
        var last: Char? = null
        for (c in s) {
            if (c != last) out.append(c)
            last = c
        }
        return out.toString()
    }

    // ---------------------------------------------------------------------------------------------
    // Alignment
    // ---------------------------------------------------------------------------------------------

    private fun isVowel(c: Char) = c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u'

    private fun subCost(a: Char, b: Char): Double = when {
        a == b -> 0.0
        isVowel(a) && isVowel(b) -> 0.5
        else -> 1.0
    }

    private fun indelCost(c: Char): Double = if (c == 'a' || c == 'h') 0.3 else 1.0

    /**
     * Lines up the expected key with what was heard (affine gaps; speech before and after the line is free).
     * Returns, per expected letter, the cost charged to it and whether it went unheard.
     */
    fun alignTake(exp: String, got: String): Pair<DoubleArray, BooleanArray> {
        val n = exp.length
        val m = got.length
        val inf = Double.POSITIVE_INFINITY
        val mm = Array(n + 1) { DoubleArray(m + 1) { inf } }
        val xx = Array(n + 1) { DoubleArray(m + 1) { inf } }
        val yy = Array(n + 1) { DoubleArray(m + 1) { inf } }
        mm[0][0] = 0.0
        for (j in 1..m) yy[0][j] = 0.0
        for (i in 1..n) xx[i][0] = (if (i > 1) xx[i - 1][0] else GAP_OPEN) + indelCost(exp[i - 1])
        for (i in 1..n) {
            val e = exp[i - 1]
            for (j in 1..m) {
                val g = got[j - 1]
                mm[i][j] = subCost(e, g) + min(min(mm[i - 1][j - 1], xx[i - 1][j - 1]), yy[i - 1][j - 1])
                xx[i][j] = indelCost(e) + min(min(mm[i - 1][j] + GAP_OPEN, xx[i - 1][j]), yy[i - 1][j] + GAP_OPEN)
                yy[i][j] = indelCost(g) + min(min(mm[i][j - 1] + GAP_OPEN, xx[i][j - 1] + GAP_OPEN), yy[i][j - 1])
            }
        }

        // Speech after the line is free: end at the first column (and state) with the lowest cost.
        var best = inf
        var j = 0
        var state = 0   // 0 = M, 1 = X, 2 = Y
        for (jj in 0..m) {
            if (mm[n][jj] < best - EPS) { best = mm[n][jj]; j = jj; state = 0 }
            if (xx[n][jj] < best - EPS) { best = xx[n][jj]; j = jj; state = 1 }
        }

        val cost = DoubleArray(n)
        val missed = BooleanArray(n)
        var i = n
        while (i > 0) {
            val e = exp[i - 1]
            when (state) {
                0 -> {
                    val c = subCost(e, got[j - 1])
                    cost[i - 1] += c
                    val prev = mm[i][j] - c
                    state = if (abs(mm[i - 1][j - 1] - prev) < EPS) 0 else if (abs(xx[i - 1][j - 1] - prev) < EPS) 1 else 2
                    i -= 1
                    j -= 1
                }
                1 -> {
                    val c = indelCost(e)
                    missed[i - 1] = true
                    val prev = xx[i][j] - c
                    if (abs(mm[i - 1][j] + GAP_OPEN - prev) < EPS) {
                        cost[i - 1] += c + GAP_OPEN; state = 0
                    } else if (abs(xx[i - 1][j] - prev) < EPS) {
                        cost[i - 1] += c; state = 1
                    } else {
                        cost[i - 1] += c + GAP_OPEN; state = 2
                    }
                    i -= 1
                }
                else -> {
                    // An extra heard letter inside the line: half its cost goes to the expected letter before it.
                    val c = indelCost(got[j - 1])
                    val prev = yy[i][j] - c
                    if (abs(mm[i][j - 1] + GAP_OPEN - prev) < EPS) {
                        cost[i - 1] += 0.5 * (c + GAP_OPEN); state = 0
                    } else if (abs(xx[i][j - 1] + GAP_OPEN - prev) < EPS) {
                        cost[i - 1] += 0.5 * (c + GAP_OPEN); state = 1
                    } else {
                        cost[i - 1] += 0.5 * c; state = 2
                    }
                    j -= 1
                }
            }
        }
        return cost to missed
    }

    // ---------------------------------------------------------------------------------------------
    // Scoring
    // ---------------------------------------------------------------------------------------------

    /** [heard] is Roman letters; pass Android's Devanagari through [devaToIast] first. */
    fun scoreLine(line: ScoringLine, heard: String, notChecked: Set<Int> = emptySet()): Result {
        val got = soundKey(heard)
        val count = line.stretches.size
        if (got.isEmpty()) {
            return Result(
                marks = List(count) { if (it in notChecked) Mark.NOT_CHECKED else Mark.SLIPPED },
                scores = List(count) { if (it in notChecked) null else 0.0 },
                tipKind = TipKind.NOTHING_HEARD, tipPart = null
            )
        }
        val (cost, missed) = alignTake(line.key, got)
        val marks = ArrayList<Mark>()
        val scores = ArrayList<Double?>()
        val skipped = ArrayList<Boolean>()
        for ((i, stretch) in line.stretches.withIndex()) {
            val (s, e) = stretch
            if (i in notChecked || e <= s) {
                marks.add(Mark.NOT_CHECKED); scores.add(null); skipped.add(false)
                continue
            }
            val length = (e - s).toDouble()
            var total = 0.0
            for (k in s until e) total += cost[k]
            val score = max(0.0, 1.0 - total / length)
            val miss = (s until e).count { missed[it] } / length
            scores.add(score)
            marks.add(if (score >= THRESHOLD - EPS) Mark.RIGHT else Mark.SLIPPED)
            skipped.add(score < THRESHOLD - EPS && miss >= SKIP_SHARE)
        }

        // Skipped parts at the very end (ignoring Not checked ones) mean the take stopped early.
        val trailing = ArrayList<Int>()
        for (i in count - 1 downTo 0) {
            if (marks[i] == Mark.NOT_CHECKED) continue
            if (!skipped[i]) break
            trailing.add(i)
        }
        val unfinished = trailing.size >= UNFINISHED_PARTS
        val early = if (unfinished) trailing.toSet() else emptySet()

        val firstSkip = (0 until count).firstOrNull { skipped[it] && it !in early }
        if (firstSkip != null) return Result(marks, scores, TipKind.SKIPPED, firstSkip)
        val firstSlip = (0 until count).firstOrNull { marks[it] == Mark.SLIPPED && it !in early }
        if (firstSlip != null) return Result(marks, scores, TipKind.SLIPPED, firstSlip)
        return Result(marks, scores, if (unfinished) TipKind.UNFINISHED else TipKind.ALL_RIGHT, null)
    }

    /** One line made of several, for the Join and From memory steps. Speaker lines are left out. */
    fun joinLines(lines: List<Pair<ScoringLine, Boolean>>): ScoringLine {
        val key = StringBuilder()
        val stretches = ArrayList<Pair<Int, Int>>()
        for ((line, speaker) in lines) {
            if (speaker) continue
            val offset = key.length
            key.append(line.key)
            line.stretches.forEach { stretches.add((it.first + offset) to (it.second + offset)) }
        }
        return ScoringLine(key.toString(), stretches)
    }

    /** Calibration (spec D9): parts the phone can't hear even from the reciter are Not checked. */
    fun notChecked(line: ScoringLine, heardFromReciter: String): Set<Int> {
        val result = scoreLine(line, heardFromReciter)
        return result.marks.indices.filter { result.marks[it] != Mark.RIGHT }.toSet()
    }
}
