import SwiftUI

struct ChapterRowView: View {
    let chapter: Chapter
    let theme: AppTheme
    let fontSize: Double

    var body: some View {
        HStack(spacing: 14) {
            Text("\(chapter.chapterNumber)")
                .font(.system(size: fontSize + 4, weight: .bold))
                .foregroundStyle(theme.accentColor)
                .frame(width: 40, alignment: .center)

            VStack(alignment: .leading, spacing: 4) {
                Text(chapter.name)
                    .font(.system(size: fontSize, weight: .semibold))
                    .foregroundStyle(theme.primaryTextColor)

                Text(chapter.meaning.en)
                    .font(.system(size: fontSize - 2))
                    .foregroundStyle(theme.secondaryTextColor)
            }

            Spacer()

            Text("\(chapter.versesCount)")
                .font(.system(size: fontSize - 4))
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
