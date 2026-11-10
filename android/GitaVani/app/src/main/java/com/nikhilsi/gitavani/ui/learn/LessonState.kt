package com.nikhilsi.gitavani.ui.learn

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nikhilsi.gitavani.audio.AudioService
import com.nikhilsi.gitavani.learn.ChantListener
import com.nikhilsi.gitavani.learn.ChantScorer
import com.nikhilsi.gitavani.model.VerseWords
import com.nikhilsi.gitavani.model.WordPart
import com.nikhilsi.gitavani.state.LearnProgress
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The lesson's state and steps (docs/chant-coach-spec.md, sections 6.2 to 6.6). Same flow as iOS's
 * LessonModel: Listen, each line, all lines together, then From memory (first letters, then nothing).
 * A clean take with nothing shown marks the verse Learned. Use from the main thread.
 */
@RequiresApi(33)
class LessonState(
    private val context: Context,
    val verseId: String,
    val words: VerseWords,
    private val audio: AudioService,
    private val progress: LearnProgress,
    private val scope: CoroutineScope,
) {
    sealed interface Setup {
        data object Checking : Setup
        data object NeedsMic : Setup
        data object MicDenied : Setup
        data object NeedsModel : Setup
        data class Downloading(val percent: Int?) : Setup
        data class Failed(val text: String) : Setup
        data object Ready : Setup
    }

    sealed interface Step {
        data object Listen : Step
        data class Line(val n: Int) : Step          // index into practiceLines
        data object Join : Step
        data class Memory(val hints: Boolean) : Step
        data object Learned : Step
    }

    /** A word part shown in the current step, with the verse line it came from. */
    data class Part(val line: Int, val part: WordPart)

    var setup: Setup by mutableStateOf(Setup.Checking)
        private set
    var step: Step by mutableStateOf(Step.Listen)
        private set
    var calibrated by mutableStateOf(false)
        private set
    var isPlayingReciter by mutableStateOf(false)
        private set
    var result: ChantScorer.Result? by mutableStateOf(null)
        private set
    var lastTake: ChantListener.Take? by mutableStateOf(null)
        private set
    var message: String? by mutableStateOf(null)
        private set
    /** From the mic tap until the take is scored. The screen treats this as listening.
     *  Nothing can change the step under a take that is still starting. */
    var isRecording by mutableStateOf(false)
        private set

    val listener = ChantListener(context)

    /** The "Not checked" note shows until a lesson has shown it once (spec 6.4). */
    private val prefs = context.getSharedPreferences("gitavani_progress", Context.MODE_PRIVATE)
    val showNotCheckedNote = !prefs.getBoolean("learnSeenNotCheckedNote", false)
    var notCheckedNoteShown = false

    /** Parts the phone can't hear even from the reciter, per line (spec D9). */
    private var notChecked by mutableStateOf<Map<Int, Set<Int>>>(emptyMap())
    private var calibration: Job? = null
    private var reciter: Job? = null

    /** Lines practised one by one. Speaker lines ("śrībhagavān uvāca") are shown but not practised. */
    val practiceLines: List<Int> = words.lines.indices.filter { !words.lines[it].speaker && words.lines[it].parts.isNotEmpty() }

    // ---------------------------------------------------------------------------------------------
    // Setup (spec 6.2)
    // ---------------------------------------------------------------------------------------------

    fun open() {
        scope.launch { continueSetup() }
    }

    fun close() {
        if (notCheckedNoteShown) prefs.edit().putBoolean("learnSeenNotCheckedNote", true).apply()
        calibration?.cancel()
        listener.close()
        audio.stop()
    }

    private suspend fun continueSetup() {
        if (!ChantListener.micAllowed(context)) {
            setup = Setup.NeedsMic
            return
        }
        var state = listener.modelState()
        if (state == ChantListener.ModelState.DOWNLOADING) {
            // Android is still fetching the model. Check every few seconds for up to 5 minutes.
            setup = Setup.Downloading(null)
            repeat(100) {
                if (state != ChantListener.ModelState.DOWNLOADING) return@repeat
                delay(3000)
                state = listener.modelState()
            }
        }
        setup = when (state) {
            ChantListener.ModelState.INSTALLED -> Setup.Ready
            ChantListener.ModelState.DOWNLOADING ->
                Setup.Failed("Android is still downloading the Hindi speech model. Try again in a few minutes.")
            ChantListener.ModelState.NEEDS_DOWNLOAD -> Setup.NeedsModel
            ChantListener.ModelState.UNSUPPORTED -> Setup.Failed("Learn isn't available on this device.")
            ChantListener.ModelState.NOT_ANSWERING ->
                Setup.Failed("This device's speech recognition isn't answering. Try again in a minute, or restart the device.")
        }
        if (setup == Setup.Ready) startCalibration()
    }

    /** Back from Settings or another app: the microphone may have been turned on meanwhile. */
    fun resumed() {
        if ((setup == Setup.MicDenied || setup == Setup.NeedsMic) && ChantListener.micAllowed(context)) {
            setup = Setup.Checking
            scope.launch { continueSetup() }
        }
    }

    /** Called with the answer to the system's microphone prompt. */
    fun micAnswered(granted: Boolean) {
        if (granted) scope.launch { continueSetup() } else setup = Setup.MicDenied
    }

    fun downloadModel() {
        setup = Setup.Downloading(0)
        scope.launch {
            val state = listener.downloadModel { setup = Setup.Downloading(it) }
            when (state) {
                ChantListener.ModelState.INSTALLED -> continueSetup()
                ChantListener.ModelState.DOWNLOADING ->
                    setup = Setup.Failed("Android is still downloading the Hindi speech model. Try again in a few minutes.")
                else -> setup = Setup.Failed("The download didn't finish. Check your connection and try again.")
            }
        }
    }

    fun retrySetup() {
        setup = Setup.Checking
        scope.launch { continueSetup() }
    }

    // ---------------------------------------------------------------------------------------------
    // Calibration (spec D9)
    // ---------------------------------------------------------------------------------------------

    private fun startCalibration() {
        if (calibration != null) return
        calibration = scope.launch {
            val found = mutableMapOf<Int, Set<Int>>()
            for (i in practiceLines) {
                val line = words.lines[i]
                val span = line.audio ?: continue
                try {
                    // Fed as fast as the recognizer reads: the Pixel 8a self-test gave the same marks
                    // as real-time pacing, about 3.5 times sooner.
                    val heard = listener.transcribeAsset("audio/$verseId.mp3", span[0], span[1], realTime = false)
                    found[i] = ChantScorer.notChecked(line.scoringLine, heard)
                } catch (e: IllegalStateException) {
                    message = "Something went wrong while getting ready. Close the lesson and try again."
                    return@launch
                }
            }
            notChecked = found
            calibrated = true
        }
    }

    /** "n of m words checked" (spec 6.3, review R13). */
    val checkedCount: Pair<Int, Int>
        get() {
            val total = practiceLines.sumOf { words.lines[it].parts.size }
            val unchecked = practiceLines.sumOf { notChecked[it]?.size ?: 0 }
            return (total - unchecked) to total
        }

    // ---------------------------------------------------------------------------------------------
    // What the current step checks
    // ---------------------------------------------------------------------------------------------

    val stepLines: List<Int>
        get() = when (val s = step) {
            is Step.Line -> listOf(practiceLines[s.n])
            else -> practiceLines
        }

    val stepParts: List<Part>
        get() = stepLines.flatMap { i -> words.lines[i].parts.map { Part(i, it) } }

    private fun stepScoring(): Pair<ChantScorer.ScoringLine, Set<Int>> {
        var offset = 0
        val skip = mutableSetOf<Int>()
        for (i in stepLines) {
            notChecked[i]?.forEach { skip.add(it + offset) }
            offset += words.lines[i].parts.size
        }
        val joined = ChantScorer.joinLines(stepLines.map { words.lines[it].scoringLine to false })
        return joined to skip
    }

    /** True when every part the phone can check was Right (and at least one was). */
    val allRight: Boolean
        get() = result?.marks?.let { ChantScorer.Mark.RIGHT in it && ChantScorer.Mark.SLIPPED !in it } == true

    // ---------------------------------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------------------------------

    /** Listen: the reciter chants the whole verse while calibration runs. */
    fun playVerse() {
        val from = practiceLines.firstOrNull()?.let { words.lines[it].audio?.first() } ?: return
        val to = practiceLines.lastOrNull()?.let { words.lines[it].audio?.last() } ?: return
        play(from, to)
    }

    /** The reciter's audio for the current step (one line, or all of them). */
    fun playReciter() {
        val spans = stepLines.mapNotNull { words.lines[it].audio }
        val from = spans.firstOrNull()?.first() ?: return
        val to = spans.lastOrNull()?.last() ?: return
        play(from, to)
    }

    /** Replays the line that holds a part of the current step (the "Listen again to" tip). */
    fun playLine(holding: Int) {
        val span = stepParts.getOrNull(holding)?.let { words.lines[it.line].audio } ?: return
        play(span[0], span[1])
    }

    private fun play(from: Double, to: Double) {
        listener.stopPlayback()
        reciter?.cancel()
        reciter = scope.launch {
            isPlayingReciter = true
            try {
                audio.playLine(verseId, from, to)
            } finally {
                isPlayingReciter = false
            }
        }
    }

    fun stopReciter() {
        reciter?.cancel()
        audio.stop()
        isPlayingReciter = false
    }

    fun begin() {
        stopReciter()
        go(Step.Line(0))
    }

    fun next() {
        when (val s = step) {
            Step.Listen -> begin()
            is Step.Line -> go(
                when {
                    s.n + 1 < practiceLines.size -> Step.Line(s.n + 1)
                    practiceLines.size > 1 -> Step.Join
                    else -> Step.Memory(hints = true)
                }
            )
            Step.Join -> go(Step.Memory(hints = true))
            is Step.Memory -> if (s.hints) go(Step.Memory(hints = false))
            Step.Learned -> Unit
        }
    }

    fun tryAgain() {
        listener.stopPlayback()
        result = null
        lastTake = null
    }

    private fun go(next: Step) {
        listener.stopPlayback()
        result = null
        lastTake = null
        message = null
        step = next
        if (next is Step.Line) playReciter()          // each line starts with the reciter (spec 6.3)
    }

    /** The mic button: start a take, or end the one in progress. */
    fun micTapped() {
        if (isRecording) {
            listener.stop()
            return
        }
        isRecording = true
        stopReciter()
        scope.launch {
            try {
                record()
            } finally {
                isRecording = false
            }
        }
    }

    private suspend fun record() {
        progress.markLearning(verseId)
        result = null
        message = null
        val (line, skip) = stepScoring()
        val fromMemory = step is Step.Memory
        try {
            val take = listener.take(line, skip, stopsOnSilence = !fromMemory)
            if (take == null) {
                message = "That take was stopped. Try again when you're ready."
                return
            }
            lastTake = take
            result = ChantScorer.scoreLine(line, take.heard, skip)
            if (step == Step.Memory(hints = false) && allRight) {
                progress.markLearned(verseId)
                step = Step.Learned
            }
        } catch (e: IllegalStateException) {
            message = "Something went wrong while listening. Try again in a moment."
        }
    }

    fun hearMyTry() {
        val take = lastTake ?: return
        stopReciter()
        scope.launch { listener.play(take) }
    }

    /** The lesson stops when the app goes to the background (spec 6.7). */
    fun appLeftForeground() {
        listener.discard()
        listener.stopPlayback()
        stopReciter()
    }
}

/** Keeps a lesson alive across rotation and other configuration changes. Closed when the screen leaves. */
@RequiresApi(33)
class LessonHolder(
    context: Context,
    verseId: String,
    words: VerseWords,
    audio: AudioService,
    progress: LearnProgress,
) : ViewModel() {
    val lesson = LessonState(context, verseId, words, audio, progress, viewModelScope)

    init {
        lesson.open()
    }

    override fun onCleared() = lesson.close()
}
