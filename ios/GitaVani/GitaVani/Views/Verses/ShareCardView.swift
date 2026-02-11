import SwiftUI

/// A themed card view designed to be rendered as a shareable image
struct ShareCardView: View {
    let verse: Verse
    let translation: Translation?
    let showTransliteration: Bool
    let theme: AppTheme

    var body: some View {
        VStack(spacing: 20) {
            // Header
            Text("Bhagavad Gita")
                .font(.system(size: 14, weight: .medium, design: .serif))
                .foregroundStyle(theme.secondaryTextColor)

            Text("Chapter \(verse.chapter), Verse \(verse.verse)")
                .font(.system(size: 12))
                .foregroundStyle(theme.accentColor)

            // Sanskrit shlok
            Text(verse.slok)
                .font(.system(size: 18))
                .multilineTextAlignment(.center)
                .foregroundStyle(theme.primaryTextColor)
                .lineSpacing(6)

            // Transliteration
            if showTransliteration {
                Text(verse.transliteration)
                    .font(.system(size: 13))
                    .italic()
                    .multilineTextAlignment(.center)
                    .foregroundStyle(theme.secondaryTextColor)
                    .lineSpacing(3)
            }

            // Divider
            Rectangle()
                .fill(theme.accentColor.opacity(0.3))
                .frame(width: 60, height: 1)

            // Translation
            if let translation {
                Text(translation.text)
                    .font(.system(size: 15))
                    .multilineTextAlignment(.center)
                    .foregroundStyle(theme.primaryTextColor)
                    .lineSpacing(4)

                Text("— \(translation.author)")
                    .font(.system(size: 11))
                    .foregroundStyle(theme.secondaryTextColor)
            }

            // Branding
            HStack(spacing: 4) {
                Image(systemName: "book.fill")
                    .font(.system(size: 10))
                Text("GitaVani")
                    .font(.system(size: 11, weight: .medium, design: .serif))
            }
            .foregroundStyle(theme.accentColor.opacity(0.6))
            .padding(.top, 8)
        }
        .padding(32)
        .frame(width: 480)
        .background(theme.backgroundColor)
    }
}
