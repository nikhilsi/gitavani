import SwiftUI

@Observable
class AppSettings {
    var fontSize: Double {
        didSet { UserDefaults.standard.set(fontSize, forKey: "fontSize") }
    }

    var defaultLanguage: String {
        didSet { UserDefaults.standard.set(defaultLanguage, forKey: "defaultLanguage") }
    }

    var showTransliteration: Bool {
        didSet { UserDefaults.standard.set(showTransliteration, forKey: "showTransliteration") }
    }

    var preferredHindiAuthor: String {
        didSet { UserDefaults.standard.set(preferredHindiAuthor, forKey: "preferredHindiAuthor") }
    }

    var preferredEnglishAuthor: String {
        didSet { UserDefaults.standard.set(preferredEnglishAuthor, forKey: "preferredEnglishAuthor") }
    }

    var preferredCommentaryLanguage: String {
        didSet { UserDefaults.standard.set(preferredCommentaryLanguage, forKey: "preferredCommentaryLanguage") }
    }

    var preferredHindiCommentaryAuthor: String {
        didSet { UserDefaults.standard.set(preferredHindiCommentaryAuthor, forKey: "preferredHindiCommentaryAuthor") }
    }

    var preferredEnglishCommentaryAuthor: String {
        didSet { UserDefaults.standard.set(preferredEnglishCommentaryAuthor, forKey: "preferredEnglishCommentaryAuthor") }
    }

    var preferredSanskritCommentaryAuthor: String {
        didSet { UserDefaults.standard.set(preferredSanskritCommentaryAuthor, forKey: "preferredSanskritCommentaryAuthor") }
    }

    /// Dynamic Type scale factor — set from ContentView's @ScaledMetric, not persisted
    var dynamicTypeScale: Double = 1.0

    /// Font size adjusted for Dynamic Type system setting
    var scaledFontSize: Double { fontSize * dynamicTypeScale }

    var favoriteVerseIds: [String] {
        didSet {
            if let data = try? JSONEncoder().encode(favoriteVerseIds) {
                UserDefaults.standard.set(data, forKey: "favoriteVerseIds")
            }
        }
    }

    func isFavorite(_ verseId: String) -> Bool {
        favoriteVerseIds.contains(verseId)
    }

    func toggleFavorite(_ verseId: String) {
        if let index = favoriteVerseIds.firstIndex(of: verseId) {
            favoriteVerseIds.remove(at: index)
        } else {
            favoriteVerseIds.insert(verseId, at: 0)
        }
    }

    init() {
        let defaults = UserDefaults.standard
        fontSize = defaults.object(forKey: "fontSize") as? Double ?? 18.0
        defaultLanguage = defaults.string(forKey: "defaultLanguage") ?? "english"
        showTransliteration = defaults.object(forKey: "showTransliteration") as? Bool ?? true
        preferredHindiAuthor = defaults.string(forKey: "preferredHindiAuthor") ?? ""
        preferredEnglishAuthor = defaults.string(forKey: "preferredEnglishAuthor") ?? ""
        preferredCommentaryLanguage = defaults.string(forKey: "preferredCommentaryLanguage") ?? ""
        preferredHindiCommentaryAuthor = defaults.string(forKey: "preferredHindiCommentaryAuthor") ?? ""
        preferredEnglishCommentaryAuthor = defaults.string(forKey: "preferredEnglishCommentaryAuthor") ?? ""
        preferredSanskritCommentaryAuthor = defaults.string(forKey: "preferredSanskritCommentaryAuthor") ?? ""
        if let data = defaults.data(forKey: "favoriteVerseIds"),
           let ids = try? JSONDecoder().decode([String].self, from: data) {
            favoriteVerseIds = ids
        } else {
            favoriteVerseIds = []
        }
    }
}
