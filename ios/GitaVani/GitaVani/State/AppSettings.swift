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

    init() {
        let defaults = UserDefaults.standard
        fontSize = defaults.object(forKey: "fontSize") as? Double ?? 18.0
        defaultLanguage = defaults.string(forKey: "defaultLanguage") ?? "english"
        showTransliteration = defaults.bool(forKey: "showTransliteration")
        preferredHindiAuthor = defaults.string(forKey: "preferredHindiAuthor") ?? ""
        preferredEnglishAuthor = defaults.string(forKey: "preferredEnglishAuthor") ?? ""
    }
}
