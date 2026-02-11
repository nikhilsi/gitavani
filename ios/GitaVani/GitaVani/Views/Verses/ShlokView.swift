import SwiftUI

struct ShlokView: View {
    let slok: String
    let transliteration: String
    let showTransliteration: Bool
    let theme: AppTheme
    let fontSize: Double

    var body: some View {
        VStack(spacing: 12) {
            Text(slok)
                .font(.system(size: fontSize + 2))
                .multilineTextAlignment(.center)
                .foregroundStyle(theme.primaryTextColor)
                .lineSpacing(6)

            if showTransliteration {
                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                Text(transliteration)
                    .font(.system(size: fontSize - 2))
                    .italic()
                    .multilineTextAlignment(.center)
                    .foregroundStyle(theme.secondaryTextColor)
                    .lineSpacing(4)
            }
        }
        .padding()
        .background(theme.cardBackgroundColor)
        .cornerRadius(12)
    }
}
