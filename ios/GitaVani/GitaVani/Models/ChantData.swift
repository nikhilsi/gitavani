import Foundation

/// Word-by-word data from chant_data.json (built by scripts/build_chant_data.py).
nonisolated struct ChantData: Codable {
    let source: String
    let verses: [String: VerseWords]
}

nonisolated struct VerseWords: Codable {
    /// False when the verse has no reliable word list yet.
    let available: Bool
    let lines: [WordLine]
}

nonisolated struct WordLine: Codable {
    let deva: String
    let roman: String
    /// Lines like "श्रीभगवानुवाच" (the Blessed Lord said). They have no parts.
    let speaker: Bool
    let parts: [WordPart]
}

nonisolated struct WordPart: Codable, Hashable {
    let deva: String
    let roman: String
    let meaning: String
}
