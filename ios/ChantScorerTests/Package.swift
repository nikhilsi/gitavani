// swift-tools-version: 6.0
// Test harness for GitaVani's Learn scorer. The source is the app's own file, linked in at
// Sources/ChantScorer/ChantScorer.swift. The app and the tests compile the same code.
// Run: cd ios/ChantScorerTests && swift test
import PackageDescription

let package = Package(
    name: "ChantScorerTests",
    platforms: [.macOS(.v14)],
    targets: [
        .target(name: "ChantScorer", path: "Sources/ChantScorer"),
        .testTarget(name: "ChantScorerTests", dependencies: ["ChantScorer"], path: "Tests/ChantScorerTests"),
    ]
)
