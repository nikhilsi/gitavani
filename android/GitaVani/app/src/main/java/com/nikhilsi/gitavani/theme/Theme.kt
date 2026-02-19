package com.nikhilsi.gitavani.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppTheme = staticCompositionLocalOf { AppTheme.Sattva }

@Composable
fun GitaVaniTheme(
    appTheme: AppTheme = AppTheme.Sattva,
    content: @Composable () -> Unit
) {
    val colorScheme = if (appTheme.isDark) {
        darkColorScheme(
            background = appTheme.backgroundColor,
            surface = appTheme.backgroundColor,
            surfaceContainer = appTheme.cardBackgroundColor,
            onBackground = appTheme.primaryTextColor,
            onSurface = appTheme.primaryTextColor,
            primary = appTheme.accentColor,
            onPrimary = appTheme.primaryTextColor
        )
    } else {
        lightColorScheme(
            background = appTheme.backgroundColor,
            surface = appTheme.backgroundColor,
            surfaceContainer = appTheme.cardBackgroundColor,
            onBackground = appTheme.primaryTextColor,
            onSurface = appTheme.primaryTextColor,
            primary = appTheme.accentColor,
            onPrimary = appTheme.backgroundColor
        )
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
