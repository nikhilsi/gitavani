package com.nikhilsi.gitavani.theme

import androidx.compose.ui.graphics.Color

data class AppTheme(
    val name: String,
    val displayName: String,
    val backgroundColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val accentColor: Color,
    val cardBackgroundColor: Color
) {
    val isDark: Boolean get() = name == "dusk"

    companion object {
        val Sattva = AppTheme(
            name = "sattva",
            displayName = "Sattva",
            backgroundColor = Color(1.0f, 1.0f, 1.0f),
            primaryTextColor = Color(0.102f, 0.102f, 0.102f),
            secondaryTextColor = Color(0.4f, 0.4f, 0.4f),
            accentColor = Color(0.0f, 0.502f, 0.502f),
            cardBackgroundColor = Color(0.96f, 0.96f, 0.96f)
        )

        val Parchment = AppTheme(
            name = "parchment",
            displayName = "Parchment",
            backgroundColor = Color(0.961f, 0.941f, 0.91f),
            primaryTextColor = Color(0.243f, 0.173f, 0.11f),
            secondaryTextColor = Color(0.478f, 0.396f, 0.322f),
            accentColor = Color(0.757f, 0.471f, 0.09f),
            cardBackgroundColor = Color(0.929f, 0.906f, 0.867f)
        )

        val Dusk = AppTheme(
            name = "dusk",
            displayName = "Dusk",
            backgroundColor = Color(0.11f, 0.11f, 0.18f),
            primaryTextColor = Color(0.91f, 0.894f, 0.863f),
            secondaryTextColor = Color(0.659f, 0.596f, 0.502f),
            accentColor = Color(0.831f, 0.659f, 0.263f),
            cardBackgroundColor = Color(0.157f, 0.157f, 0.231f)
        )

        val Lotus = AppTheme(
            name = "lotus",
            displayName = "Lotus",
            backgroundColor = Color(1.0f, 0.961f, 0.902f),
            primaryTextColor = Color(0.29f, 0.11f, 0.11f),
            secondaryTextColor = Color(0.47f, 0.37f, 0.27f),
            accentColor = Color(0.86f, 0.33f, 0.0f),
            cardBackgroundColor = Color(0.976f, 0.929f, 0.863f)
        )

        val all = listOf(Sattva, Parchment, Dusk, Lotus)

        fun named(name: String): AppTheme =
            all.firstOrNull { it.name == name } ?: Sattva
    }
}
