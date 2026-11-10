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
        lineWatch?.cancel()
        lineWatch = nil
        player?.stop()
        player = nil
        isPlaying = false
        currentVerseId = nil
    }

    // MARK: - Learn

    private var lineWatch: Task<Void, Never>?

    /// Plays one stretch of a verse's recording (a line in Learn) and returns when it ends or is stopped.
    func play(verseId: String, from start: Double, to end: Double) async {
        stop()
        guard let url = Bundle.main.url(forResource: verseId, withExtension: "mp3"),
              let player = try? AVAudioPlayer(contentsOf: url) else { return }
        player.delegate = self
        player.currentTime = start
        guard player.play() else { return }
        self.player = player
        currentVerseId = verseId
        isPlaying = true
        let watch = Task { [weak self] in
            while !Task.isCancelled, player.isPlaying, player.currentTime < end {
                try? await Task.sleep(for: .milliseconds(30))
            }
            guard !Task.isCancelled, let self, self.player === player else { return }
            self.stop()
        }
        lineWatch = watch
        await watch.value
    }

    /// The lesson records the learner. That needs play-and-record. Playback stays on the speaker or
    /// AirPods (A2DP) while the phone's own mic records (spec 7.1). Off restores the reading app's session.
    func setLessonMode(_ on: Bool) {
        stop()
        let session = AVAudioSession.sharedInstance()
        do {
            if on {
                try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetoothA2DP])
            } else {
                try session.setCategory(.playback, mode: .default)
            }
            try session.setActive(true)
        } catch {
            #if DEBUG
            print("AudioService: Failed to switch lesson mode: \(error)")
            #endif
        }
    }

    // MARK: - AVAudioPlayerDelegate

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        isPlaying = false
        currentVerseId = nil
    }
}
