// Dev tool: transcribe WAV files with Apple's SpeechAnalyzer (macOS 26+). Usage: transcribe <locale> <file>... (TIMINGS=1 adds word times)
import Foundation
import Speech
import AVFoundation

@main
struct Main {
    static func makeTranscriber(_ locale: Locale) -> SpeechTranscriber {
        SpeechTranscriber(locale: locale, transcriptionOptions: [], reportingOptions: [],
                          attributeOptions: [.audioTimeRange, .transcriptionConfidence])
    }

    static func main() async {
        setvbuf(stdout, nil, _IOLBF, 0)   // one line per file: a caller can resume after a stall
        let args = CommandLine.arguments
        let locale = Locale(identifier: args[1])
        let timings = ProcessInfo.processInfo.environment["TIMINGS"] == "1"
        do {
            if let req = try await AssetInventory.assetInstallationRequest(supporting: [makeTranscriber(locale)]) {
                print("downloading \(locale.identifier) model...")
                try await req.downloadAndInstall()
            }
        } catch { print("asset error:", error) }
        for path in args.dropFirst(2) {
            let t = makeTranscriber(locale)
            let analyzer = SpeechAnalyzer(modules: [t])
            do {
                let file = try AVAudioFile(forReading: URL(fileURLWithPath: path))
                let timings = timings
                async let collected: [String] = t.results.reduce(into: []) { acc, r in
                    var line = String(r.text.characters)
                    if timings {
                        let runs = r.text.runs.compactMap { run -> String? in
                            guard let tr = run.audioTimeRange else { return nil }
                            let w = String(r.text[run.range].characters).trimmingCharacters(in: .whitespaces)
                            return w.isEmpty ? nil : String(format: "%@@%.2f-%.2f", w, tr.start.seconds, tr.end.seconds)
                        }
                        line += "\n   timings: " + runs.joined(separator: " ")
                    }
                    acc.append(line)
                }
                if let last = try await analyzer.analyzeSequence(from: file) {
                    try await analyzer.finalizeAndFinish(through: last)
                } else {
                    await analyzer.cancelAndFinishNow()
                }
                let parts = try await collected
                print("\(locale.identifier) \(URL(fileURLWithPath: path).lastPathComponent): \(parts.joined(separator: " "))")
            } catch { print("\(path): ERROR \(error)") }
        }
    }
}
