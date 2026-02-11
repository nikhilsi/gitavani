import SwiftUI

struct VerseDetailView: View {
    let initialVerseId: String
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress

    @State private var currentVerseId: String = ""
    @State private var showShareSheet = false

    var theme: AppTheme { themeManager.currentTheme }

    private var allVerses: [Verse] { dataService.verses }

    private var currentIndex: Int? {
        allVerses.firstIndex { $0.id == currentVerseId }
    }

    private var verse: Verse? {
        guard let index = currentIndex else { return nil }
        return allVerses[index]
    }

    private var hasPrevious: Bool {
        guard let index = currentIndex else { return false }
        return index > 0
    }

    private var hasNext: Bool {
        guard let index = currentIndex else { return false }
        return index < allVerses.count - 1
    }

    var body: some View {
        Group {
            if let verse {
                VStack(spacing: 0) {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            // Verse header
                            HStack {
                                Text("Chapter \(verse.chapter), Verse \(verse.verse)")
                                    .font(.subheadline)
                                    .foregroundStyle(theme.secondaryTextColor)

                                Spacer()

                                Button {
                                    settings.toggleFavorite(currentVerseId)
                                } label: {
                                    Image(systemName: settings.isFavorite(currentVerseId) ? "heart.fill" : "heart")
                                        .foregroundStyle(settings.isFavorite(currentVerseId) ? .red : theme.accentColor)
                                }
                                .accessibilityLabel(settings.isFavorite(currentVerseId) ? "Remove from favorites" : "Add to favorites")

                                Button {
                                    settings.showTransliteration.toggle()
                                } label: {
                                    Image(systemName: settings.showTransliteration ? "character.book.closed.fill" : "character.book.closed")
                                        .foregroundStyle(theme.accentColor)
                                }
                                .accessibilityLabel(settings.showTransliteration ? "Hide transliteration" : "Show transliteration")

                                Button {
                                    showShareSheet = true
                                } label: {
                                    Image(systemName: "square.and.arrow.up")
                                        .foregroundStyle(theme.accentColor)
                                }
                                .accessibilityLabel("Share verse")
                            }

                            // Sanskrit shlok
                            ShlokView(
                                slok: verse.slok,
                                transliteration: verse.transliteration,
                                showTransliteration: settings.showTransliteration,
                                theme: theme,
                                fontSize: settings.scaledFontSize
                            )

                            // Translation
                            TranslationView(
                                translations: verse.translations,
                                theme: theme,
                                fontSize: settings.scaledFontSize,
                                settings: settings
                            )
                            .id(currentVerseId)

                            Divider()
                                .background(theme.secondaryTextColor.opacity(0.3))

                            // Commentary
                            CommentaryView(
                                commentaries: verse.commentaries,
                                theme: theme,
                                fontSize: settings.scaledFontSize,
                                settings: settings
                            )
                            .id("commentary-\(currentVerseId)")
                        }
                        .padding()
                    }

                    Divider()
                        .background(theme.secondaryTextColor.opacity(0.3))

                    VerseNavigationView(
                        hasPrevious: hasPrevious,
                        hasNext: hasNext,
                        theme: theme,
                        onPrevious: goToPrevious,
                        onNext: goToNext
                    )
                    .background(theme.backgroundColor)
                }
                .background(theme.backgroundColor)
                .gesture(
                    DragGesture(minimumDistance: 50)
                        .onEnded { value in
                            if value.translation.width > 50 && hasPrevious {
                                goToPrevious()
                            } else if value.translation.width < -50 && hasNext {
                                goToNext()
                            }
                        }
                )
                .onChange(of: currentVerseId) { _, newId in
                    if let v = dataService.verse(id: newId) {
                        readingProgress.update(chapter: v.chapter, verse: v.verse)
                    }
                }
            } else {
                Text("Verse not found")
                    .foregroundStyle(theme.secondaryTextColor)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(value: "settings") {
                    Image(systemName: "gearshape")
                        .foregroundStyle(theme.accentColor)
                }
                .accessibilityLabel("Settings")
            }
        }
        .onAppear {
            if currentVerseId.isEmpty {
                currentVerseId = initialVerseId
            }
            if let v = verse {
                readingProgress.update(chapter: v.chapter, verse: v.verse)
            }
        }
        .sheet(isPresented: $showShareSheet) {
            if let verse {
                let translation = currentTranslation(for: verse)
                let shareImage = renderShareCard(verse: verse, translation: translation)
                let shareText = buildShareText(verse: verse, translation: translation)
                ShareSheetView(items: shareImage.map { [$0 as Any] } ?? [shareText])
            }
        }
    }

    private func currentTranslation(for verse: Verse) -> Translation? {
        let lang: Language = settings.defaultLanguage == "hindi" ? .hindi : .english
        let preferredAuthor = lang == .hindi ? settings.preferredHindiAuthor : settings.preferredEnglishAuthor
        if !preferredAuthor.isEmpty,
           let match = verse.translations.first(where: { $0.language == lang && $0.author == preferredAuthor }) {
            return match
        }
        return verse.translations.first(where: { $0.language == lang })
    }

    private func buildShareText(verse: Verse, translation: Translation?) -> String {
        var text = "Bhagavad Gita — Chapter \(verse.chapter), Verse \(verse.verse)\n\n"
        text += verse.slok + "\n"
        if settings.showTransliteration {
            text += "\n" + verse.transliteration + "\n"
        }
        if let translation {
            text += "\n\"\(translation.text)\"\n— \(translation.author)\n"
        }
        text += "\nShared via GitaVani"
        return text
    }

    @MainActor
    private func renderShareCard(verse: Verse, translation: Translation?) -> UIImage? {
        let cardView = ShareCardView(
            verse: verse,
            translation: translation,
            showTransliteration: settings.showTransliteration,
            theme: theme
        )
        let renderer = ImageRenderer(content: cardView)
        renderer.scale = 3.0
        return renderer.uiImage
    }

    private func goToPrevious() {
        guard let index = currentIndex, index > 0 else { return }
        withAnimation(.easeInOut(duration: 0.2)) {
            currentVerseId = allVerses[index - 1].id
        }
    }

    private func goToNext() {
        guard let index = currentIndex, index < allVerses.count - 1 else { return }
        withAnimation(.easeInOut(duration: 0.2)) {
            currentVerseId = allVerses[index + 1].id
        }
    }
}
