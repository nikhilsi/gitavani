import SwiftUI

struct FontSizeView: View {
    let settings: AppSettings
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

                Slider(value: Binding(
                    get: { settings.fontSize },
                    set: { settings.fontSize = $0 }
                ), in: 14...28, step: 1)
                .tint(theme.accentColor)

                Text("A")
                    .font(.system(size: 28))
                    .foregroundStyle(theme.secondaryTextColor)
            }

            Text("Preview text at \(Int(settings.fontSize))pt")
                .font(.system(size: settings.fontSize))
                .foregroundStyle(theme.primaryTextColor)
        }
    }
}
