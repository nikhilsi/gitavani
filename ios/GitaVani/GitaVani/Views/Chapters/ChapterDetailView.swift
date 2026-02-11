import SwiftUI

struct ChapterDetailView: View {
    let chapterNumber: Int
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress

    var theme: AppTheme { themeManager.currentTheme }

    var chapter: Chapter? {
        dataService.chapter(number: chapterNumber)
    }

    var chapterVerses: [Verse] {
        dataService.verses(forChapter: chapterNumber)
    }

    @State private var showSummary = false

    var body: some View {
        List {
            if let chapter {
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(chapter.name)
                            .font(.system(size: settings.fontSize + 2, weight: .semibold))
                            .foregroundStyle(theme.primaryTextColor)

                        Text(chapter.meaning.en)
                            .font(.system(size: settings.fontSize - 2))
                            .foregroundStyle(theme.secondaryTextColor)

                        if showSummary {
                            Text(chapter.summary.en)
                                .font(.system(size: settings.fontSize))
                                .foregroundStyle(theme.primaryTextColor)
                                .padding(.top, 4)
                        }

                        Button {
                            withAnimation { showSummary.toggle() }
                        } label: {
                            Text(showSummary ? "Hide Summary" : "Show Summary")
                                .font(.system(size: settings.fontSize - 4))
                                .foregroundStyle(theme.accentColor)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.vertical, 4)
                    .listRowBackground(theme.cardBackgroundColor)
                }
            }

            Section {
                ForEach(chapterVerses) { verse in
                    NavigationLink(value: verse.id) {
                        VerseListRowView(verse: verse, theme: theme, fontSize: settings.fontSize)
                    }
                    .listRowBackground(theme.backgroundColor)
                }
            }
        }
        .listStyle(.plain)
        .background(theme.backgroundColor)
        .scrollContentBackground(.hidden)
        .navigationTitle("Chapter \(chapterNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
    }
}
