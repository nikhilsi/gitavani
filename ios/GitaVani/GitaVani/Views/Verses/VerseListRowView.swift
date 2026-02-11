import SwiftUI

struct VerseListRowView: View {
    let verse: Verse
    let theme: AppTheme
    let fontSize: Double

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Verse \(verse.verse)")
                .font(.system(size: fontSize - 2, weight: .semibold))
                .foregroundStyle(theme.accentColor)

            Text(verse.slok.components(separatedBy: "\n").first ?? "")
                .font(.system(size: fontSize))
                .foregroundStyle(theme.primaryTextColor)
                .lineLimit(2)
        }
        .padding(.vertical, 6)
    }
}
