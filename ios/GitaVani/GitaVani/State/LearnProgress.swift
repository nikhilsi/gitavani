import Foundation

/// Learn progress per verse (spec 6.6): New (no entry), Learning or Learned. Stored on the phone.
@Observable
class LearnProgress {
    enum Status: String, Codable { case learning, learned }

    private(set) var statuses: [String: Status] {
        didSet {
            if let data = try? JSONEncoder().encode(statuses) {
                UserDefaults.standard.set(data, forKey: Self.key)
            }
        }
    }

    private static let key = "learnProgress"

    init() {
        let data = UserDefaults.standard.data(forKey: Self.key)
        statuses = data.flatMap { try? JSONDecoder().decode([String: Status].self, from: $0) } ?? [:]
    }

    func status(_ verseId: String) -> Status? { statuses[verseId] }

    /// The first take on a verse moves it from New to Learning. A Learned verse stays Learned.
    func markLearning(_ verseId: String) {
        if statuses[verseId] == nil { statuses[verseId] = .learning }
    }

    func markLearned(_ verseId: String) {
        statuses[verseId] = .learned
    }
}
