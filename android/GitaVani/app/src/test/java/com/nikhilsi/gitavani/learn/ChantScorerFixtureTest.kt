package com.nikhilsi.gitavani.learn

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the Kotlin scorer against the shared cases in scripts/data/scorer_fixtures.json,
 * which the Python reference (scripts/chant_scorer.py) produced. The Swift scorer is checked
 * against the same file in ios/ChantScorerTests.
 *
 * Run: cd android/GitaVani && ./gradlew testDebugUnitTest
 */
class ChantScorerFixtureTest {

    @Serializable data class KeyCase(val text: String, val key: String)
    @Serializable data class DevaCase(val deva: String, val iast: String)
    @Serializable data class Part(val roman: String, val k: List<Int>)
    @Serializable data class Line(val key: String, val parts: List<Part>)
    @Serializable data class Tip(val kind: String, val part: Int? = null)
    @Serializable data class Expected(val marks: List<String>, val scores: List<Double?>, val tip: Tip)
    @Serializable data class ScoreCase(
        val name: String,
        val line: Line,
        val heard: String,
        val not_checked: List<Int>,
        val expected: Expected,
    )
    @Serializable data class Fixtures(
        val sound_key: List<KeyCase>,
        val deva_to_iast: List<DevaCase>,
        val score: List<ScoreCase>,
    )

    private val fixtures: Fixtures by lazy {
        // Tests run from the app module; walk up to the repo root to find the shared file.
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        var file: File? = null
        while (dir != null && file == null) {
            val candidate = File(dir, "scripts/data/scorer_fixtures.json")
            if (candidate.exists()) file = candidate
            dir = dir.parentFile
        }
        assertNotNull("scripts/data/scorer_fixtures.json not found above ${System.getProperty("user.dir")}", file)
        Json { ignoreUnknownKeys = true }.decodeFromString(Fixtures.serializer(), file!!.readText())
    }

    @Test
    fun soundKeyMatchesReference() {
        for (c in fixtures.sound_key) {
            assertEquals("sound key of ${c.text}", c.key, ChantScorer.soundKey(c.text))
        }
    }

    @Test
    fun devanagariToIastMatchesReference() {
        for (c in fixtures.deva_to_iast) {
            assertEquals("IAST of ${c.deva}", c.iast, ChantScorer.devaToIast(c.deva))
        }
    }

    @Test
    fun scoresMatchReference() {
        assertTrue(fixtures.score.size > 100)
        for (c in fixtures.score) {
            val line = ChantScorer.ScoringLine(c.line.key, c.line.parts.map { it.k[0] to it.k[1] })
            val result = ChantScorer.scoreLine(line, c.heard, c.not_checked.toSet())
            assertEquals("marks: ${c.name}", c.expected.marks, result.marks.map { it.code })
            assertEquals("tip: ${c.name}", c.expected.tip.kind, result.tipKind.code)
            assertEquals("tip part: ${c.name}", c.expected.tip.part, result.tipPart)
            assertEquals("score count: ${c.name}", c.expected.scores.size, result.scores.size)
            for ((got, want) in result.scores.zip(c.expected.scores)) {
                if (got == null || want == null) {
                    assertEquals("score presence: ${c.name}", want, got)
                } else {
                    assertEquals("score: ${c.name}", want, got, 0.0011)
                }
            }
        }
    }

    @Test
    fun joinLeavesSpeakerLinesOut() {
        val speaker = ChantScorer.ScoringLine("arjunauvaca", emptyList())
        val a = ChantScorer.ScoringLine("abc", listOf(0 to 3))
        val b = ChantScorer.ScoringLine("de", listOf(0 to 1, 1 to 2))
        val joined = ChantScorer.joinLines(listOf(speaker to true, a to false, b to false))
        assertEquals("abcde", joined.key)
        assertEquals(listOf(0 to 3, 3 to 4, 4 to 5), joined.stretches)
    }
}
