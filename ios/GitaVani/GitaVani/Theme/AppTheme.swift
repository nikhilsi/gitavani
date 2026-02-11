import SwiftUI

struct AppTheme {
    let name: String
    let displayName: String
    let backgroundColor: Color
    let primaryTextColor: Color
    let secondaryTextColor: Color
    let accentColor: Color
    let cardBackgroundColor: Color
}

extension AppTheme {
    static let sattva = AppTheme(
        name: "sattva",
        displayName: "Sattva",
        backgroundColor: Color(red: 1.0, green: 1.0, blue: 1.0),
        primaryTextColor: Color(red: 0.102, green: 0.102, blue: 0.102),
        secondaryTextColor: Color(red: 0.4, green: 0.4, blue: 0.4),
        accentColor: Color(red: 0.0, green: 0.502, blue: 0.502),
        cardBackgroundColor: Color(red: 0.96, green: 0.96, blue: 0.96)
    )

    static let parchment = AppTheme(
        name: "parchment",
        displayName: "Parchment",
        backgroundColor: Color(red: 0.961, green: 0.941, blue: 0.91),
        primaryTextColor: Color(red: 0.243, green: 0.173, blue: 0.11),
        secondaryTextColor: Color(red: 0.478, green: 0.396, blue: 0.322),
        accentColor: Color(red: 0.757, green: 0.471, blue: 0.09),
        cardBackgroundColor: Color(red: 0.929, green: 0.906, blue: 0.867)
    )

    static let dusk = AppTheme(
        name: "dusk",
        displayName: "Dusk",
        backgroundColor: Color(red: 0.11, green: 0.11, blue: 0.18),
        primaryTextColor: Color(red: 0.91, green: 0.894, blue: 0.863),
        secondaryTextColor: Color(red: 0.659, green: 0.596, blue: 0.502),
        accentColor: Color(red: 0.831, green: 0.659, blue: 0.263),
        cardBackgroundColor: Color(red: 0.157, green: 0.157, blue: 0.231)
    )

    static let lotus = AppTheme(
        name: "lotus",
        displayName: "Lotus",
        backgroundColor: Color(red: 1.0, green: 0.961, blue: 0.902),
        primaryTextColor: Color(red: 0.29, green: 0.11, blue: 0.11),
        secondaryTextColor: Color(red: 0.47, green: 0.37, blue: 0.27),
        accentColor: Color(red: 0.86, green: 0.33, blue: 0.0),
        cardBackgroundColor: Color(red: 0.976, green: 0.929, blue: 0.863)
    )

    static let all: [AppTheme] = [.sattva, .parchment, .dusk, .lotus]

    static func named(_ name: String) -> AppTheme {
        all.first { $0.name == name } ?? .sattva
    }
}
