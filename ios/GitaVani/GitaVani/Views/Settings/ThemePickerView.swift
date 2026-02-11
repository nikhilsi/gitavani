import SwiftUI

struct ThemePickerView: View {
    let themeManager: ThemeManager

    var theme: AppTheme { themeManager.currentTheme }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Theme")
                .font(.headline)
                .foregroundStyle(theme.primaryTextColor)

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                ForEach(AppTheme.all, id: \.name) { t in
                    Button {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            themeManager.setTheme(t)
                        }
                    } label: {
                        VStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(t.backgroundColor)
                                .frame(height: 60)
                                .overlay(
                                    VStack(spacing: 4) {
                                        Text("Aa")
                                            .font(.title3)
                                            .foregroundStyle(t.primaryTextColor)
                                        RoundedRectangle(cornerRadius: 2)
                                            .fill(t.accentColor)
                                            .frame(width: 30, height: 3)
                                    }
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(theme.name == t.name ? t.accentColor : Color.clear, lineWidth: 2)
                                )

                            Text(t.displayName)
                                .font(.caption)
                                .foregroundStyle(theme.name == t.name ? theme.accentColor : theme.secondaryTextColor)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}
