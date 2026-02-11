import SwiftUI

struct TranslationView: View {
    let translations: [Translation]
    let theme: AppTheme
    let fontSize: Double
    let defaultLanguage: String
    let preferredHindiAuthor: String
    let preferredEnglishAuthor: String

    @State private var selectedLanguage: Language = .english
    @State private var selectedAuthor: String = ""

    private var filteredTranslations: [Translation] {
        translations.filter { $0.language == selectedLanguage }
    }

    private var availableAuthors: [String] {
        Array(Set(filteredTranslations.map(\.author))).sorted()
    }

    private var currentTranslation: Translation? {
        filteredTranslations.first { $0.author == selectedAuthor }
            ?? filteredTranslations.first
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Language toggle
            HStack(spacing: 0) {
                languageButton(.english, label: "English")
                languageButton(.hindi, label: "Hindi")
            }
            .background(theme.cardBackgroundColor)
            .cornerRadius(8)

            // Author picker
            if availableAuthors.count > 1 {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(availableAuthors, id: \.self) { author in
                            Button {
                                selectedAuthor = author
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

            // Translation text
            if let translation = currentTranslation {
                Text(translation.text)
                    .font(.system(size: fontSize))
                    .foregroundStyle(theme.primaryTextColor)
                    .lineSpacing(4)

                Text("— \(translation.author)")
                    .font(.caption)
                    .foregroundStyle(theme.secondaryTextColor)
            }
        }
        .onAppear { setupDefaults() }
        .onChange(of: selectedLanguage) { _, _ in pickPreferredAuthor() }
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
        selectedLanguage = defaultLanguage == "hindi" ? .hindi : .english
        pickPreferredAuthor()
    }

    private func pickPreferredAuthor() {
        let preferred = selectedLanguage == .hindi ? preferredHindiAuthor : preferredEnglishAuthor
        if !preferred.isEmpty && availableAuthors.contains(preferred) {
            selectedAuthor = preferred
        } else {
            selectedAuthor = availableAuthors.first ?? ""
        }
    }
}
