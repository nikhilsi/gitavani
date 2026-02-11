import Foundation

struct Translation: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language.rawValue)" }
}

struct Commentary: Codable, Identifiable {
    let author: String
    let language: Language
    let text: String

    var id: String { "\(author)-\(language.rawValue)-commentary" }
}
