import SwiftUI

enum FavoritesSortOrder: String, CaseIterable {
    case recent = "Recent"
    case chapter = "Chapter Order"
}

struct FavoritesView: View {
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings

    @State private var sortOrder: FavoritesSortOrder = .recent

    var theme: AppTheme { themeManager.currentTheme }

    private var favoriteVerses: [Verse] {
        let verses = settings.favoriteVerseIds.compactMap { dataService.verse(id: $0) }
        switch sortOrder {
        case .recent:
            return verses
        case .chapter:
            return verses.sorted { ($0.chapter, $0.verse) < ($1.chapter, $1.verse) }
        }
    }

    private func translationSnippet(for verse: Verse) -> String {
        let lang: Language = settings.defaultLanguage == "hindi" ? .hindi : .english
        if let translation = verse.translations.first(where: { $0.language == lang }) {
            let text = translation.text
            if text.count > 120 {
                return String(text.prefix(120)) + "..."
            }
            return text
        }
        return ""
    }

    var body: some View {
        Group {
            if settings.favoriteVerseIds.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "heart")
                        .font(.system(size: 48))
                        .foregroundStyle(theme.secondaryTextColor.opacity(0.5))

                    Text("No Favorites Yet")
                        .font(.headline)
                        .foregroundStyle(theme.primaryTextColor)

                    Text("Tap the heart icon on any verse to save it here.")
                        .font(.subheadline)
                        .foregroundStyle(theme.secondaryTextColor)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List {
                    // Sort toggle
                    Section {
                        HStack(spacing: 0) {
                            ForEach(FavoritesSortOrder.allCases, id: \.self) { order in
                                Button {
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        sortOrder = order
                                    }
                                } label: {
                                    Text(order.rawValue)
                                        .font(.subheadline)
                                        .fontWeight(sortOrder == order ? .semibold : .regular)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 8)
                                        .background(sortOrder == order ? theme.accentColor : Color.clear)
                                        .foregroundStyle(sortOrder == order ? .white : theme.secondaryTextColor)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .background(theme.cardBackgroundColor)
                        .cornerRadius(8)
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                    }

                    // Verse list
                    Section {
                        ForEach(favoriteVerses) { verse in
                            NavigationLink(value: verse.id) {
                                VStack(alignment: .leading, spacing: 6) {
                                    HStack {
                                        Text("Chapter \(verse.chapter), Verse \(verse.verse)")
                                            .font(.caption)
                                            .foregroundStyle(theme.accentColor)

                                        Spacer()

                                        Button {
                                            settings.toggleFavorite(verse.id)
                                        } label: {
                                            Image(systemName: "heart.fill")
                                                .foregroundStyle(.red)
                                                .font(.caption)
                                        }
                                        .buttonStyle(.plain)
                                    }

                                    Text(verse.slok.components(separatedBy: "\n").first ?? "")
                                        .font(.system(size: settings.fontSize - 2))
                                        .foregroundStyle(theme.primaryTextColor)
                                        .lineLimit(1)

                                    Text(translationSnippet(for: verse))
                                        .font(.system(size: settings.fontSize - 4))
                                        .foregroundStyle(theme.secondaryTextColor)
                                        .lineLimit(2)
                                        .lineSpacing(2)
                                }
                                .padding(.vertical, 4)
                            }
                            .listRowBackground(theme.backgroundColor)
                        }
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        .background(theme.backgroundColor)
        .navigationTitle("Favorites")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
    }
}
