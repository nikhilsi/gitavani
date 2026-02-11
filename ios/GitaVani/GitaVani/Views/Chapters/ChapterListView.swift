import SwiftUI

struct ChapterListView: View {
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress
    @Binding var navigationPath: NavigationPath

    var theme: AppTheme { themeManager.currentTheme }

    private var resumeVerseId: String? {
        guard readingProgress.hasProgress else { return nil }
        return "BG\(readingProgress.lastReadChapter).\(readingProgress.lastReadVerse)"
    }

    var body: some View {
        List {
            // Book cover header
            Section {
                VStack(spacing: 12) {
                    Image(systemName: "book.fill")
                        .font(.system(size: 36))
                        .foregroundStyle(theme.accentColor)

                    Text("GitaVani")
                        .font(.system(size: settings.fontSize + 10, weight: .bold, design: .serif))
                        .foregroundStyle(theme.primaryTextColor)

                    Text("The Bhagavad Gita")
                        .font(.system(size: settings.fontSize, design: .serif))
                        .italic()
                        .foregroundStyle(theme.secondaryTextColor)

                    Text("\(dataService.chapters.count) chapters  ·  \(dataService.verses.count) verses")
                        .font(.system(size: settings.fontSize - 4))
                        .foregroundStyle(theme.secondaryTextColor)
                        .padding(.top, 2)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 20)
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            }

            // Resume reading banner
            if readingProgress.hasProgress,
               let ch = dataService.chapter(number: readingProgress.lastReadChapter) {
                Section {
                    ResumeReadingBanner(
                        chapter: readingProgress.lastReadChapter,
                        verse: readingProgress.lastReadVerse,
                        chapterName: ch.meaning.oppositeLanguage(settings.defaultLanguage),
                        theme: theme,
                        onTap: {
                            if let verseId = resumeVerseId {
                                navigationPath.append(verseId)
                            }
                        }
                    )
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    .listRowBackground(Color.clear)
                }
            }

            // Chapters
            Section {
                ForEach(dataService.chapters) { chapter in
                    NavigationLink(value: chapter.chapterNumber) {
                        ChapterRowView(chapter: chapter, theme: theme, fontSize: settings.fontSize, defaultLanguage: settings.defaultLanguage)
                    }
                    .listRowBackground(theme.backgroundColor)
                }
            }
        }
        .listStyle(.plain)
        .background(theme.backgroundColor)
        .scrollContentBackground(.hidden)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                NavigationLink(value: "help") {
                    Image(systemName: "questionmark.circle")
                        .foregroundStyle(theme.accentColor)
                }
                .accessibilityLabel("Help")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack(spacing: 16) {
                    NavigationLink(value: "favorites") {
                        Image(systemName: settings.favoriteVerseIds.isEmpty ? "heart" : "heart.fill")
                            .foregroundStyle(theme.accentColor)
                    }
                    .accessibilityLabel("Favorites")

                    NavigationLink(value: "settings") {
                        Image(systemName: "gearshape")
                            .foregroundStyle(theme.accentColor)
                    }
                    .accessibilityLabel("Settings")
                }
            }
        }
    }
}
