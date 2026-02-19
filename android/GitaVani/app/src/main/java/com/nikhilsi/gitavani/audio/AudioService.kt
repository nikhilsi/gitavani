package com.nikhilsi.gitavani.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioService(private val context: Context) : DefaultLifecycleObserver {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentVerseId = MutableStateFlow<String?>(null)
    val currentVerseId: StateFlow<String?> = _currentVerseId

    private var player: MediaPlayer? = null

    fun play(verseId: String) {
        // If same verse is playing, pause it
        if (_currentVerseId.value == verseId && _isPlaying.value) {
            pause()
            return
        }

        // If same verse is paused, resume it
        if (_currentVerseId.value == verseId && player != null) {
            player?.start()
            _isPlaying.value = true
            return
        }

        // New verse — stop current and load new
        stop()

        val afd = try {
            context.assets.openFd("audio/$verseId.mp3")
        } catch (e: Exception) {
            return
        }

        try {
            player = MediaPlayer().apply {
                afd.use { fd ->
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentVerseId.value = null
                }
                prepare()
                start()
            }
            _currentVerseId.value = verseId
            _isPlaying.value = true
        } catch (e: Exception) {
            // Release the player on failure to prevent leaks
            player?.release()
            player = null
            _isPlaying.value = false
            _currentVerseId.value = null
        }
    }

    fun pause() {
        player?.pause()
        _isPlaying.value = false
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        _currentVerseId.value = null
    }

    // Lifecycle: pause audio when app goes to background
    override fun onStop(owner: LifecycleOwner) {
        if (_isPlaying.value) {
            pause()
        }
    }

    // Lifecycle: release resources when activity is destroyed
    override fun onDestroy(owner: LifecycleOwner) {
        stop()
    }
}
