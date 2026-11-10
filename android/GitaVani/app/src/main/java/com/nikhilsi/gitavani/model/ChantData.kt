package com.nikhilsi.gitavani.model

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
    val lines: List<WordLine>
)

@Serializable
data class WordLine(
    val deva: String,
    val roman: String,
    /** Lines like "श्रीभगवानुवाच" (the Blessed Lord said). They have no parts. */
    val speaker: Boolean,
    val parts: List<WordPart>
)

@Serializable
data class WordPart(
    val deva: String,
    val roman: String,
    val meaning: String
)
