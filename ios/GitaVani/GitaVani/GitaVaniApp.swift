//
//  GitaVaniApp.swift
//  GitaVani
//
//  Created by Nikhil Singhal on 2/11/26.
//

import SwiftUI

@main
struct GitaVaniApp: App {
    @State private var dataService = GitaDataService()
    @State private var themeManager = ThemeManager()
    @State private var settings = AppSettings()
    @State private var readingProgress = ReadingProgress()
    @State private var audioService = AudioService()

    var body: some Scene {
        WindowGroup {
            ContentView(
                dataService: dataService,
                themeManager: themeManager,
                settings: settings,
                readingProgress: readingProgress,
                audioService: audioService
            )
        }
    }
}
