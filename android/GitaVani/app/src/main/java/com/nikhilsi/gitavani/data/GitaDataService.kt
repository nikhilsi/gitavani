package com.nikhilsi.gitavani.data

import android.content.Context
import android.util.Log
import com.nikhilsi.gitavani.model.Chapter
import com.nikhilsi.gitavani.model.GitaData
import com.nikhilsi.gitavani.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GitaDataService {
    var chapters: List<Chapter> = emptyList()
        private set
    var verses: List<Verse> = emptyList()
        private set
    var isLoaded: Boolean = false
        private set
    var loadError: String? = null
        private set

    private var verseById: Map<String, Verse> = emptyMap()
    private var versesByChapter: Map<Int, List<Verse>> = emptyMap()
    private var chapterByNumber: Map<Int, Chapter> = emptyMap()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadData(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("gita_data.json")
                    .bufferedReader()
                    .use { it.readText() }

                val data = json.decodeFromString<GitaData>(jsonString)

                chapters = data.chapters
                verses = data.verses

                verseById = verses.associateBy { it.id }
                versesByChapter = verses.groupBy { it.chapter }
                chapterByNumber = chapters.associateBy { it.chapterNumber }

                isLoaded = true
                Log.d("GitaDataService", "Loaded ${chapters.size} chapters, ${verses.size} verses")
            } catch (e: Exception) {
                loadError = "Failed to load Gita data: ${e.message}"
                Log.e("GitaDataService", loadError!!, e)
            }
        }
    }

    fun versesForChapter(chapterNumber: Int): List<Verse> =
        versesByChapter[chapterNumber] ?: emptyList()

    fun verse(id: String): Verse? = verseById[id]

    fun chapter(number: Int): Chapter? = chapterByNumber[number]
}
