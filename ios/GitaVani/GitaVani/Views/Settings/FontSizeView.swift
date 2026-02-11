import SwiftUI

struct FontSizeView: View {
    @Bindable var settings: AppSettings
    let theme: AppTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Font Size")
                .font(.headline)
                .foregroundStyle(theme.primaryTextColor)

            HStack {
                Text("A")
                    .font(.system(size: 14))
                    .foregroundStyle(theme.secondaryTextColor)

                Slider(value: $settings.fontSize, in: 14...28, step: 1)
                .tint(theme.accentColor)

                Text("A")
                    .font(.system(size: 28))
                    .foregroundStyle(theme.secondaryTextColor)
            }

            Text("Preview text at \(Int(settings.fontSize))pt")
                .font(.system(size: settings.scaledFontSize))
                .foregroundStyle(theme.primaryTextColor)
        }
    }
}
