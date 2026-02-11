import SwiftUI

struct VerseDetailView: View {
    let initialVerseId: String
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress

    @State private var currentVerseId: String = ""

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
                                    settings.showTransliteration.toggle()
                                } label: {
                                    Image(systemName: settings.showTransliteration ? "character.book.closed.fill" : "character.book.closed")
                                        .foregroundStyle(theme.accentColor)
                                }
                            }

                            // Sanskrit shlok
                            ShlokView(
                                slok: verse.slok,
                                transliteration: verse.transliteration,
                                showTransliteration: settings.showTransliteration,
                                theme: theme,
                                fontSize: settings.fontSize
                            )

                            // Translation
                            TranslationView(
                                translations: verse.translations,
                                theme: theme,
                                fontSize: settings.fontSize,
                                defaultLanguage: settings.defaultLanguage,
                                preferredHindiAuthor: settings.preferredHindiAuthor,
                                preferredEnglishAuthor: settings.preferredEnglishAuthor
                            )
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
        .onAppear {
            if currentVerseId.isEmpty {
                currentVerseId = initialVerseId
            }
            if let v = verse {
                readingProgress.update(chapter: v.chapter, verse: v.verse)
            }
        }
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
