import SwiftUI
import UIKit

@Observable
class ThemeManager {
    var currentTheme: AppTheme

    var isDarkTheme: Bool {
        currentTheme.name == "dusk"
    }

    init() {
        let savedThemeName = UserDefaults.standard.string(forKey: "selectedTheme") ?? "sattva"
        currentTheme = AppTheme.named(savedThemeName)
        updateNavigationBarAppearance(for: currentTheme)
    }

    func setTheme(_ theme: AppTheme) {
        currentTheme = theme
        UserDefaults.standard.set(theme.name, forKey: "selectedTheme")
        updateNavigationBarAppearance(for: theme)
    }

    private func updateNavigationBarAppearance(for theme: AppTheme) {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(theme.backgroundColor)
        appearance.titleTextAttributes = [.foregroundColor: UIColor(theme.primaryTextColor)]
        appearance.largeTitleTextAttributes = [.foregroundColor: UIColor(theme.primaryTextColor)]

        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
        UINavigationBar.appearance().compactAppearance = appearance
        UINavigationBar.appearance().tintColor = UIColor(theme.accentColor)
    }
}
