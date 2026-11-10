import Foundation

/// Word-by-word data from chant_data.json (built by scripts/build_chant_data.py).
nonisolated struct ChantData: Codable {
    let source: String
    let verses: [String: VerseWords]
}

nonisolated struct VerseWords: Codable {
    /// False when the verse has no reliable word list yet.
    let available: Bool
    /// True when Learn can teach this verse: word parts, plus line timings that passed the per-line check.
    let learn: Bool
    let lines: [WordLine]
}

nonisolated struct WordLine: Codable {
    let deva: String
    let roman: String
    /// Lines like "श्रीभगवानुवाच" (the Blessed Lord said). They have no parts.
    let speaker: Bool
    let parts: [WordPart]
    /// Learn: the line's sound key (scripts/chant_scorer.py) and its start and end in the verse recording.
    let key: String
    let audio: [Double]?
}

nonisolated struct WordPart: Codable, Hashable {
    let deva: String
    let roman: String
    let meaning: String
    /// Learn: the stretch [start, end) of the line's sound key this part owns.
    let k: [Int]
}

extension WordLine {
    var scoringLine: ChantScorer.ScoringLine {
        ChantScorer.ScoringLine(key: key, stretches: parts.map { ($0.k[0], $0.k[1]) })
    }
}
