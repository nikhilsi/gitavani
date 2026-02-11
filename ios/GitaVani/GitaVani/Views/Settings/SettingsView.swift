import SwiftUI

struct SettingsView: View {
    let themeManager: ThemeManager
    let settings: AppSettings

    var theme: AppTheme { themeManager.currentTheme }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {
                ThemePickerView(themeManager: themeManager)

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                FontSizeView(settings: settings, theme: theme)

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // Default language
                VStack(alignment: .leading, spacing: 12) {
                    Text("Default Language")
                        .font(.headline)
                        .foregroundStyle(theme.primaryTextColor)

                    HStack(spacing: 0) {
                        languageButton("english", label: "English")
                        languageButton("hindi", label: "Hindi")
                    }
                    .background(theme.cardBackgroundColor)
                    .cornerRadius(8)
                }

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // Transliteration
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Transliteration")
                            .font(.headline)
                            .foregroundStyle(theme.primaryTextColor)
                        Text("Show romanized Sanskrit text")
                            .font(.caption)
                            .foregroundStyle(theme.secondaryTextColor)
                    }

                    Spacer()

                    Toggle("", isOn: Binding(
                        get: { settings.showTransliteration },
                        set: { settings.showTransliteration = $0 }
                    ))
                    .tint(theme.accentColor)
                    .labelsHidden()
                }
            }
            .padding()
        }
        .background(theme.backgroundColor)
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func languageButton(_ language: String, label: String) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.2)) {
                settings.defaultLanguage = language
            }
        } label: {
            Text(label)
                .font(.subheadline)
                .fontWeight(settings.defaultLanguage == language ? .semibold : .regular)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(settings.defaultLanguage == language ? theme.accentColor : Color.clear)
                .foregroundStyle(settings.defaultLanguage == language ? .white : theme.secondaryTextColor)
        }
        .buttonStyle(.plain)
    }
}
