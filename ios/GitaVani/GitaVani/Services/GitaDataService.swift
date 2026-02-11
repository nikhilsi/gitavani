import Foundation

@Observable
class GitaDataService {
    private(set) var chapters: [Chapter] = []
    private(set) var verses: [Verse] = []
    private(set) var isLoaded = false

    init() {
        loadData()
    }

    private func loadData() {
        guard let url = Bundle.main.url(forResource: "gita_data", withExtension: "json") else {
            print("GitaDataService: gita_data.json not found in bundle")
            return
        }

        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
            decoder.keyDecodingStrategy = .convertFromSnakeCase
            let gitaData = try decoder.decode(GitaData.self, from: data)
            chapters = gitaData.chapters
            verses = gitaData.verses
            isLoaded = true
            print("GitaDataService: Loaded \(chapters.count) chapters, \(verses.count) verses")
        } catch {
            print("GitaDataService: Failed to load data - \(error)")
        }
    }

    func verses(forChapter chapterNumber: Int) -> [Verse] {
        verses.filter { $0.chapter == chapterNumber }
    }

    func verse(id: String) -> Verse? {
        verses.first { $0.id == id }
    }

    func chapter(number: Int) -> Chapter? {
        chapters.first { $0.chapterNumber == number }
    }
}
