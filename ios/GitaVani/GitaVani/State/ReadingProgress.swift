import Foundation

@Observable
class ReadingProgress {
    var lastReadChapter: Int {
        didSet { UserDefaults.standard.set(lastReadChapter, forKey: "lastReadChapter") }
    }

    var lastReadVerse: Int {
        didSet { UserDefaults.standard.set(lastReadVerse, forKey: "lastReadVerse") }
    }

    var hasProgress: Bool {
        lastReadChapter > 0 && lastReadVerse > 0
    }

    init() {
        let defaults = UserDefaults.standard
        lastReadChapter = defaults.integer(forKey: "lastReadChapter")
        lastReadVerse = defaults.integer(forKey: "lastReadVerse")
    }

    func update(chapter: Int, verse: Int) {
        lastReadChapter = chapter
        lastReadVerse = verse
    }
}
