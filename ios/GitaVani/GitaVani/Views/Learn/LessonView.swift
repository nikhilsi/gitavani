import SwiftUI

/// The lesson screen, titled "Abhyāsa" (spec D5, 6.3). Opened full screen from the verse's Learn button.
@available(iOS 27.0, *)
struct LessonView: View {
    @State var model: LessonModel
    let theme: AppTheme
    let fontSize: Double
    let onClose: () -> Void

    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL
    @AppStorage("learnSeenNotCheckedNote") private var seenNotCheckedNote = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    switch model.setup {
                    case .ready: lesson
                    default: setupView
                    }
                }
                .frame(maxWidth: 700, alignment: .leading)
                .frame(maxWidth: .infinity)
                .padding()
            }
            .background(theme.backgroundColor)
            .navigationTitle("Abhyāsa")
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(theme.backgroundColor, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        model.close()
                        onClose()
                    }
                    .foregroundStyle(theme.accentColor)
                }
            }
        }
        .tint(theme.accentColor)
        .task { await model.open() }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { model.appLeftForeground() }
        }
    }

    // MARK: - Setup (spec 6.2)

    @ViewBuilder
    private var setupView: some View {
        switch model.setup {
        case .checking:
            ProgressView().frame(maxWidth: .infinity).padding(.top, 40)
        case .needsMic:
            notice(icon: "mic",
                   text: "GitaVani listens while you chant so it can show where you slipped. Audio stays on your phone.",
                   button: "Allow Microphone") { Task { await model.allowMic() } }
        case .micDenied:
            notice(icon: "mic.slash",
                   text: "The microphone is off for GitaVani. Turn it on in Settings to use Learn.",
                   button: "Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) { openURL(url) }
            }
        case .needsModel:
            notice(icon: "arrow.down.circle",
                   text: "Learn needs a download from Apple: the Hindi speech model. It stays on your phone. Learn works offline after this.",
                   button: "Download") { Task { await model.downloadModel() } }
        case .downloading(let fraction):
            VStack(spacing: 12) {
                ProgressView(value: fraction)
                Text("Downloading from Apple…")
                    .font(.system(size: fontSize - 2))
                    .foregroundStyle(theme.secondaryTextColor)
            }
            .padding(.top, 40)
        case .failed(let text):
            notice(icon: "exclamationmark.circle", text: text, button: "Try Again") { Task { await model.retrySetup() } }
        case .ready:
            EmptyView()
        }
    }

    private func notice(icon: String, text: String, button: String, action: @escaping () -> Void) -> some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundStyle(theme.accentColor)
            Text(text)
                .font(.system(size: fontSize))
                .foregroundStyle(theme.primaryTextColor)
                .multilineTextAlignment(.center)
            Button(button, action: action)
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 40)
    }

    // MARK: - Lesson (spec 6.3)

    @ViewBuilder
    private var lesson: some View {
        header
        verseText
        if model.step == .listen {
            listenControls
        } else if model.step == .learned {
            learnedView
        } else {
            if showParts { partsList }
            if let tip = tipText { tipView(tip) }
            if let message = model.message {
                Text(message)
                    .font(.system(size: fontSize - 2))
                    .foregroundStyle(theme.secondaryTextColor)
            }
            practiceControls
        }
    }

    private var stepTitle: String {
        switch model.step {
        case .listen: "Listen"
        case .line(let n): "Line \(n + 1) of \(model.practiceLines.count)"
        case .join: "All lines together"
        case .memory(hints: true): "From memory, with first letters"
        case .memory(hints: false): "From memory"
        case .learned: "Learned"
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(stepTitle)
                .font(.system(size: fontSize, weight: .semibold))
                .foregroundStyle(theme.primaryTextColor)
            if model.calibrated {
                let count = model.checkedCount
                Text("\(count.checked) of \(count.total) words checked")
                    .font(.system(size: fontSize - 4))
                    .foregroundStyle(theme.secondaryTextColor)
            }
        }
    }

    // MARK: Verse text

    private var shownLines: [Int] {
        switch model.step {
        case .listen, .learned: Array(model.words.lines.indices)
        default: model.stepLines
        }
    }

    @ViewBuilder
    private var verseText: some View {
        VStack(alignment: .leading, spacing: 12) {
            switch model.step {
            case .memory(hints: true):
                ForEach(model.stepLines, id: \.self) { i in
                    Text(firstLetters(model.words.lines[i].roman))
                        .font(.system(size: fontSize + 2))
                        .foregroundStyle(theme.primaryTextColor)
                }
            case .memory(hints: false):
                Text("Chant the verse from memory.")
                    .font(.system(size: fontSize))
                    .foregroundStyle(theme.secondaryTextColor)
            default:
                ForEach(shownLines, id: \.self) { i in
                    let line = model.words.lines[i]
                    VStack(alignment: .leading, spacing: 2) {
                        Text(line.deva)
                            .font(.system(size: fontSize + 2))
                            .foregroundStyle(line.speaker ? theme.secondaryTextColor : theme.primaryTextColor)
                        Text(line.roman)
                            .font(.system(size: fontSize - 2))
                            .italic()
                            .foregroundStyle(theme.secondaryTextColor)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(theme.cardBackgroundColor)
        .cornerRadius(12)
    }

    /// "yadā yadā hi" becomes "y… y… h…".
    private func firstLetters(_ roman: String) -> String {
        roman.split(separator: " ").map { "\($0.prefix(1))…" }.joined(separator: " ")
    }

    // MARK: Parts and marks (spec 6.4)

    /// In From memory the parts would give the verse away. They show only after a take.
    private var showParts: Bool {
        if case .memory = model.step { return model.result != nil }
        return true
    }

    private var partsList: some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach(Array(model.stepParts.enumerated()), id: \.offset) { index, item in
                // A take where nothing was heard gets the tip only, not a row of Slipped marks.
                let mark = model.result.flatMap { r in
                    r.tipKind != .nothingHeard && r.marks.indices.contains(index) ? r.marks[index] : nil
                }
                HStack(alignment: .firstTextBaseline, spacing: 10) {
                    markSymbol(mark)
                    WordPartRow(part: item.part, theme: theme, fontSize: fontSize)
                }
                .accessibilityElement(children: .combine)
                .accessibilityLabel("\(item.part.roman), \(item.part.meaning)\(mark.map { ", " + markName($0) } ?? "")")
            }
            if !seenNotCheckedNote, model.result?.marks.contains(.notChecked) == true {
                Text("Not checked: the phone can't hear this word reliably yet.")
                    .font(.system(size: fontSize - 4))
                    .foregroundStyle(theme.secondaryTextColor)
                    .onDisappear { seenNotCheckedNote = true }
            }
        }
    }

    @ViewBuilder
    private func markSymbol(_ mark: ChantScorer.Mark?) -> some View {
        switch mark {
        case .right:
            Image(systemName: "checkmark.circle.fill").foregroundStyle(theme.accentColor)
        case .slipped:
            Image(systemName: "circle.fill").foregroundStyle(theme.slippedColor)
        case .notChecked:
            Image(systemName: "circle").foregroundStyle(theme.secondaryTextColor)
        case nil:
            Image(systemName: "circle").hidden()
        }
    }

    private func markName(_ mark: ChantScorer.Mark) -> String {
        switch mark {
        case .right: "right"
        case .slipped: "slipped"
        case .notChecked: "not checked"
        }
    }

    // MARK: Tips (spec 6.5)

    private var tipText: String? {
        guard let result = model.result else { return nil }
        let parts = model.stepParts
        let word = result.tipPart.flatMap { parts.indices.contains($0) ? parts[$0].part.roman : nil } ?? ""
        let oneLine = if case .line = model.step { true } else { false }
        switch result.tipKind {
        case .nothingHeard: return "Nothing was heard. Tap the mic and chant."
        case .skipped: return "You skipped \(word)."
        case .slipped: return "Listen again to \(word)."
        case .unfinished: return oneLine ? "That take didn't finish. Try the whole line." : "That take didn't finish. Try the whole verse."
        case .allRight:
            // Every part was Not checked (calibration): nothing was actually judged.
            if !result.marks.contains(.right) {
                return "None of these words can be checked on this phone yet."
            }
            switch model.step {
            case .line(let n) where n + 1 < model.practiceLines.count: return "All words right. Next line when you're ready."
            case .memory(hints: true): return "All words right. Next, without the letters."
            default: return "All words right. Next step when you're ready."
            }
        }
    }

    @ViewBuilder
    private func tipView(_ text: String) -> some View {
        let tip = Text(text)
            .font(.system(size: fontSize))
            .foregroundStyle(theme.primaryTextColor)
            .frame(maxWidth: .infinity, alignment: .leading)
        if model.result?.tipKind == .slipped, let part = model.result?.tipPart {
            Button { Task { await model.playLine(holding: part) } } label: {
                HStack(spacing: 8) {
                    tip
                    Image(systemName: "speaker.wave.2").foregroundStyle(theme.accentColor)
                }
            }
            .buttonStyle(.plain)
            .accessibilityHint("Plays the reciter's line")
        } else {
            tip
        }
    }

    // MARK: Controls

    private var listenControls: some View {
        VStack(spacing: 14) {
            Button {
                if model.isPlayingReciter { model.stopReciter() } else { Task { await model.playVerse() } }
            } label: {
                Label(model.isPlayingReciter ? "Stop" : "Hear the Verse",
                      systemImage: model.isPlayingReciter ? "stop.fill" : "speaker.wave.2")
                    .frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.bordered)

            Button { model.begin() } label: {
                Text(model.calibrated ? "Start Learning" : "Getting ready…")
                    .frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.borderedProminent)
            .disabled(!model.calibrated)

            if let message = model.message {
                Text(message)
                    .font(.system(size: fontSize - 2))
                    .foregroundStyle(theme.secondaryTextColor)
            }
        }
        .task { await model.playVerse() }      // the lesson opens with the reciter chanting the verse
    }

    private var listening: Bool { model.isRecording || (model.listener?.phase ?? .idle) != .idle }

    private var practiceControls: some View {
        VStack(spacing: 14) {
            Button { model.micTapped() } label: {
                VStack(spacing: 6) {
                    Image(systemName: listening ? "stop.circle.fill" : "mic.circle.fill")
                        .font(.system(size: 64))
                    Text(listening ? "Listening… tap when done" : "Tap and chant")
                        .font(.system(size: fontSize - 2))
                }
                .foregroundStyle(theme.accentColor)
                .frame(maxWidth: .infinity, minHeight: 100)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(listening ? "Stop listening" : "Start chanting")

            HStack(spacing: 10) {
                smallButton("Hear Reciter", icon: "speaker.wave.2") { Task { await model.playReciter() } }
                if model.lastTake != nil {
                    smallButton("Hear My Try", icon: "play.circle") { model.hearMyTry() }
                }
            }
            HStack(spacing: 10) {
                if model.result != nil {
                    smallButton("Try Again", icon: "arrow.counterclockwise") { model.tryAgain() }
                }
                if model.step != .memory(hints: false) {
                    smallButton(nextTitle, icon: "arrow.right") { model.next() }
                }
            }
        }
        .disabled(model.listener == nil)
    }

    private var nextTitle: String {
        switch model.step {
        case .line(let n) where n + 1 < model.practiceLines.count: "Next Line"
        default: "Next"
        }
    }

    private func smallButton(_ title: String, icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Label(title, systemImage: icon)
                .font(.system(size: fontSize - 3))
                .frame(maxWidth: .infinity, minHeight: 44)
        }
        .buttonStyle(.bordered)
        .disabled(listening)
    }

    private var learnedView: some View {
        VStack(spacing: 14) {
            Image(systemName: "checkmark.seal.fill")
                .font(.system(size: 48))
                .foregroundStyle(theme.accentColor)
            Text("You chanted this verse from memory. It's marked Learned.")
                .font(.system(size: fontSize))
                .foregroundStyle(theme.primaryTextColor)
                .multilineTextAlignment(.center)
            Button("Done") {
                model.close()
                onClose()
            }
            .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 8)
    }
}
