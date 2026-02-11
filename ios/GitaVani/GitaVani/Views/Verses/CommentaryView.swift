import SwiftUI

struct CommentaryView: View {
    let commentaries: [Commentary]
    let theme: AppTheme
    let fontSize: Double
    let settings: AppSettings

    @State private var selectedLanguage: Language = .english
    @State private var selectedAuthor: String = ""
    @State private var isExpanded: Bool = false

    private let truncationLimit = 1500

    private var availableLanguages: [Language] {
        let langs = Set(commentaries.map(\.language))
        return Language.allCases.filter { langs.contains($0) }
    }

    private var filteredCommentaries: [Commentary] {
        commentaries.filter { $0.language == selectedLanguage }
    }

    private var availableAuthors: [String] {
        Array(Set(filteredCommentaries.map(\.author))).sorted()
    }

    private var currentCommentary: Commentary? {
        filteredCommentaries.first { $0.author == selectedAuthor }
            ?? filteredCommentaries.first
    }

    private var needsTruncation: Bool {
        guard let commentary = currentCommentary else { return false }
        return commentary.text.count > truncationLimit
    }

    private var displayText: String {
        guard let commentary = currentCommentary else { return "" }
        if needsTruncation && !isExpanded {
            let endIndex = commentary.text.index(commentary.text.startIndex, offsetBy: truncationLimit)
            return String(commentary.text[..<endIndex]) + "..."
        }
        return commentary.text
    }

    var body: some View {
        if commentaries.isEmpty { EmptyView() } else {
            VStack(alignment: .leading, spacing: 12) {
                // Section header
                Text("Commentary")
                    .font(.headline)
                    .foregroundStyle(theme.primaryTextColor)

                // Language toggle
                if availableLanguages.count > 1 {
                    HStack(spacing: 0) {
                        ForEach(availableLanguages, id: \.self) { lang in
                            languageButton(lang, label: lang.displayName)
                        }
                    }
                    .background(theme.cardBackgroundColor)
                    .cornerRadius(8)
                }

                // Author picker
                if availableAuthors.count > 1 {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(availableAuthors, id: \.self) { author in
                                Button {
                                    selectedAuthor = author
                                    saveAuthorPreference()
                                } label: {
                                    Text(author)
                                        .font(.caption)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(selectedAuthor == author ? theme.accentColor : theme.cardBackgroundColor)
                                        .foregroundStyle(selectedAuthor == author ? .white : theme.secondaryTextColor)
                                        .cornerRadius(16)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }

                // Commentary text
                if currentCommentary != nil {
                    Text(displayText)
                        .font(.system(size: fontSize))
                        .foregroundStyle(theme.primaryTextColor)
                        .lineSpacing(4)

                    if needsTruncation {
                        Button {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                isExpanded.toggle()
                            }
                        } label: {
                            Text(isExpanded ? "Show less" : "Read more...")
                                .font(.subheadline)
                                .foregroundStyle(theme.accentColor)
                        }
                        .buttonStyle(.plain)
                    }

                    Text("— \(currentCommentary!.author)")
                        .font(.caption)
                        .foregroundStyle(theme.secondaryTextColor)
                } else {
                    Text("No commentary available for this language.")
                        .font(.subheadline)
                        .foregroundStyle(theme.secondaryTextColor)
                        .italic()
                }
            }
            .onAppear { setupDefaults() }
            .onChange(of: selectedLanguage) { _, _ in
                isExpanded = false
                pickPreferredAuthor()
                settings.preferredCommentaryLanguage = selectedLanguage.rawValue
            }
            .onChange(of: selectedAuthor) { _, _ in
                isExpanded = false
            }
        }
    }

    private func languageButton(_ language: Language, label: String) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.2)) {
                selectedLanguage = language
            }
        } label: {
            Text(label)
                .font(.subheadline)
                .fontWeight(selectedLanguage == language ? .semibold : .regular)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(selectedLanguage == language ? theme.accentColor : Color.clear)
                .foregroundStyle(selectedLanguage == language ? .white : theme.secondaryTextColor)
        }
        .buttonStyle(.plain)
    }

    private func setupDefaults() {
        // Use saved commentary language, fall back to default language
        if !settings.preferredCommentaryLanguage.isEmpty,
           let lang = Language(rawValue: settings.preferredCommentaryLanguage),
           availableLanguages.contains(lang) {
            selectedLanguage = lang
        } else if settings.defaultLanguage == "hindi" && availableLanguages.contains(.hindi) {
            selectedLanguage = .hindi
        } else if availableLanguages.contains(.english) {
            selectedLanguage = .english
        } else {
            selectedLanguage = availableLanguages.first ?? .english
        }
        pickPreferredAuthor()
    }

    private func pickPreferredAuthor() {
        let preferred: String
        switch selectedLanguage {
        case .hindi: preferred = settings.preferredHindiCommentaryAuthor
        case .english: preferred = settings.preferredEnglishCommentaryAuthor
        case .sanskrit: preferred = settings.preferredSanskritCommentaryAuthor
        }
        if !preferred.isEmpty && availableAuthors.contains(preferred) {
            selectedAuthor = preferred
        } else {
            selectedAuthor = availableAuthors.first ?? ""
        }
    }

    private func saveAuthorPreference() {
        switch selectedLanguage {
        case .hindi: settings.preferredHindiCommentaryAuthor = selectedAuthor
        case .english: settings.preferredEnglishCommentaryAuthor = selectedAuthor
        case .sanskrit: settings.preferredSanskritCommentaryAuthor = selectedAuthor
        }
    }
}

extension Language {
    var displayName: String {
        switch self {
        case .english: return "English"
        case .hindi: return "Hindi"
        case .sanskrit: return "Sanskrit"
        }
    }
}
