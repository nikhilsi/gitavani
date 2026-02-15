import AVFoundation

@Observable
class AudioService: NSObject, AVAudioPlayerDelegate {
    var isPlaying = false
    var currentVerseId: String?

    private var player: AVAudioPlayer?

    override init() {
        super.init()
        setupAudioSession()
    }

    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            #if DEBUG
            print("AudioService: Failed to set up audio session: \(error)")
            #endif
        }
    }

    func play(verseId: String) {
        // If same verse is playing, pause it
        if currentVerseId == verseId && isPlaying {
            pause()
            return
        }

        // If same verse is paused, resume it
        if currentVerseId == verseId, let player, !player.isPlaying {
            player.play()
            isPlaying = true
            return
        }

        // New verse — stop current and load new
        stop()

        guard let url = Bundle.main.url(forResource: verseId, withExtension: "mp3") else {
            #if DEBUG
            print("AudioService: Audio file not found for \(verseId)")
            #endif
            return
        }

        do {
            player = try AVAudioPlayer(contentsOf: url)
            player?.delegate = self
            player?.play()
            currentVerseId = verseId
            isPlaying = true
        } catch {
            #if DEBUG
            print("AudioService: Failed to play \(verseId): \(error)")
            #endif
        }
    }

    func pause() {
        player?.pause()
        isPlaying = false
    }

    func stop() {
        player?.stop()
        player = nil
        isPlaying = false
        currentVerseId = nil
    }

    // MARK: - AVAudioPlayerDelegate

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        isPlaying = false
        currentVerseId = nil
    }
}
