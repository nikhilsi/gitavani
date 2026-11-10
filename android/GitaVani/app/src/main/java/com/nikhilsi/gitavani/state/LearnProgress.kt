package com.nikhilsi.gitavani.state

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/** Learn progress per verse (spec 6.6): New (no entry), Learning or Learned. Stored on the phone. */
class LearnProgress(context: Context) {
    enum class Status(val code: String) { LEARNING("learning"), LEARNED("learned") }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gitavani_progress", Context.MODE_PRIVATE)

    private val _statuses = MutableStateFlow(load())
    val statuses: StateFlow<Map<String, Status>> = _statuses.asStateFlow()

    private fun load(): Map<String, Status> {
        val json = runCatching { JSONObject(prefs.getString(KEY, "{}") ?: "{}") }.getOrNull() ?: return emptyMap()
        return json.keys().asSequence().mapNotNull { id ->
            Status.entries.firstOrNull { it.code == json.optString(id) }?.let { id to it }
        }.toMap()
    }

    private fun save(next: Map<String, Status>) {
        _statuses.value = next
        prefs.edit().putString(KEY, JSONObject(next.mapValues { it.value.code }).toString()).apply()
    }

    fun status(verseId: String): Status? = _statuses.value[verseId]

    /** The first take on a verse moves it from New to Learning. A Learned verse stays Learned. */
    fun markLearning(verseId: String) {
        if (verseId !in _statuses.value) save(_statuses.value + (verseId to Status.LEARNING))
    }

    fun markLearned(verseId: String) = save(_statuses.value + (verseId to Status.LEARNED))

    private companion object {
        const val KEY = "learnProgress"
    }
}
