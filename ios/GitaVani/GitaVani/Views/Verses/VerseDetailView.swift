import SwiftUI

struct VerseDetailView: View {
    let initialVerseId: String
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress
    let audioService: AudioService

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
                            Text("Chapter \(verse.chapter), Verse \(verse.verse)")
                                .font(.subheadline)
                                .foregroundStyle(theme.secondaryTextColor)

                            // Sanskrit shlok
                            ShlokView(
                                slok: verse.slok,
                                transliteration: verse.transliteration,
                                showTransliteration: settings.showTransliteration,
                                theme: theme,
                                fontSize: settings.scaledFontSize
                            )

                            // Action bar
                            HStack(spacing: 0) {
                                Button {
                                    audioService.play(verseId: currentVerseId)
                                } label: {
                                    VStack(spacing: 4) {
                                        Image(systemName: audioService.currentVerseId == currentVerseId && audioService.isPlaying ? "speaker.wave.2.fill" : "speaker.wave.2")
                                            .font(.system(size: 20))
                                        Text(audioService.currentVerseId == currentVerseId && audioService.isPlaying ? "Pause" : "Play")
                                            .font(.caption2)
                                    }
                                    .foregroundStyle(theme.accentColor)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                                    .contentShape(Rectangle())
                                }
                                .accessibilityLabel(audioService.currentVerseId == currentVerseId && audioService.isPlaying ? "Pause audio" : "Play audio")

                                Button {
                                    settings.showTransliteration.toggle()
                                } label: {
                                    VStack(spacing: 4) {
                                        Image(systemName: settings.showTransliteration ? "character.book.closed.fill" : "character.book.closed")
                                            .font(.system(size: 20))
                                        Text("Romanize")
                                            .font(.caption2)
                                    }
                                    .foregroundStyle(theme.accentColor)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                                    .contentShape(Rectangle())
                                }
                                .accessibilityLabel(settings.showTransliteration ? "Hide transliteration" : "Show transliteration")

                                Button {
                                    settings.toggleFavorite(currentVerseId)
                                } label: {
                                    VStack(spacing: 4) {
                                        Image(systemName: settings.isFavorite(currentVerseId) ? "heart.fill" : "heart")
                                            .font(.system(size: 20))
                                        Text(settings.isFavorite(currentVerseId) ? "Saved" : "Save")
                                            .font(.caption2)
                                    }
                                    .foregroundStyle(settings.isFavorite(currentVerseId) ? .red : theme.accentColor)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                                    .contentShape(Rectangle())
                                }
                                .accessibilityLabel(settings.isFavorite(currentVerseId) ? "Remove from favorites" : "Add to favorites")

                                Button {
                                    showShareSheet = true
                                } label: {
                                    VStack(spacing: 4) {
                                        Image(systemName: "square.and.arrow.up")
                                            .font(.system(size: 20))
                                        Text("Share")
                                            .font(.caption2)
                                    }
                                    .foregroundStyle(theme.accentColor)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                                    .contentShape(Rectangle())
                                }
                                .accessibilityLabel("Share verse")
                            }

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
        .onDisappear {
            audioService.stop()
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
        audioService.stop()
        withAnimation(.easeInOut(duration: 0.2)) {
            currentVerseId = allVerses[index - 1].id
        }
    }

    private func goToNext() {
        guard let index = currentIndex, index < allVerses.count - 1 else { return }
        audioService.stop()
        withAnimation(.easeInOut(duration: 0.2)) {
            currentVerseId = allVerses[index + 1].id
        }
    }
}
