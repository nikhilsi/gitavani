package com.nikhilsi.gitavani.state

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReadingProgress(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gitavani_progress", Context.MODE_PRIVATE)

    private val _lastReadChapter = MutableStateFlow(prefs.getInt("lastReadChapter", 0))
    val lastReadChapter: StateFlow<Int> = _lastReadChapter.asStateFlow()

    private val _lastReadVerse = MutableStateFlow(prefs.getInt("lastReadVerse", 0))
    val lastReadVerse: StateFlow<Int> = _lastReadVerse.asStateFlow()

    val hasProgress: Boolean
        get() = _lastReadChapter.value > 0 && _lastReadVerse.value > 0

    fun update(chapter: Int, verse: Int) {
        _lastReadChapter.value = chapter
        _lastReadVerse.value = verse
        prefs.edit()
            .putInt("lastReadChapter", chapter)
            .putInt("lastReadVerse", verse)
            .apply()
    }
}
