package com.nikhilsi.gitavani.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalizedText(
    val en: String,
    val hi: String
) {
    fun forLanguage(language: String): String =
        if (language == "hindi") hi else en

    fun oppositeLanguage(language: String): String =
        if (language == "hindi") en else hi
}
