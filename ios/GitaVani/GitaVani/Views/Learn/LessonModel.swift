import Foundation

/// The lesson's state and steps (docs/chant-coach-spec.md, sections 6.2 to 6.6).
/// Listen, then each line, then Join (when the verse has more than one line), then From memory:
/// first with the first letter of each word, then with nothing shown. A clean take with nothing
/// shown marks the verse Learned.
@available(iOS 27.0, *)
@Observable
final class LessonModel {
    enum Setup: Equatable {
        case checking, needsMic, micDenied, needsModel, downloading(Double), failed(String), ready
    }

    enum Step: Equatable {
        case listen
        case line(Int)              // index into practiceLines
        case join
        case memory(hints: Bool)
        case learned
    }

    /// A word part shown in the current step, with where it came from.
    struct Part {
        let line: Int               // index into words.lines
        let part: WordPart
    }

    let verseId: String
    let words: VerseWords
    let audio: AudioService
    let progress: LearnProgress

    private(set) var setup = Setup.checking
    private(set) var step = Step.listen
    private(set) var calibrated = false
    private(set) var isPlayingReciter = false
    private(set) var result: ChantScorer.Result?
    private(set) var lastTake: ChantListener.Take?
    private(set) var message: String?
    private(set) var listener: ChantListener?
    /// From the mic tap until the take is scored. The screen treats this as listening.
    /// Nothing can change the step under a take that is still starting.
    private(set) var isRecording = false
    private var closed = false

    /// Parts the phone can't hear even from the reciter, per line (spec D9).
    private var notChecked: [Int: Set<Int>] = [:]
    private var calibration: Task<Void, Never>?

    init(verseId: String, words: VerseWords, audio: AudioService, progress: LearnProgress) {
        self.verseId = verseId
        self.words = words
        self.audio = audio
        self.progress = progress
    }

    /// Lines practised one by one. Speaker lines ("śrībhagavān uvāca") are shown but not practised.
    var practiceLines: [Int] {
        words.lines.indices.filter { !words.lines[$0].speaker && !words.lines[$0].parts.isEmpty }
    }

    // MARK: - Setup (spec 6.2)

    func open() async {
        audio.setLessonMode(true)
        let locale = await ChantListener.supportedLocale()
        guard !closed else { return }
        guard let locale else {
            setup = .failed("Learn isn't available on this device.")
            return
        }
        listener = ChantListener(locale: locale)
        await continueSetup()
    }

    func close() {
        closed = true
        calibration?.cancel()
        listener?.close()
        audio.stop()
        audio.setLessonMode(false)
    }

    private func continueSetup() async {
        guard let listener else { return }
        if !ChantListener.micAllowed {
            setup = ChantListener.micDenied ? .micDenied : .needsMic
            return
        }
        let installed = await listener.modelInstalled()
        guard !closed else { return }
        if !installed {
            setup = .needsModel
            return
        }
        setup = .ready
        startCalibration()
    }

    func allowMic() async {
        _ = await ChantListener.askForMic()
        await continueSetup()
    }

    func downloadModel() async {
        guard let listener else { return }
        setup = .downloading(0)
        do {
            try await listener.installModel { [weak self] fraction in self?.setup = .downloading(fraction) }
            guard !closed else { return }
            await continueSetup()
        } catch {
            setup = .failed("The download didn't finish. Check your connection and try again.")
        }
    }

    func retrySetup() async {
        setup = .checking
        await continueSetup()
    }

    // MARK: - Calibration (spec D9)

    private func startCalibration() {
        guard !closed, let listener, let url = Bundle.main.url(forResource: verseId, withExtension: "mp3") else { return }
        calibration = Task {
            for i in practiceLines {
                let line = words.lines[i]
                guard let span = line.audio, span.count == 2 else { continue }
                do {
                    let heard = try await listener.transcribe(file: url, from: span[0], to: span[1])
                    if Task.isCancelled { return }
                    notChecked[i] = ChantScorer.notChecked(line.scoringLine, heardFromReciter: heard)
                } catch {
                    if Task.isCancelled { return }
                    message = "Something went wrong while getting ready. Close the lesson and try again."
                    return
                }
            }
            calibrated = true
        }
    }

    /// "n of m words checked" (spec 6.3, review R13).
    var checkedCount: (checked: Int, total: Int) {
        let lines = practiceLines
        let total = lines.reduce(0) { $0 + words.lines[$1].parts.count }
        let unchecked = lines.reduce(0) { $0 + (notChecked[$1]?.count ?? 0) }
        return (total - unchecked, total)
    }

    // MARK: - What the current step checks

    /// The verse lines the current step covers.
    var stepLines: [Int] {
        switch step {
        case .line(let n): [practiceLines[n]]
        case .listen, .join, .memory, .learned: practiceLines
        }
    }

    var stepParts: [Part] {
        stepLines.flatMap { i in words.lines[i].parts.map { Part(line: i, part: $0) } }
    }

    private var stepScoring: (line: ChantScorer.ScoringLine, notChecked: Set<Int>) {
        var offset = 0
        var skip = Set<Int>()
        for i in stepLines {
            skip.formUnion((notChecked[i] ?? []).map { $0 + offset })
            offset += words.lines[i].parts.count
        }
        let joined = ChantScorer.joinLines(stepLines.map { (words.lines[$0].scoringLine, false) })
        return (joined, skip)
    }

    /// True when every part the phone can check was Right (and at least one was).
    var allRight: Bool {
        guard let result else { return false }
        return result.marks.contains(.right) && !result.marks.contains(.slipped)
    }

    // MARK: - Actions

    /// Listen: the reciter chants the whole verse while calibration runs.
    func playVerse() async {
        guard let first = practiceLines.first.flatMap({ words.lines[$0].audio?.first }),
              let last = practiceLines.last.flatMap({ words.lines[$0].audio?.last }) else { return }
        await play(from: first, to: last)
    }

    /// The reciter's audio for the current step (one line, or all of them).
    func playReciter() async {
        let spans = stepLines.compactMap { words.lines[$0].audio }
        guard let from = spans.first?.first, let to = spans.last?.last else { return }
        await play(from: from, to: to)
    }

    /// Replays the line that holds a part of the current step (the "Listen again to" tip).
    func playLine(holding part: Int) async {
        let parts = stepParts
        guard parts.indices.contains(part), let span = words.lines[parts[part].line].audio else { return }
        await play(from: span[0], to: span[1])
    }

    private func play(from: Double, to: Double) async {
        listener?.stopPlayback()
        isPlayingReciter = true
        await audio.play(verseId: verseId, from: from, to: to)
        isPlayingReciter = false
    }

    func stopReciter() {
        audio.stop()
        isPlayingReciter = false
    }

    func begin() {
        stopReciter()
        go(to: .line(0))
    }

    func next() {
        switch step {
        case .listen: begin()
        case .line(let n) where n + 1 < practiceLines.count: go(to: .line(n + 1))
        case .line: go(to: practiceLines.count > 1 ? .join : .memory(hints: true))
        case .join: go(to: .memory(hints: true))
        case .memory(hints: true): go(to: .memory(hints: false))
        case .memory(hints: false), .learned: break
        }
    }

    func tryAgain() {
        listener?.stopPlayback()
        result = nil
        lastTake = nil
    }

    private func go(to next: Step) {
        listener?.stopPlayback()
        result = nil
        lastTake = nil
        message = nil
        step = next
        if case .line = next {
            Task { await playReciter() }       // each line starts with the reciter (spec 6.3)
        }
    }

    /// The mic button: start a take, or end the one in progress.
    func micTapped() {
        guard let listener else { return }
        if isRecording {
            listener.stop()
            return
        }
        isRecording = true
        stopReciter()
        Task {
            await record(with: listener)
            isRecording = false
        }
    }

    private func record(with listener: ChantListener) async {
        progress.markLearning(verseId)
        result = nil
        message = nil
        let scoring = stepScoring
        let fromMemory = if case .memory = step { true } else { false }
        do {
            guard let take = try await listener.take(line: scoring.line, notChecked: scoring.notChecked,
                                                     stopsOnSilence: !fromMemory) else {
                message = "That take was stopped. Try again when you're ready."
                return
            }
            lastTake = take
            result = ChantScorer.scoreLine(scoring.line, heard: take.heard, notChecked: scoring.notChecked)
            if step == .memory(hints: false), allRight {
                progress.markLearned(verseId)
                step = .learned
            }
        } catch {
            message = "Something went wrong while listening. Try again in a moment."
        }
    }

    func hearMyTry() {
        guard let lastTake, let listener else { return }
        stopReciter()
        listener.play(lastTake)
    }

    /// Lesson stops when the app leaves the screen (spec 6.7).
    func appLeftForeground() {
        listener?.discard()
        listener?.stopPlayback()
        stopReciter()
    }
}
