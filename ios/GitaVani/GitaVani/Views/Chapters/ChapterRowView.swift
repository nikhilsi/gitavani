import SwiftUI

struct ChapterRowView: View {
    let chapter: Chapter
    let theme: AppTheme

    var body: some View {
        HStack(spacing: 14) {
            Text("\(chapter.chapterNumber)")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundStyle(theme.accentColor)
                .frame(width: 40, alignment: .center)

            VStack(alignment: .leading, spacing: 4) {
                Text(chapter.name)
                    .font(.headline)
                    .foregroundStyle(theme.primaryTextColor)

                Text(chapter.meaning.en)
                    .font(.subheadline)
                    .foregroundStyle(theme.secondaryTextColor)
            }

            Spacer()

            Text("\(chapter.versesCount)")
                .font(.caption)
                .foregroundStyle(theme.secondaryTextColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(theme.cardBackgroundColor)
                .cornerRadius(8)
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
    }
}
