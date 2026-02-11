import SwiftUI

struct ChapterListView: View {
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress
    @Binding var navigationPath: NavigationPath

    @State private var searchText = ""
    @State private var debouncedSearchText = ""

    var theme: AppTheme { themeManager.currentTheme }

    private var resumeVerseId: String? {
        guard readingProgress.hasProgress else { return nil }
        return "BG\(readingProgress.lastReadChapter).\(readingProgress.lastReadVerse)"
    }

    private var isSearching: Bool {
        debouncedSearchText.trimmingCharacters(in: .whitespaces).count >= 2
    }

    private var searchResults: [Verse] {
        guard isSearching else { return [] }
        let query = debouncedSearchText.lowercased()
        return dataService.verses.filter { verse in
            verse.slok.lowercased().contains(query) ||
            verse.transliteration.lowercased().contains(query) ||
            verse.translations.contains { $0.text.lowercased().contains(query) }
        }
    }

    private func matchSnippet(for verse: Verse, query: String) -> String? {
        let q = query.lowercased()

        // Check translations first (most useful to show)
        let lang: Language = settings.defaultLanguage == "hindi" ? .hindi : .english
        if let match = verse.translations.first(where: { $0.language == lang && $0.text.lowercased().contains(q) }) {
            return snippetAround(text: match.text, query: q)
        }
        // Any translation
        if let match = verse.translations.first(where: { $0.text.lowercased().contains(q) }) {
            return snippetAround(text: match.text, query: q)
        }
        // Transliteration
        if verse.transliteration.lowercased().contains(q) {
            return snippetAround(text: verse.transliteration, query: q)
        }
        // Sanskrit
        if verse.slok.lowercased().contains(q) {
            return verse.slok.components(separatedBy: "\n").first
        }
        return nil
    }

    private func snippetAround(text: String, query: String) -> String {
        guard let range = text.lowercased().range(of: query) else {
            return String(text.prefix(120))
        }
        let matchStart = text.distance(from: text.startIndex, to: range.lowerBound)
        let snippetStart = max(0, matchStart - 40)
        let startIndex = text.index(text.startIndex, offsetBy: snippetStart)
        let endIndex = text.index(startIndex, offsetBy: min(120, text.distance(from: startIndex, to: text.endIndex)))
        var snippet = String(text[startIndex..<endIndex])
        if snippetStart > 0 { snippet = "..." + snippet }
        if endIndex < text.endIndex { snippet = snippet + "..." }
        return snippet
    }

    var body: some View {
        List {
            if isSearching {
                // Search results
                if searchResults.isEmpty {
                    Section {
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 36))
                                .foregroundStyle(theme.secondaryTextColor.opacity(0.5))
                            Text("No results for \"\(debouncedSearchText)\"")
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
                        Text("\(searchResults.count) result\(searchResults.count == 1 ? "" : "s")")
                            .font(.caption)
                            .foregroundStyle(theme.secondaryTextColor)
                            .listRowBackground(Color.clear)
                            .listRowSeparator(.hidden)
                    }
                    Section {
                        ForEach(searchResults) { verse in
                            NavigationLink(value: verse.id) {
                                VStack(alignment: .leading, spacing: 6) {
                                    Text("Chapter \(verse.chapter), Verse \(verse.verse)")
                                        .font(.caption)
                                        .foregroundStyle(theme.accentColor)

                                    if let snippet = matchSnippet(for: verse, query: debouncedSearchText) {
                                        Text(snippet)
                                            .font(.system(size: settings.fontSize - 2))
                                            .foregroundStyle(theme.primaryTextColor)
                                            .lineLimit(3)
                                            .lineSpacing(2)
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                            .listRowBackground(theme.backgroundColor)
                        }
                    }
                }
            } else {
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
        }
        .listStyle(.plain)
        .background(theme.backgroundColor)
        .scrollContentBackground(.hidden)
        .searchable(text: $searchText, prompt: "Search verses...")
        .task(id: searchText) {
            do {
                try await Task.sleep(for: .milliseconds(300))
                debouncedSearchText = searchText
            } catch {
                // Task cancelled — user kept typing, ignore
            }
        }
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
