import SwiftUI

struct VerseNavigationView: View {
    let hasPrevious: Bool
    let hasNext: Bool
    let theme: AppTheme
    let onPrevious: () -> Void
    let onNext: () -> Void

    var body: some View {
        HStack {
            Button(action: onPrevious) {
                HStack(spacing: 4) {
                    Image(systemName: "chevron.left")
                    Text("Previous")
                }
                .font(.subheadline)
                .foregroundStyle(hasPrevious ? theme.accentColor : theme.secondaryTextColor.opacity(0.4))
            }
            .disabled(!hasPrevious)
            .accessibilityLabel("Previous verse")

            Spacer()

            Button(action: onNext) {
                HStack(spacing: 4) {
                    Text("Next")
                    Image(systemName: "chevron.right")
                }
                .font(.subheadline)
                .foregroundStyle(hasNext ? theme.accentColor : theme.secondaryTextColor.opacity(0.4))
            }
            .disabled(!hasNext)
            .accessibilityLabel("Next verse")
        }
        .padding()
    }
}
