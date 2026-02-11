import SwiftUI

struct HelpItem {
    let icon: String
    let title: String
    let description: String
}

struct HelpView: View {
    let theme: AppTheme

    private let items: [HelpItem] = [
        HelpItem(
            icon: "hand.draw.fill",
            title: "Navigate Verses",
            description: "Swipe left or right on the reading screen to move between verses. You can also use the Previous/Next buttons at the bottom."
        ),
        HelpItem(
            icon: "globe",
            title: "Switch Language",
            description: "On the reading screen, tap English or Hindi to switch the translation language. Tap an author's name to see their translation."
        ),
        HelpItem(
            icon: "character.book.closed.fill",
            title: "Transliteration",
            description: "Tap the book icon on the reading screen to show Sanskrit verses in Roman script. You can also toggle this in Settings."
        ),
        HelpItem(
            icon: "text.book.closed.fill",
            title: "Commentaries",
            description: "Scroll below the translation on any verse to find commentaries from 17 scholars in English, Hindi, and Sanskrit. Use the language and author tabs to switch between them."
        ),
        HelpItem(
            icon: "magnifyingglass",
            title: "Search Verses",
            description: "Pull down on the home screen to search across all verses — Sanskrit, transliteration, and translations. You can also search within a specific chapter from its verse list."
        ),
        HelpItem(
            icon: "heart.fill",
            title: "Favorites",
            description: "Tap the heart icon on any verse to save it as a favorite. Access your saved verses from the heart icon in the toolbar. Sort by recent or chapter order."
        ),
        HelpItem(
            icon: "square.and.arrow.up",
            title: "Share Verses",
            description: "Tap the share icon on the reading screen to share a verse as a beautiful themed image card. The card includes the Sanskrit text, transliteration, and translation."
        ),
        HelpItem(
            icon: "paintpalette.fill",
            title: "Change Theme",
            description: "Go to Settings (gear icon) to choose between 4 themes: Sattva (light), Parchment (warm), Dusk (dark), or Lotus (saffron)."
        ),
        HelpItem(
            icon: "textformat.size",
            title: "Adjust Font Size",
            description: "In Settings, use the font size slider to make text larger or smaller (14–28pt). The app also responds to your system Dynamic Type setting."
        ),
        HelpItem(
            icon: "bookmark.fill",
            title: "Resume Reading",
            description: "The app remembers where you left off. When you return, tap the 'Continue Reading' banner on the home screen to pick up where you stopped."
        ),
        HelpItem(
            icon: "text.alignleft",
            title: "Chapter Summary",
            description: "On the chapter screen, tap 'Show Summary' to read a brief overview of the chapter before diving into individual verses."
        )
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("How to use GitaVani")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(theme.primaryTextColor)
                    .padding(.bottom, 4)

                ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                    HStack(alignment: .top, spacing: 14) {
                        Image(systemName: item.icon)
                            .font(.title3)
                            .foregroundStyle(theme.accentColor)
                            .frame(width: 32, alignment: .center)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title)
                                .font(.headline)
                                .foregroundStyle(theme.primaryTextColor)

                            Text(item.description)
                                .font(.subheadline)
                                .foregroundStyle(theme.secondaryTextColor)
                                .lineSpacing(3)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .padding()
        }
        .background(theme.backgroundColor)
        .navigationTitle("Help")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
    }
}
