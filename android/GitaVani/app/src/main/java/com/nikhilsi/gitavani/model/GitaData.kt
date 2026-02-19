package com.nikhilsi.gitavani.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitaMetadata(
    val source: String,
    @SerialName("source_url") val sourceUrl: String,
    val license: String,
    @SerialName("generated_at") val generatedAt: String,
    @SerialName("total_chapters") val totalChapters: Int,
    @SerialName("total_verses") val totalVerses: Int
)

@Serializable
data class GitaData(
    val metadata: GitaMetadata,
    val chapters: List<Chapter>,
    val verses: List<Verse>
)
