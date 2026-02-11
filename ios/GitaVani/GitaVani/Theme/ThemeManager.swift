import SwiftUI

@Observable
class ThemeManager {
    var currentTheme: AppTheme

    init() {
        let savedThemeName = UserDefaults.standard.string(forKey: "selectedTheme") ?? "sattva"
        currentTheme = AppTheme.named(savedThemeName)
    }

    func setTheme(_ theme: AppTheme) {
        currentTheme = theme
        UserDefaults.standard.set(theme.name, forKey: "selectedTheme")
    }
}
