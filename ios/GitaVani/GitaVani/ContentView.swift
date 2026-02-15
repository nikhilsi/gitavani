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
    let audioService: AudioService

    @State private var navigationPath = NavigationPath()
    @AppStorage("hasSeenOnboarding") private var hasSeenOnboarding = false
    @State private var showOnboarding = false
    @ScaledMetric(relativeTo: .body) private var dynamicTypeBase: CGFloat = 16

    var theme: AppTheme { themeManager.currentTheme }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            if dataService.isLoaded {
                ChapterListView(
                    dataService: dataService,
                    themeManager: themeManager,
                    settings: settings,
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
                    } else if route == "help" {
                        HelpView(theme: theme)
                    } else if route == "about" {
                        AboutView(theme: theme)
                    } else if route == "favorites" {
                        FavoritesView(
                            dataService: dataService,
                            themeManager: themeManager,
                            settings: settings
                        )
                    } else {
                        VerseDetailView(
                            initialVerseId: route,
                            dataService: dataService,
                            themeManager: themeManager,
                            settings: settings,
                            readingProgress: readingProgress,
                            audioService: audioService
                        )
                    }
                }
            } else if let error = dataService.loadError {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundStyle(.secondary)
                    Text("Unable to Load")
                        .font(.title2)
                        .fontWeight(.semibold)
                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
            } else {
                ProgressView("Loading...")
            }
        }
        .tint(theme.accentColor)
        .preferredColorScheme(themeManager.isDarkTheme ? .dark : .light)
        .fullScreenCover(isPresented: $showOnboarding) {
            OnboardingView {
                hasSeenOnboarding = true
                showOnboarding = false
            }
        }
        .onAppear {
            if !hasSeenOnboarding {
                showOnboarding = true
            }
            settings.dynamicTypeScale = dynamicTypeBase / 16.0
        }
        .onChange(of: dynamicTypeBase) { _, newValue in
            settings.dynamicTypeScale = newValue / 16.0
        }
    }
}
