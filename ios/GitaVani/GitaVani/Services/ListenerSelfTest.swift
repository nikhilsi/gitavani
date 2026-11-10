#if DEBUG
import SwiftUI

/// Debug-only check of ChantListener's streaming path on a real phone (spec step 10).
///
/// Launch with "-GVListenerSelfTest". It reads Documents/selftest/plan.json, copied in with
/// `xcrun devicectl device copy to`, runs each recording or reciter line through the same chunked path a live
/// take uses, then writes Documents/selftest/results.json. Recordings stay out of the repo and the app bundle.
struct ListenerSelfTestView: View {
    @State private var log: [String] = []

    var body: some View {
        ScrollView {
            Text(log.joined(separator: "\n"))
                .font(.system(size: 11, design: .monospaced))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
        }
        .background(Color(uiColor: .systemBackground))
        .task {
            if #available(iOS 27.0, *) { await run() } else { add("needs iOS 27") }
        }
    }

    private struct Plan: Decodable {
        struct Item: Decodable {
            let name: String
            let file: String?       // a file in Documents/selftest
            let verse: String?      // or a bundled reciter recording
            let from: Double?
            let to: Double?
        }
        let items: [Item]
    }

    private func add(_ line: String) {
        log.append(line)
        print("SelfTest:", line)
    }

    @available(iOS 27.0, *)
    private func run() async {
        let dir = URL.documentsDirectory.appending(path: "selftest")
        var results: [[String: Any]] = []
        func save(_ done: Bool) {
            let out: [String: Any] = ["done": done, "results": results]
            if let data = try? JSONSerialization.data(withJSONObject: out, options: [.prettyPrinted, .sortedKeys]) {
                try? data.write(to: dir.appending(path: "results.json"))
            }
        }
        add("iOS \(UIDevice.current.systemVersion)")
        guard let locale = await ChantListener.supportedLocale() else {
            add("hi-IN not supported")
            return
        }
        let listener = ChantListener(locale: locale)
        if await !listener.modelInstalled() {
            add("installing model")
            do { try await listener.installModel { _ in } } catch { add("install failed: \(error)") }
        }
        add("model installed: \(await listener.modelInstalled())")
        guard let data = try? Data(contentsOf: dir.appending(path: "plan.json")),
              let plan = try? JSONDecoder().decode(Plan.self, from: data) else {
            add("no plan.json in Documents/selftest")
            return
        }
        for item in plan.items {
            let url: URL? = if let file = item.file { dir.appending(path: file) }
                else { item.verse.flatMap { Bundle.main.url(forResource: $0, withExtension: "mp3") } }
            guard let url else {
                add("\(item.name): no audio")
                continue
            }
            let start = Date.now
            do {
                let heard = try await listener.transcribe(file: url, from: item.from ?? 0, to: item.to)
                let seconds = Date.now.timeIntervalSince(start)
                results.append(["name": item.name, "heard": heard, "seconds": seconds])
                add(String(format: "%@ (%.1f s): %@", item.name, seconds, heard))
            } catch {
                results.append(["name": item.name, "error": "\(error)"])
                add("\(item.name): ERROR \(error)")
            }
            save(false)
        }
        save(true)
        add("DONE")
    }
}
#endif
