import Foundation

struct GitaMetadata: Codable {
    let source: String
    let sourceUrl: String
    let license: String
    let generatedAt: String
    let totalChapters: Int
    let totalVerses: Int
}

struct GitaData: Codable {
    let metadata: GitaMetadata
    let chapters: [Chapter]
    let verses: [Verse]
}
