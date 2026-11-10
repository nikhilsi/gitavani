import SwiftUI

/// "Word by word" section on the reading screen: each word of the verse with its meaning.
/// Closed by default; the open or closed state is remembered in AppSettings.
struct WordByWordView: View {
    let words: VerseWords
    let theme: AppTheme
    let fontSize: Double
    let settings: AppSettings

    private var lineGroups: [WordLine] {
        words.lines.filter { !$0.parts.isEmpty }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    settings.showWordByWord.toggle()
                }
            } label: {
                HStack {
                    Text("Word by word")
                        .font(.system(size: fontSize, weight: .semibold))
                        .foregroundStyle(theme.primaryTextColor)
                    Spacer()
                    Image(systemName: settings.showWordByWord ? "chevron.up" : "chevron.down")
                        .foregroundStyle(theme.accentColor)
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(settings.showWordByWord ? "Hide word by word meanings" : "Show word by word meanings")

            if settings.showWordByWord {
                if words.available && !lineGroups.isEmpty {
                    ForEach(Array(lineGroups.enumerated()), id: \.offset) { index, line in
                        if index > 0 {
                            Divider()
                                .background(theme.secondaryTextColor.opacity(0.3))
                        }
                        VStack(alignment: .leading, spacing: 10) {
                            ForEach(Array(line.parts.enumerated()), id: \.offset) { _, part in
                                WordPartRow(part: part, theme: theme, fontSize: fontSize)
                            }
                        }
                    }

                    Text("Word meanings: Swami Sivananda")
                        .font(.system(size: max(fontSize - 5, 11)))
                        .foregroundStyle(theme.secondaryTextColor)
                        .padding(.top, 2)
                } else {
                    Text("Word meanings aren't available for this verse yet.")
                        .font(.system(size: fontSize - 2))
                        .foregroundStyle(theme.secondaryTextColor)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(theme.cardBackgroundColor)
        .cornerRadius(12)
    }
}

/// One word: Devanagari and Roman letters on the first line, its meaning below.
struct WordPartRow: View {
    let part: WordPart
    let theme: AppTheme
    let fontSize: Double

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text(part.deva)
                    .font(.system(size: fontSize))
                    .foregroundStyle(theme.primaryTextColor)
                Text(part.roman)
                    .font(.system(size: fontSize - 3))
                    .italic()
                    .foregroundStyle(theme.secondaryTextColor)
            }
            Text(part.meaning)
                .font(.system(size: fontSize - 2))
                .foregroundStyle(theme.primaryTextColor)
                .fixedSize(horizontal: false, vertical: true)
        }
        .accessibilityElement(children: .combine)
    }
}
