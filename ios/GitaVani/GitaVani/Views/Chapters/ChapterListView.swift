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
            if readingProgress.hasProgress,
               let ch = dataService.chapter(number: readingProgress.lastReadChapter) {
                Section {
                    ResumeReadingBanner(
                        chapter: readingProgress.lastReadChapter,
                        verse: readingProgress.lastReadVerse,
                        chapterName: ch.meaning.en,
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

            Section {
                ForEach(dataService.chapters) { chapter in
                    NavigationLink(value: chapter.chapterNumber) {
                        ChapterRowView(chapter: chapter, theme: theme, fontSize: settings.fontSize)
                    }
                    .listRowBackground(theme.backgroundColor)
                }
            }
        }
        .listStyle(.plain)
        .background(theme.backgroundColor)
        .scrollContentBackground(.hidden)
        .navigationTitle("Bhagavad Gita")
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                NavigationLink(value: "help") {
                    Image(systemName: "questionmark.circle")
                        .foregroundStyle(theme.accentColor)
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(value: "settings") {
                    Image(systemName: "gearshape")
                        .foregroundStyle(theme.accentColor)
                }
            }
        }
    }
}
