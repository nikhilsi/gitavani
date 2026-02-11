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
    @State private var searchText = ""
    @State private var debouncedSearchText = ""

    private var isSearching: Bool {
        debouncedSearchText.trimmingCharacters(in: .whitespaces).count >= 2
    }

    private var filteredVerses: [Verse] {
        guard isSearching else { return chapterVerses }
        let query = debouncedSearchText.lowercased()
        return chapterVerses.filter { verse in
            verse.slok.lowercased().contains(query) ||
            verse.transliteration.lowercased().contains(query) ||
            verse.translations.contains { $0.text.lowercased().contains(query) }
        }
    }

    /// English default: transliteration title, Hindi meaning subtitle
    /// Hindi default: Sanskrit name title, English meaning subtitle
    private var chapterTitle: String {
        guard let chapter else { return "" }
        return settings.defaultLanguage == "hindi" ? chapter.name : chapter.transliteration
    }

    private var chapterSubtitle: String {
        guard let chapter else { return "" }
        return chapter.meaning.oppositeLanguage(settings.defaultLanguage)
    }

    var body: some View {
        List {
            if !isSearching {
                if let chapter {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(chapterTitle)
                                .font(.system(size: settings.fontSize + 2, weight: .semibold))
                                .foregroundStyle(theme.primaryTextColor)

                            Text(chapterSubtitle)
                                .font(.system(size: settings.fontSize - 2))
                                .foregroundStyle(theme.secondaryTextColor)

                            Text(chapter.summary.forLanguage(settings.defaultLanguage))
                                .font(.system(size: settings.fontSize))
                                .foregroundStyle(theme.primaryTextColor)
                                .lineLimit(showSummary ? nil : 3)
                                .padding(.top, 4)

                            Button {
                                withAnimation { showSummary.toggle() }
                            } label: {
                                Text(showSummary ? "Show less" : "Read more...")
                                    .font(.system(size: settings.fontSize - 4))
                                    .foregroundStyle(theme.accentColor)
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.vertical, 4)
                        .listRowBackground(theme.cardBackgroundColor)
                    }
                }
            }

            if isSearching && filteredVerses.isEmpty {
                Section {
                    VStack(spacing: 12) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 36))
                            .foregroundStyle(theme.secondaryTextColor.opacity(0.5))
                        Text("No results in this chapter")
                            .font(.subheadline)
                            .foregroundStyle(theme.secondaryTextColor)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 40)
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
                }
            } else {
                Section {
                    ForEach(filteredVerses) { verse in
                        NavigationLink(value: verse.id) {
                            VerseListRowView(verse: verse, theme: theme, fontSize: settings.fontSize)
                        }
                        .listRowBackground(theme.backgroundColor)
                    }
                }
            }
        }
        .listStyle(.plain)
        .background(theme.backgroundColor)
        .scrollContentBackground(.hidden)
        .searchable(text: $searchText, prompt: "Search this chapter...")
        .task(id: searchText) {
            do {
                try await Task.sleep(for: .milliseconds(300))
                debouncedSearchText = searchText
            } catch {}
        }
        .navigationTitle("Chapter \(chapterNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
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
