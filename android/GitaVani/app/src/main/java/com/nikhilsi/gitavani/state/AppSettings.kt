package com.nikhilsi.gitavani.state

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gitavani_settings", Context.MODE_PRIVATE)

    // Detect tablet: smallest screen width >= 600dp
    private val isTablet = context.resources.configuration.smallestScreenWidthDp >= 600

    private val _fontSize = MutableStateFlow(
        prefs.getFloat("fontSize", if (isTablet) 22f else 18f)
    )
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _defaultLanguage = MutableStateFlow(
        prefs.getString("defaultLanguage", "english") ?: "english"
    )
    val defaultLanguage: StateFlow<String> = _defaultLanguage.asStateFlow()

    private val _showTransliteration = MutableStateFlow(
        prefs.getBoolean("showTransliteration", true)
    )
    val showTransliteration: StateFlow<Boolean> = _showTransliteration.asStateFlow()

    private val _selectedTheme = MutableStateFlow(
        prefs.getString("selectedTheme", "sattva") ?: "sattva"
    )
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _preferredHindiAuthor = MutableStateFlow(
        prefs.getString("preferredHindiAuthor", "") ?: ""
    )
    val preferredHindiAuthor: StateFlow<String> = _preferredHindiAuthor.asStateFlow()

    private val _preferredEnglishAuthor = MutableStateFlow(
        prefs.getString("preferredEnglishAuthor", "") ?: ""
    )
    val preferredEnglishAuthor: StateFlow<String> = _preferredEnglishAuthor.asStateFlow()

    private val _preferredCommentaryLanguage = MutableStateFlow(
        prefs.getString("preferredCommentaryLanguage", "") ?: ""
    )
    val preferredCommentaryLanguage: StateFlow<String> = _preferredCommentaryLanguage.asStateFlow()

    private val _preferredHindiCommentaryAuthor = MutableStateFlow(
        prefs.getString("preferredHindiCommentaryAuthor", "") ?: ""
    )
    val preferredHindiCommentaryAuthor: StateFlow<String> = _preferredHindiCommentaryAuthor.asStateFlow()

    private val _preferredEnglishCommentaryAuthor = MutableStateFlow(
        prefs.getString("preferredEnglishCommentaryAuthor", "") ?: ""
    )
    val preferredEnglishCommentaryAuthor: StateFlow<String> = _preferredEnglishCommentaryAuthor.asStateFlow()

    private val _preferredSanskritCommentaryAuthor = MutableStateFlow(
        prefs.getString("preferredSanskritCommentaryAuthor", "") ?: ""
    )
    val preferredSanskritCommentaryAuthor: StateFlow<String> = _preferredSanskritCommentaryAuthor.asStateFlow()

    private val _hasSeenOnboarding = MutableStateFlow(
        prefs.getBoolean("hasSeenOnboarding", false)
    )
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()

    private val _favoriteVerseIds = MutableStateFlow(
        loadFavorites()
    )
    val favoriteVerseIds: StateFlow<List<String>> = _favoriteVerseIds.asStateFlow()

    private fun loadFavorites(): List<String> {
        val json = prefs.getString("favoriteVerseIds", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        prefs.edit().putFloat("fontSize", size).apply()
    }

    fun setDefaultLanguage(language: String) {
        _defaultLanguage.value = language
        prefs.edit().putString("defaultLanguage", language).apply()
    }

    fun setShowTransliteration(show: Boolean) {
        _showTransliteration.value = show
        prefs.edit().putBoolean("showTransliteration", show).apply()
    }

    fun setSelectedTheme(theme: String) {
        _selectedTheme.value = theme
        prefs.edit().putString("selectedTheme", theme).apply()
    }

    fun setPreferredHindiAuthor(author: String) {
        _preferredHindiAuthor.value = author
        prefs.edit().putString("preferredHindiAuthor", author).apply()
    }

    fun setPreferredEnglishAuthor(author: String) {
        _preferredEnglishAuthor.value = author
        prefs.edit().putString("preferredEnglishAuthor", author).apply()
    }

    fun setPreferredCommentaryLanguage(language: String) {
        _preferredCommentaryLanguage.value = language
        prefs.edit().putString("preferredCommentaryLanguage", language).apply()
    }

    fun setPreferredHindiCommentaryAuthor(author: String) {
        _preferredHindiCommentaryAuthor.value = author
        prefs.edit().putString("preferredHindiCommentaryAuthor", author).apply()
    }

    fun setPreferredEnglishCommentaryAuthor(author: String) {
        _preferredEnglishCommentaryAuthor.value = author
        prefs.edit().putString("preferredEnglishCommentaryAuthor", author).apply()
    }

    fun setPreferredSanskritCommentaryAuthor(author: String) {
        _preferredSanskritCommentaryAuthor.value = author
        prefs.edit().putString("preferredSanskritCommentaryAuthor", author).apply()
    }

    fun setHasSeenOnboarding(seen: Boolean) {
        _hasSeenOnboarding.value = seen
        prefs.edit().putBoolean("hasSeenOnboarding", seen).apply()
    }

    fun isFavorite(verseId: String): Boolean =
        _favoriteVerseIds.value.contains(verseId)

    fun toggleFavorite(verseId: String) {
        val current = _favoriteVerseIds.value.toMutableList()
        val index = current.indexOf(verseId)
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, verseId)
        }
        _favoriteVerseIds.value = current
        prefs.edit().putString("favoriteVerseIds", Json.encodeToString(current)).apply()
    }
}
