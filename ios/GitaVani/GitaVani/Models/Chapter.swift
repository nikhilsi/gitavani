import Foundation

struct Chapter: Codable, Identifiable {
    let chapterNumber: Int
    let versesCount: Int
    let name: String
    let translation: String
    let transliteration: String
    let meaning: LocalizedText
    let summary: LocalizedText

    var id: Int { chapterNumber }
}
