package com.nikhilsi.gitavani.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Language {
    @SerialName("hindi") HINDI,
    @SerialName("english") ENGLISH,
    @SerialName("sanskrit") SANSKRIT
}
