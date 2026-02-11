import Foundation

struct Verse: Codable, Identifiable {
    let id: String
    let chapter: Int
    let verse: Int
    let slok: String
    let transliteration: String
    let translations: [Translation]
    let commentaries: [Commentary]
}
