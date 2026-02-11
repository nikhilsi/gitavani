import Foundation

@Observable
class GitaDataService {
    private(set) var chapters: [Chapter] = []
    private(set) var verses: [Verse] = []
    private(set) var isLoaded = false
    private(set) var loadError: String?

    // Dictionary lookups for O(1) access
    private var verseById: [String: Verse] = [:]
    private var versesByChapter: [Int: [Verse]] = [:]
    private var chapterByNumber: [Int: Chapter] = [:]

    init() {
        Task { await loadData() }
    }

    @MainActor
    private func loadData() async {
        guard let url = Bundle.main.url(forResource: "gita_data", withExtension: "json") else {
            loadError = "Data file not found in app bundle."
            return
        }

        do {
            let data = try Data(contentsOf: url)
            let decoded = try await Task.detached {
                let decoder = JSONDecoder()
                decoder.keyDecodingStrategy = .convertFromSnakeCase
                return try decoder.decode(GitaData.self, from: data)
            }.value

            chapters = decoded.chapters
            verses = decoded.verses

            // Build lookup dictionaries
            verseById = Dictionary(uniqueKeysWithValues: verses.map { ($0.id, $0) })
            versesByChapter = Dictionary(grouping: verses, by: { $0.chapter })
            chapterByNumber = Dictionary(uniqueKeysWithValues: chapters.map { ($0.chapterNumber, $0) })

            isLoaded = true
            #if DEBUG
            print("GitaDataService: Loaded \(chapters.count) chapters, \(verses.count) verses")
            #endif
        } catch {
            loadError = "Failed to load Gita data: \(error.localizedDescription)"
            #if DEBUG
            print("GitaDataService: \(loadError!)")
            #endif
        }
    }

    func verses(forChapter chapterNumber: Int) -> [Verse] {
        versesByChapter[chapterNumber] ?? []
    }

    func verse(id: String) -> Verse? {
        verseById[id]
    }

    func chapter(number: Int) -> Chapter? {
        chapterByNumber[number]
    }
}
