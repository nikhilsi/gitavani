import AVFoundation
import Speech

/// Learn's listening (docs/chant-coach-spec.md, sections 6.2, 6.3 and 7.1).
///
/// Records the learner with AVAudioEngine and streams the audio to Apple's on-device recognizer
/// (SpeechAnalyzer with SpeechTranscriber, hi-IN). Audio never leaves the phone. The reciter's lines go
/// through the same path for calibration (spec D9). The phone judges both voices the same way.
@available(iOS 27.0, *)
@Observable
final class ChantListener {
    enum Phase { case idle, starting, listening, finishing }

    /// One recorded try: what the recognizer heard, plus the audio for "Hear my try".
    struct Take {
        let heard: String
        fileprivate let audio: [AVAudioPCMBuffer]
    }

    enum Failure: Error { case noMicrophone, recognizerStalled }

    // Stop rules (spec 6.3).
    static let endPause = 1.5        // stop this long after the last expected word is heard
    static let silenceLimit = 4.0    // stop after this long with nothing new heard
    static let longestTake = 120.0   // safety stop for a take nobody ends

    private(set) var phase = Phase.idle
    /// What the recognizer has heard so far in the current take.
    private(set) var heardSoFar = ""
    private(set) var isPlayingTake = false

    let locale: Locale

    private var stopRequest: Bool?            // true keeps the take, false discards it
    private var lastChange = Date.now
    private var endHeardAt: Date?
    private var scoring: (line: ChantScorer.ScoringLine, notChecked: Set<Int>)?
    private var playback: (engine: AVAudioEngine, task: Task<Void, Never>)?
    private var observers: [NSObjectProtocol] = []

    init(locale: Locale) {
        self.locale = locale
        let center = NotificationCenter.default
        // A call, Siri or an alarm, or headphones coming or going, ends the take without scoring it (spec 6.7).
        observers.append(center.addObserver(forName: AVAudioSession.interruptionNotification, object: nil, queue: .main) {
            [weak self] _ in MainActor.assumeIsolated { self?.discard() }
        })
        observers.append(center.addObserver(forName: AVAudioSession.routeChangeNotification, object: nil, queue: .main) {
            [weak self] note in
            let reason = (note.userInfo?[AVAudioSessionRouteChangeReasonKey] as? UInt).flatMap(AVAudioSession.RouteChangeReason.init)
            guard reason == .newDeviceAvailable || reason == .oldDeviceUnavailable else { return }
            MainActor.assumeIsolated { self?.discard() }
        })
    }

    /// Call when the lesson closes.
    func close() {
        discard()
        stopPlayback()
        observers.forEach(NotificationCenter.default.removeObserver)
        observers = []
    }

    // MARK: - Device checks (spec 6.1, 6.2)

    /// Whether to show the Learn button. Asked once per launch.
    static func deviceCanLearn() async -> Bool {
        if let known = canLearn { return known }
        let can = await supportedLocale() != nil
        canLearn = can
        return can
    }
    private static var canLearn: Bool?

    /// The recognizer's locale for Learn, or nil when this device can't do it.
    static func supportedLocale() async -> Locale? {
        guard SpeechTranscriber.isAvailable else { return nil }
        return await SpeechTranscriber.supportedLocale(equivalentTo: Locale(identifier: "hi-IN"))
    }

    /// True when the Hindi speech model is on the phone. The system can remove it: check at every lesson open.
    func modelInstalled() async -> Bool {
        await AssetInventory.status(forModules: [RecognizerSession.makeTranscriber(locale)]) == .installed
    }

    /// Downloads the speech model from Apple. `progress` gets 0 to 1.
    func installModel(progress: @escaping (Double) -> Void) async throws {
        let modules = [RecognizerSession.makeTranscriber(locale)]
        guard let request = try await AssetInventory.assetInstallationRequest(supporting: modules) else { return }
        let watcher = Task {
            while !Task.isCancelled {
                progress(request.progress.fractionCompleted)
                try? await Task.sleep(for: .milliseconds(250))
            }
        }
        defer { watcher.cancel() }
        try await request.downloadAndInstall()
        progress(1)
    }

    static var micAllowed: Bool { AVAudioApplication.shared.recordPermission == .granted }
    static var micDenied: Bool { AVAudioApplication.shared.recordPermission == .denied }
    static func askForMic() async -> Bool { await AVAudioApplication.requestRecordPermission() }

    // MARK: - Calibration (spec D9)

    /// Runs a stretch of a recording through the recognizer, fed in the same chunks as a live take.
    func transcribe(file url: URL, from start: Double = 0, to end: Double? = nil) async throws -> String {
        let file = try AVAudioFile(forReading: url)
        let format = file.processingFormat
        let first = AVAudioFramePosition(max(0, start) * format.sampleRate)
        let last = min(file.length, end.map { AVAudioFramePosition($0 * format.sampleRate) } ?? file.length)
        file.framePosition = first
        let session = try await RecognizerSession(locale: locale)
        var left = AVAudioFrameCount(max(0, last - first))
        while left > 0 {
            let count = min(4096, left)
            guard let buffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: count) else { break }
            try file.read(into: buffer, frameCount: count)
            if buffer.frameLength == 0 { break }
            session.feed(buffer)
            left -= min(left, buffer.frameLength)
        }
        return try await session.finish()
    }

    // MARK: - Takes

    /// Records one take and returns what was heard. Returns nil when the take was discarded.
    /// - Parameters:
    ///   - line: the expected line (or joined lines), used to stop soon after its last word.
    ///   - stopsOnSilence: false in From memory, where a learner may pause to recall.
    func take(line: ChantScorer.ScoringLine, notChecked: Set<Int>, stopsOnSilence: Bool) async throws -> Take? {
        guard phase == .idle else { return nil }
        stopPlayback()
        phase = .starting
        heardSoFar = ""
        stopRequest = nil
        endHeardAt = nil
        scoring = (line, notChecked)
        defer {
            phase = .idle
            scoring = nil
        }

        let session = try await RecognizerSession(locale: locale, keepsAudio: true) { text in
            Task { @MainActor in self.heard(text) }
        }
        let engine = AVAudioEngine()
        let input = engine.inputNode
        let format = input.outputFormat(forBus: 0)
        guard format.sampleRate > 0, format.channelCount > 0 else {
            await session.cancel()
            throw Failure.noMicrophone
        }
        input.installTap(onBus: 0, bufferSize: 4096, format: format, block: Self.tap(feeding: session))
        let configChange = NotificationCenter.default.addObserver(
            forName: .AVAudioEngineConfigurationChange, object: engine, queue: .main
        ) { [weak self] _ in MainActor.assumeIsolated { self?.discard() } }
        defer { NotificationCenter.default.removeObserver(configChange) }
        do {
            engine.prepare()
            try engine.start()
        } catch {
            input.removeTap(onBus: 0)
            await session.cancel()
            throw error
        }

        let started = Date.now
        lastChange = started
        if stopRequest == nil { phase = .listening }
        var keep = true
        while true {
            try? await Task.sleep(for: .milliseconds(100))
            if Task.isCancelled { keep = false; break }
            if let request = stopRequest { keep = request; break }
            let now = Date.now
            if let end = endHeardAt, now.timeIntervalSince(end) >= Self.endPause { break }
            if stopsOnSilence, now.timeIntervalSince(lastChange) >= Self.silenceLimit { break }
            if now.timeIntervalSince(started) >= Self.longestTake { break }
        }

        phase = .finishing
        input.removeTap(onBus: 0)
        engine.stop()
        guard keep else {
            await session.cancel()
            return nil
        }
        return Take(heard: try await session.finish(), audio: session.audio)
    }

    /// The learner's second tap on the mic: end the take and score it.
    func stop() {
        if phase == .starting || phase == .listening { stopRequest = stopRequest ?? true }
    }

    /// End the take without scoring it (interruptions, route changes, the app going to the background).
    func discard() {
        if phase == .starting || phase == .listening { stopRequest = false }
    }

    private func heard(_ text: String) {
        guard phase == .listening, text != heardSoFar else { return }
        heardSoFar = text
        lastChange = .now
        if endHeardAt == nil, let scoring, Self.reachedEnd(scoring.line, heard: text, notChecked: scoring.notChecked) {
            endHeardAt = .now
        }
    }

    /// True when the last part that gets checked is already Right in what was heard so far.
    static func reachedEnd(_ line: ChantScorer.ScoringLine, heard: String, notChecked: Set<Int>) -> Bool {
        let result = ChantScorer.scoreLine(line, heard: heard, notChecked: notChecked)
        guard let last = result.marks.lastIndex(where: { $0 != .notChecked }) else { return false }
        return result.marks[last] == .right
    }

    /// Built outside the main actor: the engine calls the tap on its own audio thread.
    nonisolated private static func tap(feeding session: RecognizerSession) -> AVAudioNodeTapBlock {
        { buffer, _ in session.feed(buffer) }
    }

    // MARK: - Hear my try

    func play(_ take: Take) {
        stopPlayback()
        guard let format = take.audio.first?.format else { return }
        let engine = AVAudioEngine()
        let node = AVAudioPlayerNode()
        engine.attach(node)
        engine.connect(node, to: engine.mainMixerNode, format: format)
        for buffer in take.audio.dropLast() { node.scheduleBuffer(buffer) }
        do {
            try engine.start()
        } catch {
            return
        }
        node.play()
        isPlayingTake = true
        let task = Task { [weak self] in
            if let last = take.audio.last { _ = await node.scheduleBuffer(last, completionCallbackType: .dataPlayedBack) }
            guard !Task.isCancelled else { return }
            self?.stopPlayback()
        }
        playback = (engine, task)
    }

    func stopPlayback() {
        playback?.task.cancel()
        playback?.engine.stop()
        playback = nil
        isPlayingTake = false
    }
}

/// One recognizer run: audio in, text out. Each take and each calibration line gets a fresh one,
/// released after use (spec 7.1). `feed` is called from the audio thread, the rest from the main actor.
@available(iOS 27.0, *)
nonisolated final class RecognizerSession: @unchecked Sendable {
    static func makeTranscriber(_ locale: Locale) -> SpeechTranscriber {
        SpeechTranscriber(locale: locale, transcriptionOptions: [], reportingOptions: [.volatileResults], attributeOptions: [])
    }

    private let analyzer: SpeechAnalyzer
    private let converter: AnalyzerInputConverter
    private let input: AsyncStream<AnalyzerInput>.Continuation
    private let results: Task<String, Error>
    private let keepsAudio: Bool
    private let lock = NSLock()
    private var kept: [AVAudioPCMBuffer] = []
    private var closed = false
    private var latest = ""             // everything heard so far, for when the recognizer stalls
    private var answered = false        // the caller of finish() has its answer

    /// - Parameter onText: called with everything heard so far, each time the recognizer's guess changes.
    init(locale: Locale, keepsAudio: Bool = false, onText: (@Sendable (String) -> Void)? = nil) async throws {
        let transcriber = Self.makeTranscriber(locale)
        let modules: [any SpeechModule] = [transcriber]
        analyzer = SpeechAnalyzer(modules: modules)
        converter = try await AnalyzerInputConverter.converter(compatibleWith: modules)
        self.keepsAudio = keepsAudio
        let (stream, continuation) = AsyncStream.makeStream(of: AnalyzerInput.self)
        input = continuation
        let box = Box()
        results = Task {
            var finals: [String] = []
            var guess = ""
            for try await result in transcriber.results {
                let text = String(result.text.characters).trimmingCharacters(in: .whitespaces)
                if result.isFinal {
                    if !text.isEmpty { finals.append(text) }
                    guess = ""
                } else {
                    guess = text
                }
                let soFar = (finals + [guess]).filter { !$0.isEmpty }.joined(separator: " ")
                box.session?.remember(soFar)
                onText?(soFar)
            }
            return finals.joined(separator: " ")
        }
        box.session = self
        try await analyzer.prepareToAnalyze(in: await SpeechAnalyzer.bestAvailableAudioFormat(compatibleWith: modules))
        try await analyzer.start(inputSequence: stream)
    }

    /// Converts and queues one buffer. The buffer is copied first, since the caller may reuse it.
    func feed(_ buffer: AVAudioPCMBuffer) {
        guard let copy = buffer.copy() as? AVAudioPCMBuffer else { return }
        lock.lock()
        defer { lock.unlock() }
        guard !closed else { return }
        if keepsAudio { kept.append(copy) }
        do {
            for item in try converter.convert(copy, at: nil) { input.yield(item) }
        } catch {
            #if DEBUG
            print("RecognizerSession: convert failed: \(error)")
            #endif
        }
    }

    /// Ends the input and returns the final transcript. Apple's recognizer can stall (the Mac tool
    /// hung once in a few thousand runs). After `timeout` this keeps what was heard so far. It
    /// throws if nothing was. A stalled task is left behind rather than waited on.
    func finish(timeout: Duration = .seconds(10)) async throws -> String {
        let tail = lock.withLock {
            closed = true
            return (try? converter.flush()) ?? []
        }
        for item in tail { input.yield(item) }
        input.finish()
        return try await withCheckedThrowingContinuation { (answer: CheckedContinuation<String, Error>) in
            Task {
                do {
                    try await analyzer.finalizeAndFinishThroughEndOfInput()
                    let text = try await results.value
                    if claimAnswer() { answer.resume(returning: text) }
                } catch {
                    if claimAnswer() { answer.resume(throwing: error) }
                }
            }
            Task {
                try? await Task.sleep(for: timeout)
                guard claimAnswer() else { return }
                let soFar = lock.withLock { latest }
                if soFar.isEmpty {
                    answer.resume(throwing: ChantListener.Failure.recognizerStalled)
                } else {
                    answer.resume(returning: soFar)
                }
            }
        }
    }

    private func claimAnswer() -> Bool {
        lock.withLock {
            if answered { return false }
            answered = true
            return true
        }
    }

    fileprivate func remember(_ text: String) {
        lock.withLock { latest = text }
    }

    /// Lets the results task reach the session once it exists.
    private final class Box: @unchecked Sendable { weak var session: RecognizerSession? }

    func cancel() async {
        lock.withLock { closed = true }
        input.finish()
        await analyzer.cancelAndFinishNow()
        results.cancel()
    }

    var audio: [AVAudioPCMBuffer] { lock.withLock { kept } }
}
