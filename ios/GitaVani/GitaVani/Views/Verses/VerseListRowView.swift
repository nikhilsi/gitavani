import SwiftUI

struct VerseListRowView: View {
    let verse: Verse
    let theme: AppTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Verse \(verse.verse)")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundStyle(theme.accentColor)

            Text(verse.slok.components(separatedBy: "\n").first ?? "")
                .font(.body)
                .foregroundStyle(theme.primaryTextColor)
                .lineLimit(2)
        }
        .padding(.vertical, 6)
    }
}
