import SwiftUI

struct AboutView: View {
    let theme: AppTheme

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }

    private var buildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                // App header
                VStack(spacing: 8) {
                    Image(systemName: "book.fill")
                        .font(.system(size: 40))
                        .foregroundStyle(theme.accentColor)

                    Text("GitaVani")
                        .font(.system(size: 24, weight: .bold, design: .serif))
                        .foregroundStyle(theme.primaryTextColor)

                    Text("Version \(appVersion) (\(buildNumber))")
                        .font(.caption)
                        .foregroundStyle(theme.secondaryTextColor)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // About
                aboutSection(
                    title: "About",
                    text: "GitaVani is a clean, ad-free reader for the Bhagavad Gita. All 701 verses across 18 chapters, with Sanskrit text, transliteration, and translations from 12 scholars in Hindi and English."
                )

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // Data source
                aboutSection(
                    title: "Data Source",
                    text: "Verse data sourced from the Vedic Scriptures Bhagavad Gita project by Pt. Prashant Tripathi. Includes translations and commentaries from renowned scholars including Swami Sivananda, Swami Ramsukhdas, Sri Shankaracharya, and others."
                )

                linkRow(title: "Vedic Scriptures Project", url: "https://github.com/vedicscriptures/bhagavad-gita")

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // License
                aboutSection(
                    title: "License",
                    text: "Verse data is used under the LGPL-3.0 license. The original Sanskrit shlokas of the Bhagavad Gita are ancient public domain text."
                )

                Divider()
                    .background(theme.secondaryTextColor.opacity(0.3))

                // Privacy
                aboutSection(
                    title: "Privacy",
                    text: "GitaVani collects no personal data. All data is stored locally on your device. No analytics, no tracking, no network calls. Your reading is entirely private."
                )
            }
            .padding()
        }
        .background(theme.backgroundColor)
        .navigationTitle("About")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(theme.backgroundColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
    }

    private func aboutSection(title: String, text: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundStyle(theme.primaryTextColor)

            Text(text)
                .font(.subheadline)
                .foregroundStyle(theme.secondaryTextColor)
                .lineSpacing(3)
        }
    }

    private func linkRow(title: String, url: String) -> some View {
        Link(destination: URL(string: url)!) {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .foregroundStyle(theme.accentColor)

                Spacer()

                Image(systemName: "arrow.up.right.square")
                    .font(.caption)
                    .foregroundStyle(theme.accentColor)
            }
        }
        .accessibilityLabel(title)
    }
}
