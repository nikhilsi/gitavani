package com.nikhilsi.gitavani.learn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.ModelDownloadListener
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Learn's listening (docs/chant-coach-spec.md, sections 6.2, 6.3 and 7.2).
 *
 * Records the learner at 16 kHz and feeds the audio through a pipe to Android's on-device recognizer
 * (hi-IN). Audio never leaves the phone. The reciter's lines go through the same pipe for calibration
 * (spec D9). The recognizer writes Devanagari; results come back in Roman letters for the scorer.
 *
 * The Pixel 8a trial showed the on-device recognizer allows one session at a time. One listener
 * (and one recognizer) serves the whole lesson. Create it and call every method on the main thread.
 */
@RequiresApi(33)
class ChantListener(private val context: Context) {

    enum class Phase { IDLE, STARTING, LISTENING, FINISHING }
    enum class ModelState { INSTALLED, DOWNLOADING, NEEDS_DOWNLOAD, UNSUPPORTED, NOT_ANSWERING }

    /** One recorded try: what the recognizer heard (Roman letters), plus the audio for "Hear my try". */
    class Take(val heard: String, internal val pcm: ShortArray)

    companion object {
        const val LANGUAGE = "hi-IN"
        const val SAMPLE_RATE = 16000

        // Stop rules (spec 6.3).
        const val END_PAUSE_MS = 1500L         // stop this long after the last expected word is heard
        const val SILENCE_LIMIT_MS = 4000L     // stop after this long with nothing new heard
        const val LONGEST_TAKE_MS = 120_000L   // safety stop for a take nobody ends
        private const val STALL_MS = 5000L     // end a session after this long with no sign of life

        fun micAllowed(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        /** True when the last part that gets checked is already Right in what was heard so far. */
        fun reachedEnd(line: ChantScorer.ScoringLine, heard: String, notChecked: Set<Int>): Boolean {
            val result = ChantScorer.scoreLine(line, heard, notChecked)
            val last = result.marks.indexOfLast { it != ChantScorer.Mark.NOT_CHECKED }
            return last >= 0 && result.marks[last] == ChantScorer.Mark.RIGHT
        }
    }

    private val _phase = MutableStateFlow(Phase.IDLE)
    val phase: StateFlow<Phase> = _phase
    private val _heardSoFar = MutableStateFlow("")
    /** What the recognizer has heard so far in the current take. */
    val heardSoFar: StateFlow<String> = _heardSoFar
    private val _isPlayingTake = MutableStateFlow(false)
    val isPlayingTake: StateFlow<Boolean> = _isPlayingTake

    private val main = ContextCompat.getMainExecutor(context)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val recognizer: SpeechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
    private var stopRequest: Boolean? = null        // true keeps the take, false discards it
    private var player: AudioTrack? = null

    /** Call when the lesson closes. */
    fun close() {
        discard()
        stopPlayback()
        recognizer.destroy()
    }

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }

    // ---------------------------------------------------------------------------------------------
    // Device checks (spec 6.1, 6.2)
    // ---------------------------------------------------------------------------------------------

    /**
     * Is the Hindi model on the phone? The system can remove it. Check at every lesson open.
     * The Pixel 8a's speech service once stopped answering altogether. The check gives up after 10 s.
     */
    suspend fun modelState(): ModelState = withTimeoutOrNull(10_000) { supportCheck() } ?: ModelState.NOT_ANSWERING

    private suspend fun supportCheck(): ModelState = suspendCancellableCoroutine { cont ->
        recognizer.checkRecognitionSupport(intent(), main, object : RecognitionSupportCallback {
            override fun onSupportResult(support: RecognitionSupport) {
                fun has(list: List<String>) = list.any { it.equals(LANGUAGE, ignoreCase = true) }
                val state = when {
                    has(support.installedOnDeviceLanguages) -> ModelState.INSTALLED
                    has(support.pendingOnDeviceLanguages) -> ModelState.DOWNLOADING
                    has(support.supportedOnDeviceLanguages) -> ModelState.NEEDS_DOWNLOAD
                    else -> ModelState.UNSUPPORTED
                }
                if (cont.isActive) cont.resume(state)
            }

            override fun onError(error: Int) {
                if (cont.isActive) cont.resume(ModelState.UNSUPPORTED)
            }
        })
    }

    /**
     * Asks Android for the Hindi model. Android shows its own approval prompt. [onProgress] gets 0 to 100
     * on Android 14 and later. Returns the model state when the download ends or is left to finish later.
     */
    suspend fun downloadModel(onProgress: (Int) -> Unit): ModelState {
        if (Build.VERSION.SDK_INT >= 34) {
            val done = suspendCancellableCoroutine { cont ->
                recognizer.triggerModelDownload(intent(), main, object : ModelDownloadListener {
                    override fun onProgress(completedPercent: Int) = onProgress(completedPercent)
                    override fun onSuccess() { if (cont.isActive) cont.resume(ModelState.INSTALLED) }
                    override fun onScheduled() { if (cont.isActive) cont.resume(ModelState.DOWNLOADING) }
                    override fun onError(error: Int) { if (cont.isActive) cont.resume(ModelState.NEEDS_DOWNLOAD) }
                })
            }
            return done
        }
        recognizer.triggerModelDownload(intent())
        repeat(150) {                       // Android 13 gives no progress: check every 2 s for 5 minutes
            delay(2000)
            val state = modelState()
            if (state == ModelState.INSTALLED) return state
        }
        return modelState()
    }

    // ---------------------------------------------------------------------------------------------
    // Recognizer sessions
    // ---------------------------------------------------------------------------------------------

    /**
     * One recognizer session over a pipe. [feed] runs on its own thread, writes 16 kHz mono 16-bit PCM,
     * and returns when the audio ends or when the flag it gets is set (the session is over). [onText]
     * gets everything heard so far, in Roman letters, each time the guess changes. Returns the final text.
     *
     * A watchdog ends the session when nothing has moved for [STALL_MS]: no audio taken in and no
     * callback. The Pixel 8a showed two ways the recognizer can stop answering. It may finish the audio
     * and never report the end ("Result lattice is not set"); then what was heard is kept, including
     * the last partial guess. Or it may stop reading audio altogether. Then nothing was heard. This
     * throws. A stuck recognizer never looks like a silent learner.
     */
    private suspend fun recognize(
        feed: (OutputStream, AtomicBoolean) -> Unit,
        onText: ((String) -> Unit)? = null,
    ): String {
        val (readEnd, writeEnd) = ParcelFileDescriptor.createPipe()
        val finals = mutableListOf<String>()
        var guess = ""
        val ended = CompletableDeferred<Int?>()          // null on a clean end, else the error code
        val lastSign = AtomicLong(System.currentTimeMillis())
        fun alive() = lastSign.set(System.currentTimeMillis())
        fun heard() = ChantScorer.devaToIast((finals + guess).filter { it.isNotBlank() }.joinToString(" "))
        fun first(b: Bundle?) = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(b: Bundle?) { alive(); guess = first(b); onText?.invoke(heard()) }
            override fun onSegmentResults(b: Bundle) {
                alive()
                first(b).takeIf { it.isNotEmpty() }?.let { finals.add(it) }
                guess = ""
                onText?.invoke(heard())
            }
            override fun onResults(b: Bundle?) {
                first(b).takeIf { it.isNotEmpty() }?.let { finals.add(it) }
                ended.complete(null)
            }
            override fun onEndOfSegmentedSession() { ended.complete(null) }
            override fun onError(error: Int) { ended.complete(error) }
            override fun onReadyForSpeech(p: Bundle?) = alive()
            override fun onBeginningOfSpeech() = alive()
            override fun onRmsChanged(v: Float) = alive()
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() = alive()
            override fun onEvent(t: Int, b: Bundle?) {}
        })
        recognizer.startListening(intent().apply {
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, onText != null)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readEnd)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
            // Segmented: the recognizer keeps listening through pauses until the pipe closes.
            putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
        })

        // A plain thread, not a coroutine: if the recognizer stops reading, the write blocks. The
        // lesson must not wait on it. Closing the write end tells the recognizer the audio has ended.
        val over = AtomicBoolean(false)
        Thread {
            ParcelFileDescriptor.AutoCloseOutputStream(writeEnd).use { raw ->
                val out = object : OutputStream() {
                    override fun write(b: Int) { raw.write(b); alive() }
                    override fun write(b: ByteArray, off: Int, len: Int) { raw.write(b, off, len); alive() }
                }
                runCatching { feed(out, over) }
            }
        }.apply { isDaemon = true; start() }

        try {
            while (!ended.isCompleted && System.currentTimeMillis() - lastSign.get() < STALL_MS) delay(200)
            if (!ended.isCompleted) {
                val text = heard()
                if (text.isBlank()) throw IllegalStateException("recognizer stopped answering")
                return text
            }
            val error = ended.await()
            if (error != null && finals.isEmpty() && error != SpeechRecognizer.ERROR_NO_MATCH &&
                error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                throw IllegalStateException("recognizer error $error")
            }
            return ChantScorer.devaToIast(finals.joinToString(" "))
        } finally {
            if (!ended.isCompleted) recognizer.cancel()   // cancelled: the recognizer sends no more callbacks
            over.set(true)
            readEnd.close()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Calibration (spec D9)
    // ---------------------------------------------------------------------------------------------

    /**
     * Runs a stretch of a reciter recording (an asset such as "audio/BG4.7.mp3") through the recognizer,
     * paced like a live take. Returns the text in Roman letters.
     */
    suspend fun transcribeAsset(assetPath: String, fromSec: Double, toSec: Double, realTime: Boolean = true): String {
        val pcm = withContext(Dispatchers.Default) { decodeTo16kMono(context, assetPath, fromSec, toSec) }
        return transcribe(pcm, realTime)
    }

    /** Runs 16 kHz mono PCM through the recognizer, in 100 ms chunks. */
    suspend fun transcribe(pcm: ShortArray, realTime: Boolean = true): String =
        recognize(feed = { out, over ->
            val chunk = SAMPLE_RATE / 10
            var i = 0
            while (i < pcm.size && !over.get()) {
                val n = minOf(chunk, pcm.size - i)
                out.write(toBytes(pcm, i, n))
                i += n
                if (realTime) Thread.sleep(100)
            }
        })

    // ---------------------------------------------------------------------------------------------
    // Takes
    // ---------------------------------------------------------------------------------------------

    /**
     * Records one take and returns what was heard. Returns null when the take was discarded.
     * [line] is the expected line (or joined lines), used to stop soon after its last word.
     * [stopsOnSilence] is false in From memory, where a learner may pause to recall.
     */
    suspend fun take(line: ChantScorer.ScoringLine, notChecked: Set<Int>, stopsOnSilence: Boolean): Take? {
        if (_phase.value != Phase.IDLE || !micAllowed(context)) return null
        stopPlayback()
        _phase.value = Phase.STARTING
        _heardSoFar.value = ""
        stopRequest = null
        var lastChange = System.currentTimeMillis()
        var endHeardAt: Long? = null
        val recorded = PcmBuffer()
        val stopFeed = AtomicBoolean(false)

        // A call, an alarm or another app taking the audio ends the take without scoring it (spec 6.7).
        val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT).build())
            .setOnAudioFocusChangeListener({ change -> if (change < 0) discard() }, Handler(Looper.getMainLooper()))
            .build()
        audioManager.requestAudioFocus(focus)
        // Headphones coming or going ends it too. The callback first reports the devices already present.
        val present = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS or AudioManager.GET_DEVICES_OUTPUTS).map { it.id }.toSet()
        val devices = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) {
                if (added.any { it.id !in present }) discard()
            }
            override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) = discard()
        }
        audioManager.registerAudioDeviceCallback(devices, Handler(Looper.getMainLooper()))

        try {
            return coroutineScope {
                // TalkBack announces the mic button first. Give it a moment so the take doesn't record it (spec 6.7).
                if (context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled == true) delay(1000)
                var heard: String? = null
                var failure: Exception? = null
                val session = launch {
                    try {
                        heard = recognize(
                            feed = { out, over -> record(out, recorded, stopFeed, over) },
                            onText = { text ->
                                if (_phase.value == Phase.LISTENING && text != _heardSoFar.value) {
                                    _heardSoFar.value = text
                                    lastChange = System.currentTimeMillis()
                                    if (endHeardAt == null && reachedEnd(line, text, notChecked)) endHeardAt = lastChange
                                }
                            },
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        failure = e
                    }
                }
                val started = System.currentTimeMillis()
                if (stopRequest == null) _phase.value = Phase.LISTENING
                var keep = true
                while (isActive && !session.isCompleted) {
                    delay(100)
                    stopRequest?.let { keep = it }
                    if (stopRequest != null) break
                    val now = System.currentTimeMillis()
                    if (endHeardAt?.let { now - it >= END_PAUSE_MS } == true) break
                    if (stopsOnSilence && now - lastChange >= SILENCE_LIMIT_MS) break
                    if (now - started >= LONGEST_TAKE_MS) break
                }
                _phase.value = Phase.FINISHING
                stopFeed.set(true)           // closing the pipe ends the session
                if (!keep) {
                    session.cancel()
                    return@coroutineScope null
                }
                session.join()
                failure?.let { throw it }
                heard?.let { Take(it, recorded.toArray()) }
            }
        } finally {
            audioManager.unregisterAudioDeviceCallback(devices)
            audioManager.abandonAudioFocusRequest(focus)
            _phase.value = Phase.IDLE
        }
    }

    /** The learner's second tap on the mic: end the take and score it. */
    fun stop() {
        if (_phase.value == Phase.STARTING || _phase.value == Phase.LISTENING) stopRequest = stopRequest ?: true
    }

    /** End the take without scoring it (interruptions, device changes, the app going to the background). */
    fun discard() {
        if (_phase.value == Phase.STARTING || _phase.value == Phase.LISTENING) stopRequest = false
    }

    /**
     * Records the mic and writes it to the pipe until [stop] or [over] is set. Runs on the session's
     * feeding thread. The mic is read on a thread of its own. A recognizer that stops reading
     * (a blocked write) then can't keep the microphone open. The mic is released when the take ends.
     */
    private fun record(out: OutputStream, recorded: PcmBuffer, stop: AtomicBoolean, over: AtomicBoolean) {
        val chunk = SAMPLE_RATE / 10
        val queue = LinkedBlockingQueue<ShortArray>(300)        // 30 s of audio waiting for the pipe
        val reader = Thread {
            val size = maxOf(AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT), chunk * 4)
            val rec = try {
                // VOICE_RECOGNITION: the source Android tunes for speech recognizers.
                AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, size)
            } catch (e: SecurityException) {
                return@Thread
            }
            try {
                if (rec.state != AudioRecord.STATE_INITIALIZED) return@Thread
                rec.startRecording()
                val buf = ShortArray(chunk)
                while (!stop.get() && !over.get()) {
                    val n = rec.read(buf, 0, chunk)
                    if (n <= 0) break
                    recorded.append(buf, n)
                    queue.offer(buf.copyOf(n))
                }
            } finally {
                runCatching { rec.stop() }
                rec.release()
            }
        }.apply { isDaemon = true; start() }
        while (!over.get()) {
            val next = queue.poll(100, TimeUnit.MILLISECONDS)
            if (next != null) out.write(toBytes(next, 0, next.size))
            else if (!reader.isAlive) break
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Hear my try
    // ---------------------------------------------------------------------------------------------

    suspend fun play(take: Take) {
        stopPlayback()
        if (take.pcm.isEmpty()) return
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(take.pcm.size * 2)
            .build()
        track.write(take.pcm, 0, take.pcm.size)
        player = track
        _isPlayingTake.value = true
        track.play()
        while (player === track && track.playbackHeadPosition < take.pcm.size) delay(50)
        if (player === track) stopPlayback()
    }

    fun stopPlayback() {
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
        _isPlayingTake.value = false
    }

    private class PcmBuffer {
        private var data = ShortArray(SAMPLE_RATE * 10)
        private var size = 0

        @Synchronized fun append(src: ShortArray, n: Int) {
            if (size + n > data.size) data = data.copyOf(maxOf(data.size * 2, size + n))
            System.arraycopy(src, 0, data, size, n)
            size += n
        }

        @Synchronized fun toArray(): ShortArray = data.copyOf(size)
    }
}

/** Whether to show the Learn button: Android 13 or later (audio fed through a pipe, segmented sessions,
 *  the language check) with an on-device recognizer. Whether Hindi is installed is checked in the lesson. */
fun deviceCanLearn(context: Context): Boolean =
    Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

private fun toBytes(pcm: ShortArray, from: Int, count: Int): ByteArray {
    val bytes = ByteArray(count * 2)
    for (i in 0 until count) {
        val v = pcm[from + i].toInt()
        bytes[2 * i] = (v and 0xff).toByte()          // little-endian, as the recognizer expects
        bytes[2 * i + 1] = (v shr 8 and 0xff).toByte()
    }
    return bytes
}

/** Decodes part of an MP3 asset to 16 kHz mono 16-bit PCM. */
@RequiresApi(33)
internal fun decodeTo16kMono(context: Context, assetPath: String, fromSec: Double, toSec: Double): ShortArray {
    val extractor = MediaExtractor()
    context.assets.openFd(assetPath).use { extractor.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
    try {
        val track = (0 until extractor.trackCount).first {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        }
        val format = extractor.getTrackFormat(track)
        extractor.selectTrack(track)
        val fromUs = (fromSec * 1_000_000).toLong()
        val toUs = (toSec * 1_000_000).toLong()
        extractor.seekTo(fromUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        codec.configure(format, null, null, 0)
        codec.start()
        var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        var mono = FloatArray(rate * 20)
        var count = 0
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        try {
            while (!outputDone) {
                if (!inputDone) {
                    val i = codec.dequeueInputBuffer(10_000)
                    if (i >= 0) {
                        val n = extractor.readSampleData(codec.getInputBuffer(i)!!, 0)
                        if (n < 0 || extractor.sampleTime > toUs) {
                            codec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(i, 0, n, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val o = codec.dequeueOutputBuffer(info, 10_000)
                when {
                    o == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        rate = codec.outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channels = codec.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                    o >= 0 -> {
                        val shorts = codec.getOutputBuffer(o)!!.order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                        val frames = shorts.remaining() / channels
                        for (f in 0 until frames) {
                            val t = info.presentationTimeUs + f * 1_000_000L / rate
                            if (t < fromUs || t >= toUs) continue
                            var sum = 0f
                            for (c in 0 until channels) sum += shorts.get(f * channels + c)
                            if (count == mono.size) mono = mono.copyOf(mono.size * 2)
                            mono[count++] = sum / channels
                        }
                        codec.releaseOutputBuffer(o, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                }
            }
        } finally {
            codec.stop()
            codec.release()
        }
        return resample(mono.copyOf(count), rate, ChantListener.SAMPLE_RATE)
    }
    finally {
        extractor.release()
    }
}

/** Low-pass below the new rate's Nyquist (windowed sinc), then linear interpolation. */
internal fun resample(input: FloatArray, fromRate: Int, toRate: Int): ShortArray {
    if (input.isEmpty()) return ShortArray(0)
    val filtered = if (toRate < fromRate) {
        val cutoff = 0.45 * toRate / fromRate        // cycles per input sample
        val taps = 63
        val half = taps / 2
        val h = DoubleArray(taps) { i ->
            val n = i - half
            val sinc = if (n == 0) 2 * cutoff else sin(2 * PI * cutoff * n) / (PI * n)
            sinc * (0.54 - 0.46 * cos(2 * PI * i / (taps - 1)))
        }
        val sum = h.sum()
        FloatArray(input.size) { i ->
            var acc = 0.0
            for (k in 0 until taps) {
                val j = i + k - half
                if (j in input.indices) acc += input[j] * h[k]
            }
            (acc / sum).toFloat()
        }
    } else input
    val count = (input.size.toLong() * toRate / fromRate).toInt()
    val step = fromRate.toDouble() / toRate
    return ShortArray(count) { j ->
        val pos = j * step
        val i0 = floor(pos).toInt().coerceAtMost(filtered.size - 1)
        val i1 = (i0 + 1).coerceAtMost(filtered.size - 1)
        val frac = pos - i0
        (filtered[i0] * (1 - frac) + filtered[i1] * frac).roundToInt().coerceIn(-32768, 32767).toShort()
    }
}
