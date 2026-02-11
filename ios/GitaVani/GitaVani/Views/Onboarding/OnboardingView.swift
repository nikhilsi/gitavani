import SwiftUI

struct OnboardingPage {
    let icon: String
    let title: String
    let subtitle: String
    let description: String
}

struct OnboardingView: View {
    let onComplete: () -> Void

    @State private var currentPage = 0

    private let pages: [OnboardingPage] = [
        OnboardingPage(
            icon: "book.fill",
            title: "Welcome to GitaVani",
            subtitle: "The Bhagavad Gita, beautifully presented",
            description: "Read all 701 verses across 18 chapters — in Sanskrit, Hindi, and English. No ads, no clutter, just the sacred text."
        ),
        OnboardingPage(
            icon: "text.book.closed.fill",
            title: "Multiple Translations",
            subtitle: "12 scholars, 2 languages",
            description: "Switch between Hindi and English translations. Choose from renowned scholars like Swami Sivananda, Swami Gambirananda, and more."
        ),
        OnboardingPage(
            icon: "character.book.closed.fill",
            title: "Transliteration",
            subtitle: "Sanskrit in Roman script",
            description: "Can't read Devanagari? Toggle transliteration to see Sanskrit verses in Roman letters, making them accessible to everyone."
        ),
        OnboardingPage(
            icon: "paintpalette.fill",
            title: "Make It Yours",
            subtitle: "4 themes, adjustable font size",
            description: "Choose from Sattva (light), Parchment (warm), Dusk (dark), or Lotus (saffron). Adjust the font size to your comfort."
        )
    ]

    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $currentPage) {
                ForEach(Array(pages.enumerated()), id: \.offset) { index, page in
                    OnboardingPageView(page: page)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .always))

            // Bottom button
            Button(action: {
                if currentPage < pages.count - 1 {
                    withAnimation { currentPage += 1 }
                } else {
                    onComplete()
                }
            }) {
                Text(currentPage < pages.count - 1 ? "Next" : "Get Started")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color(red: 1.0, green: 0.42, blue: 0.0))
                    .foregroundStyle(.white)
                    .cornerRadius(14)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)

            if currentPage < pages.count - 1 {
                Button("Skip") {
                    onComplete()
                }
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .padding(.bottom, 20)
            } else {
                Spacer()
                    .frame(height: 40)
            }
        }
        .background(Color(.systemBackground))
    }
}

struct OnboardingPageView: View {
    let page: OnboardingPage

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: page.icon)
                .font(.system(size: 64))
                .foregroundStyle(Color(red: 1.0, green: 0.42, blue: 0.0))
                .padding(.bottom, 8)

            Text(page.title)
                .font(.title)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)

            Text(page.subtitle)
                .font(.title3)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Text(page.description)
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .lineSpacing(4)

            Spacer()
            Spacer()
        }
        .padding()
    }
}
