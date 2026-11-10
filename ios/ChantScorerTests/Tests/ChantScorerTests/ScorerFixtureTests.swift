import Foundation
import XCTest
@testable import ChantScorer

/// Checks the Swift scorer against the shared cases in scripts/data/scorer_fixtures.json,
/// which the Python reference (scripts/chant_scorer.py) produced.
final class ScorerFixtureTests: XCTestCase {
    struct Fixtures: Decodable {
        struct KeyCase: Decodable { let text: String; let key: String }
        struct Part: Decodable { let roman: String; let k: [Int] }
        struct Line: Decodable { let key: String; let parts: [Part] }
        struct Tip: Decodable { let kind: String; let part: Int? }
        struct Expected: Decodable { let marks: [String]; let scores: [Double?]; let tip: Tip }
        struct ScoreCase: Decodable {
            let name: String
            let line: Line
            let heard: String
            let not_checked: [Int]
            let expected: Expected
        }
        let sound_key: [KeyCase]
        let score: [ScoreCase]
    }

    static let fixtures: Fixtures = {
        let url = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent().deletingLastPathComponent().deletingLastPathComponent()
            .deletingLastPathComponent().deletingLastPathComponent()
            .appendingPathComponent("scripts/data/scorer_fixtures.json")
        let data = try! Data(contentsOf: url)
        return try! JSONDecoder().decode(Fixtures.self, from: data)
    }()

    func testSoundKeyMatchesReference() {
        for c in Self.fixtures.sound_key {
            XCTAssertEqual(ChantScorer.soundKey(c.text), c.key, "sound key of \(c.text)")
        }
    }

    func testScoresMatchReference() {
        XCTAssertGreaterThan(Self.fixtures.score.count, 100)
        for c in Self.fixtures.score {
            let line = ChantScorer.ScoringLine(key: c.line.key, stretches: c.line.parts.map { ($0.k[0], $0.k[1]) })
            let result = ChantScorer.scoreLine(line, heard: c.heard, notChecked: Set(c.not_checked))
            XCTAssertEqual(result.marks.map(\.rawValue), c.expected.marks, "marks: \(c.name)")
            XCTAssertEqual(result.tipKind.rawValue, c.expected.tip.kind, "tip: \(c.name)")
            XCTAssertEqual(result.tipPart, c.expected.tip.part, "tip part: \(c.name)")
            for (got, want) in zip(result.scores, c.expected.scores) {
                switch (got, want) {
                case (nil, nil): break
                case let (g?, w?): XCTAssertEqual(g, w, accuracy: 0.0011, "score: \(c.name)")
                default: XCTFail("score presence differs: \(c.name)")
                }
            }
        }
    }

    func testJoinLeavesSpeakerLinesOut() {
        let speaker = ChantScorer.ScoringLine(key: "arjunauvaca", stretches: [])
        let a = ChantScorer.ScoringLine(key: "abc", stretches: [(0, 3)])
        let b = ChantScorer.ScoringLine(key: "de", stretches: [(0, 1), (1, 2)])
        let joined = ChantScorer.joinLines([(speaker, true), (a, false), (b, false)])
        XCTAssertEqual(joined.key, "abcde")
        XCTAssertEqual(joined.stretches.map { [$0.0, $0.1] }, [[0, 3], [3, 4], [4, 5]])
    }
}
