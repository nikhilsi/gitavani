//
//  ContentView.swift
//  GitaVani
//
//  Created by Nikhil Singhal on 2/11/26.
//

import SwiftUI

struct ContentView: View {
    let dataService: GitaDataService
    let themeManager: ThemeManager
    let settings: AppSettings
    let readingProgress: ReadingProgress

    @State private var navigationPath = NavigationPath()

    var theme: AppTheme { themeManager.currentTheme }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            if dataService.isLoaded {
                ChapterListView(
                    dataService: dataService,
                    themeManager: themeManager,
                    readingProgress: readingProgress,
                    navigationPath: $navigationPath
                )
                .navigationDestination(for: Int.self) { chapterNumber in
                    ChapterDetailView(
                        chapterNumber: chapterNumber,
                        dataService: dataService,
                        themeManager: themeManager,
                        settings: settings,
                        readingProgress: readingProgress
                    )
                }
                .navigationDestination(for: String.self) { route in
                    if route == "settings" {
                        SettingsView(
                            themeManager: themeManager,
                            settings: settings
                        )
                    } else {
                        VerseDetailView(
                            initialVerseId: route,
                            dataService: dataService,
                            themeManager: themeManager,
                            settings: settings,
                            readingProgress: readingProgress
                        )
                    }
                }
            } else {
                ProgressView("Loading...")
            }
        }
        .tint(theme.accentColor)
    }
}
