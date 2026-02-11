import Foundation

struct LocalizedText: Codable {
    let en: String
    let hi: String

    func forLanguage(_ language: String) -> String {
        language == "hindi" ? hi : en
    }

    func oppositeLanguage(_ language: String) -> String {
        language == "hindi" ? en : hi
    }
}
