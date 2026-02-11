import SwiftUI

struct ResumeReadingBanner: View {
    let chapter: Int
    let verse: Int
    let chapterName: String
    let theme: AppTheme
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: "book.fill")
                    .font(.title3)
                    .foregroundStyle(theme.accentColor)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Continue Reading")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(theme.primaryTextColor)
                    Text("Chapter \(chapter): \(chapterName) — Verse \(verse)")
                        .font(.caption)
                        .foregroundStyle(theme.secondaryTextColor)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundStyle(theme.secondaryTextColor)
            }
            .padding()
            .background(theme.cardBackgroundColor)
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}
