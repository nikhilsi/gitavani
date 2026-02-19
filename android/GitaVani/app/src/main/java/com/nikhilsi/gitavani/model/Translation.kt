package com.nikhilsi.gitavani.model

import kotlinx.serialization.Serializable

@Serializable
data class Translation(
    val author: String,
    val language: Language,
    val text: String
)

@Serializable
data class Commentary(
    val author: String,
    val language: Language,
    val text: String
)
