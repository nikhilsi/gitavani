import Foundation

/// Learn's scorer (docs/chant-coach-spec.md, section 7.3).
///
/// A rule-for-rule port of scripts/chant_scorer.py. scripts/data/scorer_fixtures.json holds shared cases,
/// and ios/ChantScorerTests checks this file against them (`swift test`). Change the Python reference
/// first, regenerate the fixtures, then mirror the change here and in the Kotlin scorer.
nonisolated enum ChantScorer {
    static let threshold = 0.6        // a part scoring at or above this is Right
    static let skipShare = 0.7        // a low part with at least this share of its letters unheard was skipped
    static let unfinishedParts = 2    // this many skipped parts at the very end means the take stopped early
    static let gapOpen = 1.0          // extra cost to start a run of unheard or extra letters
    private static let eps = 1e-9

    enum Mark: String { case right, slipped, notChecked = "not_checked" }

    enum TipKind: String {
        case nothingHeard = "nothing_heard", skipped, slipped, unfinished, allRight = "all_right"
    }

    struct Result {
        var marks: [Mark]
        var scores: [Double?]
        var tipKind: TipKind
        var tipPart: Int?
    }

    /// A line to score: its sound key and one stretch [start, end) of the key per word part.
    struct ScoringLine {
        var key: String
        var stretches: [(Int, Int)]
    }

    // MARK: - Sound key

    private static let iastFold: [(String, String)] = [
        ("ṛ", "ri"), ("ṝ", "ri"), ("ḷ", "li"), ("ṃ", "m"), ("ḥ", "h"), ("ṅ", "n"), ("ñ", "n"),
        ("ṇ", "n"), ("ṭ", "t"), ("ḍ", "d"), ("ś", "sh"), ("ṣ", "sh"), ("ā", "a"), ("ī", "i"), ("ū", "u"),
    ]
    private static let soundFold: [(String, String)] = [
        ("x", "ks"), ("ksh", "ks"), ("sh", "s"), ("th", "t"), ("dh", "d"), ("bh", "b"), ("ph", "p"),
        ("kh", "k"), ("gh", "g"), ("jh", "j"), ("ch", "c"), ("f", "p"), ("w", "v"), ("ee", "i"),
        ("oo", "u"), ("ri", "r"), ("ru", "r"),
    ]

    /// Coarse sound key for IAST and for the recognizer's Hindi-style Roman letters.
    static func soundKey(_ text: String) -> String {
        var s = text.lowercased()
        for (a, b) in iastFold { s = s.replacingOccurrences(of: a, with: b, options: .literal) }
        // Drop remaining accents (compatibility decomposition, then no combining marks), keep a to z only.
        let letters = s.decomposedStringWithCompatibilityMapping.unicodeScalars.filter {
            $0.properties.canonicalCombiningClass == .notReordered && $0.value >= 97 && $0.value <= 122
        }
        s = String(String.UnicodeScalarView(letters))
        for (a, b) in soundFold { s = s.replacingOccurrences(of: a, with: b, options: .literal) }
        var out = ""
        var last: Character?
        for c in s {
            if c != last { out.append(c) }
            last = c
        }
        return out
    }

    // MARK: - Alignment

    private static let vowels = Set("aeiou".utf8)

    private static func subCost(_ a: UInt8, _ b: UInt8) -> Double {
        if a == b { return 0 }
        if vowels.contains(a) && vowels.contains(b) { return 0.5 }
        return 1
    }

    private static func indelCost(_ c: UInt8) -> Double {
        (c == UInt8(ascii: "a") || c == UInt8(ascii: "h")) ? 0.3 : 1
    }

    /// Lines up the expected key with what was heard (affine gaps; speech before and after the line is free).
    /// Returns, per expected letter, the cost charged to it and whether it went unheard.
    static func alignTake(_ exp: [UInt8], _ got: [UInt8]) -> (cost: [Double], missed: [Bool]) {
        let n = exp.count, m = got.count
        let inf = Double.infinity
        var M = Array(repeating: Array(repeating: inf, count: m + 1), count: n + 1)
        var X = M
        var Y = M
        M[0][0] = 0
        if m > 0 { for j in 1...m { Y[0][j] = 0 } }
        if n > 0 {
            for i in 1...n { X[i][0] = (i > 1 ? X[i - 1][0] : gapOpen) + indelCost(exp[i - 1]) }
        }
        if n > 0 && m > 0 {
            for i in 1...n {
                let e = exp[i - 1]
                for j in 1...m {
                    let g = got[j - 1]
                    M[i][j] = subCost(e, g) + min(M[i - 1][j - 1], X[i - 1][j - 1], Y[i - 1][j - 1])
                    X[i][j] = indelCost(e) + min(M[i - 1][j] + gapOpen, X[i - 1][j], Y[i - 1][j] + gapOpen)
                    Y[i][j] = indelCost(g) + min(M[i][j - 1] + gapOpen, X[i][j - 1] + gapOpen, Y[i][j - 1])
                }
            }
        }

        // Speech after the line is free: end at the first column (and state) with the lowest cost.
        var best = inf, j = 0, state = 0          // state: 0 = M, 1 = X, 2 = Y
        for jj in 0...m {
            if M[n][jj] < best - eps { best = M[n][jj]; j = jj; state = 0 }
            if X[n][jj] < best - eps { best = X[n][jj]; j = jj; state = 1 }
        }

        var cost = Array(repeating: 0.0, count: n)
        var missed = Array(repeating: false, count: n)
        var i = n
        while i > 0 {
            let e = exp[i - 1]
            if state == 0 {
                let c = subCost(e, got[j - 1])
                cost[i - 1] += c
                let prev = M[i][j] - c
                state = abs(M[i - 1][j - 1] - prev) < eps ? 0 : (abs(X[i - 1][j - 1] - prev) < eps ? 1 : 2)
                i -= 1
                j -= 1
            } else if state == 1 {
                let c = indelCost(e)
                missed[i - 1] = true
                let prev = X[i][j] - c
                if abs(M[i - 1][j] + gapOpen - prev) < eps {
                    cost[i - 1] += c + gapOpen
                    state = 0
                } else if abs(X[i - 1][j] - prev) < eps {
                    cost[i - 1] += c
                    state = 1
                } else {
                    cost[i - 1] += c + gapOpen
                    state = 2
                }
                i -= 1
            } else {
                // An extra heard letter inside the line: half its cost goes to the expected letter before it.
                let c = indelCost(got[j - 1])
                let prev = Y[i][j] - c
                if abs(M[i][j - 1] + gapOpen - prev) < eps {
                    cost[i - 1] += 0.5 * (c + gapOpen)
                    state = 0
                } else if abs(X[i][j - 1] + gapOpen - prev) < eps {
                    cost[i - 1] += 0.5 * (c + gapOpen)
                    state = 1
                } else {
                    cost[i - 1] += 0.5 * c
                    state = 2
                }
                j -= 1
            }
        }
        return (cost, missed)
    }

    // MARK: - Scoring

    static func scoreLine(_ line: ScoringLine, heard: String, notChecked: Set<Int> = []) -> Result {
        let got = Array(soundKey(heard).utf8)
        let count = line.stretches.count
        if got.isEmpty {
            return Result(marks: (0..<count).map { notChecked.contains($0) ? .notChecked : .slipped },
                          scores: (0..<count).map { notChecked.contains($0) ? nil : 0.0 },
                          tipKind: .nothingHeard, tipPart: nil)
        }
        let (cost, missed) = alignTake(Array(line.key.utf8), got)
        var marks: [Mark] = [], scores: [Double?] = [], skipped: [Bool] = []
        for (i, (s, e)) in line.stretches.enumerated() {
            if notChecked.contains(i) || e <= s {
                marks.append(.notChecked)
                scores.append(nil)
                skipped.append(false)
                continue
            }
            let length = Double(e - s)
            let score = max(0, 1 - cost[s..<e].reduce(0, +) / length)
            let miss = Double(missed[s..<e].filter { $0 }.count) / length
            scores.append(score)
            marks.append(score >= threshold - eps ? .right : .slipped)
            skipped.append(score < threshold - eps && miss >= skipShare)
        }

        // Skipped parts at the very end (ignoring Not checked ones) mean the take stopped early.
        var trailing: [Int] = []
        for i in stride(from: count - 1, through: 0, by: -1) {
            if marks[i] == .notChecked { continue }
            if !skipped[i] { break }
            trailing.append(i)
        }
        let unfinished = trailing.count >= unfinishedParts
        let early = unfinished ? Set(trailing) : []

        if let part = (0..<count).first(where: { skipped[$0] && !early.contains($0) }) {
            return Result(marks: marks, scores: scores, tipKind: .skipped, tipPart: part)
        }
        if let part = (0..<count).first(where: { marks[$0] == .slipped && !early.contains($0) }) {
            return Result(marks: marks, scores: scores, tipKind: .slipped, tipPart: part)
        }
        return Result(marks: marks, scores: scores, tipKind: unfinished ? .unfinished : .allRight, tipPart: nil)
    }

    /// One line made of several, for the Join and From memory steps. Speaker lines are left out.
    static func joinLines(_ lines: [(line: ScoringLine, speaker: Bool)]) -> ScoringLine {
        var key = ""
        var stretches: [(Int, Int)] = []
        for (line, speaker) in lines where !speaker {
            let offset = key.utf8.count
            key += line.key
            stretches += line.stretches.map { ($0.0 + offset, $0.1 + offset) }
        }
        return ScoringLine(key: key, stretches: stretches)
    }

    /// Calibration (spec D9): parts the phone can't hear even from the reciter are Not checked.
    static func notChecked(_ line: ScoringLine, heardFromReciter: String) -> Set<Int> {
        let result = scoreLine(line, heard: heardFromReciter)
        return Set(result.marks.indices.filter { result.marks[$0] != .right })
    }
}
