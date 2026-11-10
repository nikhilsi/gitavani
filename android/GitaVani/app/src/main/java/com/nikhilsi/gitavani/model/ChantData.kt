package com.nikhilsi.gitavani.model

import com.nikhilsi.gitavani.learn.ChantScorer
import kotlinx.serialization.Serializable

/** Word-by-word data from chant_data.json (built by scripts/build_chant_data.py). */
@Serializable
data class ChantData(
    val source: String,
    val verses: Map<String, VerseWords>
)

@Serializable
data class VerseWords(
    /** False when the verse has no reliable word list yet. */
    val available: Boolean,
    /** True when Learn can teach this verse: word parts, plus line timings that passed the per-line check. */
    val learn: Boolean = false,
    val lines: List<WordLine>
)

@Serializable
data class WordLine(
    val deva: String,
    val roman: String,
    /** Lines like "श्रीभगवानुवाच" (the Blessed Lord said). They have no parts. */
    val speaker: Boolean,
    val parts: List<WordPart>,
    /** Learn: the line's sound key (scripts/chant_scorer.py) and its start and end in the verse recording. */
    val key: String = "",
    val audio: List<Double>? = null
) {
    val scoringLine: ChantScorer.ScoringLine
        get() = ChantScorer.ScoringLine(key, parts.map { it.k[0] to it.k[1] })
}

@Serializable
data class WordPart(
    val deva: String,
    val roman: String,
    val meaning: String,
    /** Learn: the stretch [start, end) of the line's sound key this part owns. */
    val k: List<Int> = listOf(0, 0)
)
