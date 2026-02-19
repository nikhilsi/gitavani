package com.nikhilsi.gitavani.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verses_count") val versesCount: Int,
    val name: String,
    val translation: String,
    val transliteration: String,
    val meaning: LocalizedText,
    val summary: LocalizedText
)
