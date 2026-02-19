package com.nikhilsi.gitavani.model

import kotlinx.serialization.Serializable

@Serializable
data class Verse(
    val id: String,
    val chapter: Int,
    val verse: Int,
    val slok: String,
    val transliteration: String,
    val translations: List<Translation>,
    val commentaries: List<Commentary>
)
